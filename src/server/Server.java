package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    private static final String BODY = "<html><head><link rel=\"icon\" href=\"data:,\"></head><body><h1 align=\"center\">Nohup.out</h1></body></html>";
    private static final String HEADER =
            "HTTP/1.1 200 OK\n" +
                    "Server: MyServer\n" +
                    "Content-Type: text/html; charset=utf-8\n" +

                    "Connection: close\n\n";
//"Content-Length: %s\n"+

    public void start() {

        try (ServerSocket server = new ServerSocket(5050)) {
            System.out.println("Server started");
            while (true) {
                try (Socket socket = server.accept()) {
                    //socket.setSoTimeout(1000);
                    try (FileInputStream fileReader = new FileInputStream(new File("nohup.out"))) {
                        try (OutputStream outputStream = socket.getOutputStream()) {
                            byte[] bytes = new byte[fileReader.available()];
                            fileReader.read(bytes);
                            String body = new String(bytes);
                            String page = HEADER + BODY + body;
                            outputStream.write(page.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//                        String line;
//                        while ((line = bufferedReader.readLine()) != null) {
//                            if (!line.equals("")) {
//                                System.out.println(line);
//                            } else {
//                                try (OutputStream outputStream = socket.getOutputStream()) {
//                                    String page = String.format(HEADER, BODY.length()) + BODY;
//                                    outputStream.write(page.getBytes());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                break;
//                            }
//                        }
//                    } catch (IOException e) {
//                        System.err.println(e.getMessage());
//                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
