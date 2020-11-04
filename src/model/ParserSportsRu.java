package model;

import database.Db;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ParserSportsRu {

    ParseArticle parseArticle = new ParseArticle();
    ParseChampionship parseChampionship = new ParseChampionship();

    public void start() {
        TimerTask parsingArticles = new TimerTask() {
            @Override
            public void run() {
                parseArticle.parse();
            }
        };

        TimerTask parsingDriversPosition = new TimerTask() {
            @Override
            public void run() {
                parseChampionship.parse();
            }
        };

        new Timer().schedule(parsingArticles, 0, TimeUnit.MINUTES.toMillis(30));
        new Timer().schedule(parsingDriversPosition, 0, TimeUnit.DAYS.toMillis(1));

    }
}
