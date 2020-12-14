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
            Collections.reverse(articles);
            addArticlesToDb(articles);
            clearAllArrays();
            System.out.println(db.getArticlesFromDbForPage(1));
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
        List<String> articlesLinksFromDb = db.getLinksFromDb(newsLink.size() * 2);
        List<Integer> index = new ArrayList<>();
        if (articlesLinksFromDb.size() > 0) {
            newsLink.forEach(link -> {
                if (!articlesLinksFromDb.contains(link)) {
                    index.add(newsLink.indexOf(link));
                }
            });
        }
        return index;
    }

    //Создает объекты без текста
    private void alignmentAllArray(List<Integer> index) {
        for (Integer i : index) {
            articles.add(new Article(newsDate.get(i), newsTitle.get(i), newsLink.get(i), null));
        }
        articles.forEach(this::parseNewsTextFromLink);
    }

    //Получает ссылки на новые статьи, дополняет объекты текстами статей и фотографиями
    private void parseNewsTextFromLink(Article article) {
        StringBuilder articleText = new StringBuilder();
        List<String> images = new ArrayList<>();
        try {
            Document document = Jsoup.connect(article.getLink()).get();
            Elements elements = document.body().getElementsByClass("news-item__content js-mediator-article");
            elements.forEach(element -> {
                List<Element> paragraphWithText = new ArrayList<>();
                Elements paragraph = element.select("p");
                paragraph.forEach(p -> {
                    if (p.hasText()) {
                        paragraphWithText.add(p);
                    }
                    if (!p.select("img").attr("src").isEmpty()) {
                        images.add(p.select("img").attr("src"));
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
            article.setText(articleText.toString());
            if (!images.isEmpty()) {
                article.setImages(images);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Добавление статей в базу данных
    private void addArticlesToDb(List<Article> articles) {
        List<Article> addedArticles = new ArrayList<>();
        if (articles.size() != 0) {
            articles.forEach(article -> {
                if (!article.getText().startsWith("Sports.ru") && article.getText() != null) {
                    if (article.getImages() == null) {
                        db.addArticle(article);
                    } else {
                        db.addArticleWithImages(article);
                    }
                    addedArticles.add(article);
                }
            });
            System.out.println("Articles added: " + addedArticles.size());
        } else {
            System.out.println("New articles is not found");
        }
    }

    private void clearAllArrays() {
        newsTitle.clear();
        newsDate.clear();
        newsLink.clear();
        articles.clear();
    }

}
