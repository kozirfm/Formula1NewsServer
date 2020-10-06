package main;

import data.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ParserSportsRu {

    private final List<String> newsDate = new ArrayList<>();
    private final List<String> newsTitle = new ArrayList<>();
    private final List<String> newsLink = new ArrayList<>();
    private final List<String> newsText = new ArrayList<>();
    private final List<Article> articles = new ArrayList<>();

    public List<Article> getArticles() {
        return articles;
    }

    public void parse() {
        try {
            Document doc = Jsoup.connect("https://www.sports.ru/f1-championship/").get();
            Elements elements = doc.getElementsByClass("nl-item");
            elements.forEach(element -> element.select("a").eachText().forEach(s -> {
                if (s.equals("Sports.ru")) {
                    List<String> list = element.getElementsByClass("date").eachText();
                    Elements news = element.getElementsByClass("short-text");
                    newsTitle.addAll(news.eachText());
                    newsLink.addAll(news.eachAttr("href"));
                    newsDate.addAll(changeListDate(list));
                }
            }));
            newsLink.forEach(this::parseNewsTextFromLink);
            changeToObject(newsDate, newsTitle, newsLink, newsText);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private List<String> changeListDate(List<String> list) {
        String day = list.get(0);
        List<String> newDateList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            newDateList.add(day + " " + list.get(i));
        }
        return newDateList;
    }

    private void changeToObject(List<String> date, List<String> title, List<String> link, List<String> text) {
        for (int i = 0; i < date.size(); i++) {
            articles.add(new Article(
                    date.get(i),
                    title.get(i),
                    link.get(i),
                    text.get(i)));
        }
        Collections.reverse(articles);
    }
}
