package model;

import data.Article;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ParseArticle {

    private final Db db = new Db();
    private final List<String> newsDate = new ArrayList<>();
    private final List<String> newsTitle = new ArrayList<>();
    private final List<String> newsLink = new ArrayList<>();
    private final List<String> newsText = new ArrayList<>();
    private final List<Article> articles = new ArrayList<>();

    public void parse() {
        try {
            System.out.println("Start session: " + new Date().toString());
            db.connect();
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
            alignmentAllArray(compareLinksArrayToLinksDb());
            changeToObject(newsDate, newsTitle, newsLink, newsText);
            addArticlesToDb(articles);
            System.out.println("End session: " + new Date().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.disconnect();
        }
    }

    //Изменяет список с датой
    private List<String> changeListDate(List<String> list) {
        String day = list.get(0);
        List<String> newDateList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            newDateList.add(day + " " + list.get(i));
        }
        return newDateList;
    }

    //Сравнивает полученные с сайта статьи со списком статей из базы данных и возвращает номера новых статей в списках
    private List<Integer> compareLinksArrayToLinksDb() {
        List<String> articlesLinksFromDb = db.getLinksFromDb(newsLink.size());
        List<Integer> index = new ArrayList<>();
        if (articlesLinksFromDb.size() > 0) {
            System.out.println("Compare links array to links db before: " + articlesLinksFromDb.size());
            newsLink.forEach(link -> {
                if (!articlesLinksFromDb.contains(link)) {
                    index.add(newsLink.indexOf(link));
                }
            });
            System.out.println("Compare links array to links db after: " + index.size() + " " + index);
        }
        return index;
    }

    //Получает список номеров новых статей, чистит коллекции и заполняет коллекции новыми статьями
    private void alignmentAllArray(List<Integer> index) {
        List<String> temporaryTitles = new ArrayList<>(newsTitle);
        List<String> temporaryDates = new ArrayList<>(newsDate);
        List<String> temporaryLinks = new ArrayList<>(newsLink);

        newsTitle.clear();
        newsDate.clear();
        newsLink.clear();
        newsText.clear();
        articles.clear();

        index.forEach(i -> {
            newsTitle.add(temporaryTitles.get(i));
            newsDate.add(temporaryDates.get(i));
            newsLink.add(temporaryLinks.get(i));
        });
        System.out.println(newsLink);
        newsLink.forEach(this::parseNewsTextFromLink);

    }

    //Получает ссылки на новые статьи и заполняет коллекцию текстами статей
    private void parseNewsTextFromLink(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            Elements elements = document.body().getElementsByClass("news-item__content js-mediator-article");
            StringBuilder articleText = new StringBuilder();
            elements.forEach(element -> {
                List<Element> paragraphWithText = new ArrayList<>();
                Elements paragraph = element.select("p");
                paragraph.forEach(p -> {
                    if (p.hasText()) {
                        paragraphWithText.add(p);
                    }
                });
                List<String> text = paragraph.eachText();
                if (text.size() > 2) {
                    int textSizeWithoutLastThree = text.size() - 3;
                    for (int i = 0; i < textSizeWithoutLastThree; i++) {
                        articleText.append("\n").append(text.get(i)).append("\n");
                    }
                    for (int i = textSizeWithoutLastThree; i < text.size(); i++) {
                        Elements elementsWithStrong = paragraphWithText.get(i).select("strong");
                        if (elementsWithStrong.isEmpty()) {
                            articleText.append("\n").append(text.get(i)).append("\n");
                        }
                    }
                } else {
                    articleText.append("Гран-при");
                }
            });
            newsText.add(articleText.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Получает списки частей статьи и создает экземпляры конкретных статей
    private void changeToObject(List<String> date, List<String> title, List<String> link, List<String> text) {
        System.out.println("Change to object size: " + date.size() + " " + title.size() + " " + link.size() + " " + text.size());
        if (date.size() > 0) {
            for (String s : title) {
                if (!s.startsWith("Гран-при")) {
                    int i = title.indexOf(s);
                    articles.add(new Article(
                            date.get(i),
                            title.get(i),
                            link.get(i),
                            text.get(i)));
                }
            }
            Collections.reverse(articles);
        }
    }

    //Добавление статей в базу данных
    private void addArticlesToDb(List<Article> articles) {
        if (articles.size() != 0) {
            articles.forEach(article -> {
                if (!article.getText().startsWith("Sports.ru")) {
                    if (!article.getTitle().startsWith("Гран-при")) {
                        db.addArticles(article);
                    }
                }
            });
            System.out.println("Articles added: " + articles.size());
        } else {
            System.out.println("New articles is not found");
        }
    }

}
