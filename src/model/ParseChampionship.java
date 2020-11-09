package model;

import data.Driver;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ParseChampionship {

    private final List<Driver> drivers = new ArrayList<>();
    private final Db db = new Db();

    public void parse() {
        try {
            db.connect();
            URL url = new URL("https://www.sports.ru/f1-championship/table/");
            Document doc = Jsoup.parse(url, (int) TimeUnit.SECONDS.toMillis(30));
            Elements elements = doc.getElementsByTag("tbody");
            Elements driversElements = elements.get(1).select("tr");
            driversElements.forEach(driver -> addDriver(driver.text()));
            db.updateDriversPositionTable(drivers);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
    }

    private void addDriver(String driver) {
        String[] split = driver.split(" ");
        int position = Integer.parseInt(split[0]);
        String name = split[1];
        String surname = split[2];
        String team;
        int points;
        if (split.length == 5) {
            team = split[3];
            points = Integer.parseInt(split[4]);
        } else {
            team = String.format("%s %s", split[3], split[4]);
            points = Integer.parseInt(split[5]);
        }
        drivers.add(new Driver(position, name, surname, team, points));
    }

}
