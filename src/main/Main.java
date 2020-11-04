package main;

import model.ParserSportsRu;
import server.Server;

public class Main {

    public static void main(String[] args) {

        new Thread(() -> new Server().start()).start();
        ParserSportsRu parserSportsRu = new ParserSportsRu();
        parserSportsRu.start();

    }

}
