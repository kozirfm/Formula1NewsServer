package model;

import data.GrandPrix;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserCalendar implements Constants {

    private static final String SEASON_YEAR = "2021";
    List<GrandPrix> grandPrixList = new ArrayList<>();

    public ParserCalendar(Db db) {
        this.db = db;
    }

    private final Db db;

    public void parse() {
        try {
            Document calendarPage = Jsoup.connect(BASE_URL_F1NEWS_CALENDAR).get();
            Elements articleBody = calendarPage.getElementsByAttributeValue("itemprop", "articleBody");
            Elements tableRow = articleBody.select("tr");
            List<Elements> tableData = new ArrayList<>();
            for (int i = 1; i < tableRow.size(); i++) {
                tableData.add(tableRow.get(i).getElementsByTag("td"));
            }
            tableData.forEach(data -> {
                String[] splitDate = data.get(0).text().split("/");
                String date = String.format("%s.%s.%s", splitDate[0], splitDate[1], SEASON_YEAR);
                String flag = "https:" + data.get(1).select("img").attr("src");
                String grandPrix = data.get(2).text();
                String track = data.get(3).text();
                String length = data.get(4).text();
                String laps = data.get(5).text();
                String distance = data.get(6).text();
                String driver = data.get(7).text();
                String team = data.get(8).text();
                if (!track.equals("-")) {
                    grandPrixList.add(new GrandPrix(date, flag, grandPrix, track, length, laps, distance, driver, team));
                }
            });
            db.updateCalendarTable(grandPrixList);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            grandPrixList.clear();
        }
    }
}
