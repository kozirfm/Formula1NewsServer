package main;

import server.Server;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        new Thread(() -> new Server().start()).start();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ParserSportsRu parser = new ParserSportsRu();
                parser.start();
            }
        };

        new Timer().schedule(timerTask, 0, TimeUnit.MINUTES.toMillis(30));

    }

}
