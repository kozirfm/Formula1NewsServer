package main;

import data.Article;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
                    if (!article.getTitle().startsWith("Гран-при")) {
                        db.addToDb(article);
                    }
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
            searchSameArticlesInParsedArrays();
            alignmentAllArray(compareLinksArrayToLinksDb());

            changeToObject(newsDate, newsTitle, newsLink, newsText);
        } catch (IOException e) {
            e.printStackTrace();
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

    //Ищет одинаковые статьи в полученных с сайта данных и удаляет их
    private void searchSameArticlesInParsedArrays() {
        List<String> links = new ArrayList<>();
        newsLink.forEach(link -> {
            if (!links.contains(link)) {
                links.add(link);
            } else {
                int i = links.size();
                newsTitle.remove(i);
                newsDate.remove(i);
                newsLink.remove(i);
            }
        });
    }

    //Сравнивает полученные с сайта статьи со списком статей из базы данных и возвращает номера новых статей в списках
    private List<Integer> compareLinksArrayToLinksDb() {
        List<String> articlesLinksFromDb = db.getLinkFromDb(newsLink.size());
        List<Integer> index = new ArrayList<>();
        newsLink.forEach(link -> {
            if (!articlesLinksFromDb.contains(link)) {
                index.add(newsLink.indexOf(link));
                parseNewsTextFromLink(link);
            }
        });

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

        index.forEach(i -> {
            newsTitle.add(temporaryTitles.get(i));
            newsDate.add(temporaryDates.get(i));
            newsLink.add(temporaryLinks.get(i));
        });
    }

    //Получает ссылки на новые статьи и заполняет коллекцию текстами статей
    private void parseNewsTextFromLink(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            Elements elements = document.body().getElementsByClass("news-item__content js-mediator-article");
            StringBuilder articleText = new StringBuilder();
            elements.forEach(element -> {
                List<String> text = element.select("p").eachText();
                Elements paragraph = element.select("p");
                int paragraphSizeWithoutLastThree = paragraph.size() - 3;
                for (int i = 0; i < paragraphSizeWithoutLastThree; i++) {
                    articleText.append("\n").append(text.get(i)).append("\n");
                }
                for (int i = paragraphSizeWithoutLastThree; i < paragraph.size(); i++) {
                    Element firstElement = paragraph.get(i).children().first();
                    if (firstElement != null) {
                        if (!firstElement.is("strong")) {
                            articleText.append("\n").append(text.get(i)).append("\n");
                        }
                    }
                }
                newsText.add(articleText.toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Получает списки частей статьи и создает экземпляры конкретных статей
    private void changeToObject(List<String> date, List<String> title, List<String> link, List<String> text) {
        for (int i = 0; i < text.size(); i++) {
            articles.add(new Article(
                    date.get(i),
                    title.get(i),
                    link.get(i),
                    text.get(i)));
        }
        Collections.reverse(articles);
    }

}
