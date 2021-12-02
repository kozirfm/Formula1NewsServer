package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.*;
import database.Db;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String HEADER =
            "HTTP/1.1 200 OK\n" +
                    "Server: UbuntuServer\n" +
                    "Content-Type: application/json; charset=utf-8\n" +
                    "Connection: close\n\n";

    public Server(Db db) {
        this.db = db;
    }

    private final Db db;

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

                                if (inputStreamLine.startsWith("POST")) {
                                    String json = line[line.length - 1];
                                    System.out.println(json);
//                                    User user = new Gson().fromJson(json, User.class);
//                                    Db db = new Db();
//                                    db.connect();
//                                    if (!db.isContainsUsernameOrEmail(user.getUsername(), user.getEmail())) {
//                                        db.addNewUserToDb(user);
//                                    }
//                                    db.disconnect();
                                }

                                if (inputStreamLine.startsWith("GET")) {
                                    String[] headerString = line[0].split(" ");
                                    String requestString = headerString[1];
                                    HashMap<String, Integer> values = parseGetLineWithKeyValue(requestString);
                                    RequestVersion requestVersion = getRequestVersion(requestString);
                                    if (values.containsKey("count")) {
                                        handleNewsByCount(socket, values, requestVersion);
                                    }
                                    if (values.containsKey("page")) {
                                        handleNewsByPage(socket, values, requestVersion);
                                    }
                                    if (parseSimpleString(requestString).contains("championship")) {
                                        handleChampionship(socket, requestVersion);
                                    }
                                    if (parseSimpleString(requestString).contains("calendar")) {
                                        handleCalendar(socket, requestVersion);
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

    private String parseSimpleString(String requestString) {
        String[] s = requestString.split("/");
        return s.length > 1 ? s[1] : "";
    }

    private RequestVersion getRequestVersion(String requestString) {
        if (parseSimpleString(requestString).startsWith("v2")) {
            return RequestVersion.VERSION2;
        } else {
            return RequestVersion.VERSION1;
        }
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
        //Создаем команды
        drivers.forEach(driver -> {
            if (!teamsName.contains(driver.getTeam())) {
                teamsName.add(driver.getTeam());
                teams.add(new Team(driver.getTeam()));
            }
        });
        //Добавляем пилотов
        drivers.forEach(driver -> {
            for (Team team : teams) {
                if (team.getName().equals(driver.getTeam())) {
                    team.getDrivers().add(driver);
                    team.setPoints(team.getPoints() + driver.getPoints());
                }
            }
        });
        teams.sort(Comparator.comparingInt(Team::getPoints).reversed());
        return teams;
    }

    private <T> String getResponseByVersion(T data, RequestVersion requestVersion) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String response;
        if (requestVersion == RequestVersion.VERSION1) {
            response = gson.toJson(data);
        } else {
            response = gson.toJson(new ServerResponse<>("OK", 200, "OK", data));
        }
        return response;
    }

    private void handleNewsByCount(Socket socket, HashMap<String, Integer> values, RequestVersion requestVersion) {
        try (OutputStream outputStream = socket.getOutputStream()) {
            List<News> news = db.getArticlesFromDb(values.get("count"));
            String page = HEADER + getResponseByVersion(news, requestVersion);
            outputStream.write(page.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNewsByPage(Socket socket, HashMap<String, Integer> values, RequestVersion requestVersion) {
        try (OutputStream outputStream = socket.getOutputStream()) {
            List<News> news = db.getArticlesFromDbForPage(values.get("page"));
            String page = HEADER + getResponseByVersion(news, requestVersion);
            outputStream.write(page.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleChampionship(Socket socket, RequestVersion requestVersion) {
        try (OutputStream outputStream = socket.getOutputStream()) {
            List<Driver> drivers = db.getDriversChampionshipTable();
            String page = HEADER + getResponseByVersion(drivers, requestVersion);
            outputStream.write(page.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCalendar(Socket socket, RequestVersion requestVersion) {
        try (OutputStream outputStream = socket.getOutputStream()) {
            List<GrandPrix> grandPrixes = db.getCalendarTable();
            String page = HEADER + getResponseByVersion(grandPrixes, requestVersion);
            outputStream.write(page.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
