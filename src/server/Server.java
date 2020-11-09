package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.Article;
import data.Driver;
import data.Team;
import database.Db;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

    private final Db db = new Db();

    public void start() {

        try (ServerSocket server = new ServerSocket(5050)) {
            System.out.println("Server started");
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
            while (true) {
                Socket socket = server.accept();
                socket.setSoTimeout(5000);
                fixedThreadPool.execute(() -> {
                    if (!socket.isClosed()) {
                        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream())) {
                            byte[] b = new byte[1024];
                            if (bufferedInputStream.read(b) > -1) {
                                String inputStreamLine = new String(b).trim();
                                String[] line = inputStreamLine.split("\n");

//                                if (inputStreamLine.startsWith("POST")) {
//                                    String json = line[line.length - 1];
//                                    User user = new Gson().fromJson(json, User.class);
//                                    Db db = new Db();
//                                    db.connect();
//                                    if (!db.isContainsUsernameOrEmail(user.getUsername(), user.getEmail())) {
//                                        db.addNewUserToDb(user);
//                                    }
//                                    db.disconnect();
//                                }

                                if (inputStreamLine.startsWith("GET")) {
                                    String[] headerString = line[0].split(" ");
                                    String requestString = headerString[1];
                                    HashMap<String, Integer> values = parseGetLineWithKeyValue(requestString);
                                    if (values.containsKey("count")) {
                                        try (OutputStream outputStream = socket.getOutputStream()) {
                                            db.connect();
                                            List<Article> articles = db.getArticlesFromDb(values.get("count"));
                                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                            String page = HEADER + gson.toJson(articles);
                                            outputStream.write(page.getBytes());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (parseSimpleString(requestString).contains("championship")) {
                                        try (OutputStream outputStream = socket.getOutputStream()) {
                                            db.connect();
                                            List<Driver> drivers = db.getDriversChampionshipTable();
                                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                            String page = HEADER + gson.toJson(makeTeamFromDrivers(drivers));
                                            outputStream.write(page.getBytes());
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Remote Socket Address: " + socket.getRemoteSocketAddress());
                        } finally {
                            db.disconnect();
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

    private String parseSimpleString(String requestString) {
        String[] s = requestString.split("/");
        return s.length > 1 ? s[1] : "";
    }

    private HashMap<String, Integer> parseGetLineWithKeyValue(String requestString) {
        HashMap<String, Integer> values = new HashMap<>();
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

    private List<Team> makeTeamFromDrivers(List<Driver> drivers) {
        List<Team> teams = new ArrayList<>();
        List<String> teamsName = new ArrayList<>();
        drivers.forEach(driver -> {
            if (!teamsName.contains(driver.getTeam())) {
                teamsName.add(driver.getTeam());
                teams.add(new Team(driver.getTeam()));
            }
        });

        drivers.forEach(driver -> {
            for (Team team : teams) {
                if (team.getName().equals(driver.getTeam())) {
                    team.getDrivers().add(driver);
                }
            }
        });
        return teams;
    }

}
