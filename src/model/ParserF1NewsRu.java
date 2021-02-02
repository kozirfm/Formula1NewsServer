package model;

import data.News;
import database.Db;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParserF1NewsRu implements Constants {

    private final List<News> news = new ArrayList<>();

    public ParserF1NewsRu(Db db) {
        this.db = db;
    }

    private final Db db;

    public void parse() {
        try {
            System.out.println("Start session: " + new Date().toString());
            Document mainPage = Jsoup.connect(BASE_URL_F1NEWS_NEWS).get();
            Elements firstPartElements = mainPage.getElementsByClass("b-home__list");
            Elements elementsWithTitlesAndLinks = firstPartElements.first().getElementsByClass("b-news-list__title");
            List<String> titles = parseToTitlesList(elementsWithTitlesAndLinks).stream().map(title -> {
                if (title.startsWith("Видео:")) title = title.substring(7);
                return title;
            }).collect(Collectors.toList());
            List<String> links = parseToLinksList(elementsWithTitlesAndLinks);
            List<Integer> indexes = compareLinksArrayToLinksDb(links);
            if (!titles.isEmpty() && !links.isEmpty() && !indexes.isEmpty()) {
                createNewArticles(indexes, titles, links).forEach(this::parseNewsTextFromLink);
                Collections.reverse(news);
            }
            addArticlesToDb(news);
            System.out.println("End session: " + new Date().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            news.clear();
        }
    }

    private List<String> parseToLinksList(Elements elementsWithLinks) {
        return elementsWithLinks.eachAttr("href")
                .stream().map(link -> BASE_URL_F1NEWS_NEWS + link)
                .collect(Collectors.toList());
    }

    private List<String> parseToTitlesList(Elements elementsWithTitles) {
        return elementsWithTitles.eachText();
    }

    //Сравнивает полученные с сайта статьи со списком статей из базы данных, после чего возвращает список номеров новых статей
    private List<Integer> compareLinksArrayToLinksDb(List<String> links) {
        List<String> articlesLinksFromDb = db.getLinksFromDb(links.size() * 2);
        List<Integer> index = new ArrayList<>();
        if (articlesLinksFromDb.size() > 0) {
            links.forEach(link -> {
                if (!articlesLinksFromDb.contains(link)) {
                    index.add(links.indexOf(link));
                }
            });
        }
        return index;
    }

    //Создает новые статьи
    private List<News> createNewArticles(List<Integer> index, List<String> titles, List<String> links) {
        for (Integer i : index) {
            news.add(new News(null, titles.get(i), links.get(i), null));
        }
        return news;
    }

    private void parseNewsTextFromLink(News news) {
        try {
            Document linkPage = Jsoup.connect(news.getLink()).get();
            String[] date = linkPage.getElementsByAttributeValue("itemprop", "datePublished").text().split(",");
            news.setDate(String.format("%s %s", date[0].substring(0, date[0].length() - 4).trim(), date[1].trim()));
            String image = linkPage.getElementsByAttributeValue("itemprop", "contentUrl url").attr("src");
            if (!image.isEmpty()) {
                news.setImages(Collections.singletonList(image));
            }
            Elements articleBody = linkPage.getElementsByAttributeValue("itemprop", "articleBody");
            articleBody.forEach(paragraphs -> {
                paragraphs.getElementsByTag("blockquote").remove();
                StringBuilder fullTextWithBreakLines = new StringBuilder();
                paragraphs.select("p").forEach(paragraph -> {
                    if (paragraph.hasText()) {
                        fullTextWithBreakLines.append("\n").append(paragraph.text()).append("\n");
                    }
                });
                news.setText(fullTextWithBreakLines.toString());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addArticlesToDb(List<News> news) {
        System.out.println("Articles added: " + db.addArticles(news));
    }

}
