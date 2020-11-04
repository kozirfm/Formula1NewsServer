package model;

import data.Driver;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseChampionship {

    private final List<Driver> drivers = new ArrayList<>();
    private final Db db = new Db();

    public void parse() {
        try {
            db.connect();
            Document doc = Jsoup.connect("https://www.sports.ru/f1-championship/table/").get();
            Elements elements = doc.getElementsByTag("tbody");
            Elements driversElements = elements.get(1).select("tr");
            driversElements.forEach(driver -> addDriver(driver.text()));
            db.updateDriversPositionTable(drivers);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
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
