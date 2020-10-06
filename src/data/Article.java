package data;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class Article {

    @Expose
    private final String date;
    @Expose
    private final String title;
    @Expose
    private final String link;
    @Expose
    private final String text;

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getText() {
        return text;
    }

    public Article(String date, String title, String link, String text) {
        this.date = date;
        this.title = title;
        this.link = link;
        this.text = text;
    }

    @Override
    public String toString() {
        return "Article{" +
                "date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return date.equals(article.date) &&
                title.equals(article.title) &&
                link.equals(article.link) &&
                text.equals(article.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, title, link, text);
    }

}
