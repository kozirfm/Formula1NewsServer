package main;

import database.Db;
import model.*;
import server.Server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        Db db = new Db();
        db.connect();

        new Thread(() -> new Server(db).start()).start();
//        ParserF1NewsRu parserF1NewsRu = new ParserF1NewsRu(db);
//        ParserCalendar parserChampionshipCalendar = new ParserCalendar(db);
//        start(parserF1NewsRu, parserChampionshipCalendar);
        ParseNews parseNewsSportsRu = new ParseNews(db);
        ParseChampionship parseChampionship = new ParseChampionship(db);
        start(parseNewsSportsRu, parseChampionship);
    }

    public static void start(ParseNews parseArticle, ParseChampionship parseChampionship){
        TimerTask parsingNewsF1News = new TimerTask() {
            @Override
            public void run() {
                parseArticle.parse();
            }
        };

        TimerTask parsingCalendar = new TimerTask() {
            @Override
            public void run() {
                parseChampionship.parse();
            }
        };

        new Timer().schedule(parsingNewsF1News, 0, TimeUnit.MINUTES.toMillis(30));
        new Timer().schedule(parsingCalendar, 5000, TimeUnit.DAYS.toMillis(1));
    }

    public static void start(ParserF1NewsRu parserF1NewsRu, ParserCalendar parserCalendar) {
        TimerTask parsingNewsF1News = new TimerTask() {
            @Override
            public void run() {
                parserF1NewsRu.parse();
            }
        };

        TimerTask parsingCalendar = new TimerTask() {
            @Override
            public void run() {
                parserCalendar.parse();
            }
        };

        new Timer().schedule(parsingNewsF1News, 0, TimeUnit.MINUTES.toMillis(30));
        new Timer().schedule(parsingCalendar, 0, TimeUnit.DAYS.toMillis(1));

    }

}
