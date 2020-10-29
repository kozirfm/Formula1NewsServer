package data;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class Article {

    public Article(String date, String title, String link, String text) {
        this.date = date;
        this.title = title;
        this.link = link;
        this.text = text;
    }

    public Article(long id, String date, String title, String link, String text) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.link = link;
        this.text = text;
    }

    @Expose
    private long id;
    @Expose
    private String date;
    @Expose
    private String title;
    @Expose
    private String link;
    @Expose
    private String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", date='" + date + '\'' +
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
        return id == article.id &&
                Objects.equals(date, article.date) &&
                Objects.equals(title, article.title) &&
                Objects.equals(link, article.link) &&
                Objects.equals(text, article.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, title, link, text);
    }
}
