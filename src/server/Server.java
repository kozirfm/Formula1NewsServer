package server;

import com.google.gson.Gson;
import data.Article;
import database.Db;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String HEADER =
            "HTTP/1.1 200 OK\n" +
                    "Server: MyServer\n" +
                    "Content-Type: application/json; charset=utf-8\n" +
                    "Connection: close\n\n";

    public void start() {

        try (ServerSocket server = new ServerSocket(5050)) {
            System.out.println("Server started");
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
            while (true) {
                Socket socket = server.accept();
                socket.setSoTimeout(5000);
                fixedThreadPool.execute(() -> {
                    if (!socket.isClosed()) {
                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            String line = bufferedReader.readLine();
                            if (line != null) {
                                if (line.startsWith("GET")) {
                                    HashMap<String, Integer> values = parseLine(line);
                                    if (values.containsKey("count")) {
                                        try (OutputStream outputStream = socket.getOutputStream()) {
                                            Db db = new Db();
                                            db.connect();
                                            List<Article> articles = db.getArticlesFromDb(values.get("count"));
                                            db.disconnect();
                                            Gson gson = new Gson();
                                            String page = HEADER + gson.toJson(articles);
                                            outputStream.write(page.getBytes());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Remote Socket Address: " + socket.getRemoteSocketAddress());
                        } finally {
                            try {
                                if (!socket.isClosed()) {
                                    socket.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    private HashMap<String, Integer> parseLine(String line) {
        HashMap<String, Integer> values = new HashMap<>();
        String[] headerString = line.split(" ");
        String requestString = headerString[1];
        String[] questionMark = requestString.split("\\?");
        if (questionMark.length == 2) {
            String afterQuestionMarkString = questionMark[1];
            String[] link = afterQuestionMarkString.split("&");
            for (String s : link) {
                String[] value = s.split("=");
                if (value.length == 2) {
                    if (!values.containsKey(value[0])) {
                        values.put(value[0], Integer.valueOf(value[1]));
                    }
                }
            }
        }
        return values;
    }
}
