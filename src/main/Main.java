package main;

import data.Article;
import database.Db;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Db db = new Db();
                ParserSportsRu parser = new ParserSportsRu();
                System.out.println("Start session: " + new Date().toString());
                db.connect();
                parser.parse();
                List<Article> articleList = db.getFromDb(parser.getArticles().size());
                parser.getArticles().forEach(article -> {
                    if (!articleList.contains(article)){
                        db.addToDb(article);
                        System.out.println(article);
                    }
                });
                db.disconnect();
                System.out.println("End session: " + new Date().toString());
            }
        };

        new Timer().schedule(timerTask, 0, TimeUnit.MINUTES.toMillis(30));

    }

}
