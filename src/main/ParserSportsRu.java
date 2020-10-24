package main;

import data.Article;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ParserSportsRu {

    Db db = new Db();

    private final List<String> newsDate = new ArrayList<>();
    private final List<String> newsTitle = new ArrayList<>();
    private final List<String> newsLink = new ArrayList<>();
    private final List<String> newsText = new ArrayList<>();
    private final List<Article> articles = new ArrayList<>();

    public void start() {
        System.out.println("Start session: " + new Date().toString());
        db.connect();
        parse();
        if (articles.size() != 0) {
            articles.forEach(article -> {
                if (!article.getText().startsWith("Sports.ru")) {
                    db.addToDb(article);
                }
            });
            System.out.println("Articles added: " + articles.size());
        } else {
            System.out.println("New articles is not found");
        }
        db.disconnect();
        System.out.println("End session: " + new Date().toString());
    }

    public void parse() {
        try {
            Document doc = Jsoup.connect("https://www.sports.ru/f1-championship/").get();
            Elements elements = doc.getElementsByClass("nl-item");
            elements.forEach(element -> element.select("a").eachText().forEach(s -> {
                if (s.equals("Sports.ru")) {
                    List<String> date = element.getElementsByClass("date").eachText();
                    Elements news = element.getElementsByClass("short-text");
                    newsTitle.addAll(news.eachText());
                    newsLink.addAll(news.eachAttr("href"));
                    newsDate.addAll(changeListDate(date));
                }
            }));
            getNewArticlesLink();
            changeToObject(newsDate, newsTitle, newsLink, newsText, newsText.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> changeListDate(List<String> list) {
        String day = list.get(0);
        List<String> newDateList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            newDateList.add(day + " " + list.get(i));
        }
        return newDateList;
    }

    private void getNewArticlesLink() {
        ArrayList<String> newLinks = new ArrayList<>();
        newsLink.forEach(link -> {
            if (!newLinks.contains(link)) {
                newLinks.add(link);
            } else {
                int i = newLinks.size();
                newsTitle.remove(i);
                newsDate.remove(i);
                newsLink.remove(i);
            }
        });
        List<String> links = db.getLinkFromDb(newsLink.size());
        newsLink.forEach(link -> {
            if (!links.contains(link)) {
                parseNewsTextFromLink(link);
            }
        });
    }

    private void parseNewsTextFromLink(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            Elements elements = document.body().getElementsByClass("news-item__content js-mediator-article");
            elements.forEach(element -> {
                element.select("strong").remove();
                newsText.add(element.text());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeToObject(List<String> date, List<String> title, List<String> link, List<String> text, int newArticlesCount) {
        for (int i = 0; i < newArticlesCount; i++) {
            articles.add(new Article(
                    date.get(i),
                    title.get(i),
                    link.get(i),
                    text.get(i)));
        }
        Collections.reverse(articles);
    }

}
