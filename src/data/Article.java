package data;

import com.google.gson.annotations.Expose;

import java.util.List;
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

    public Article(long id, String date, String title, String link, String text, Boolean image) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.link = link;
        this.text = text;
        this.image = image;
    }

    @Expose
    private long id;
    @Expose
    private final String date;
    @Expose
    private final String title;
    @Expose
    private final String link;
    @Expose
    private String text;
    private Boolean image;
    @Expose
    private List<String> images;

    public long getId() {
        return id;
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

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Boolean hasImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", text='" + text + '\'' +
                ", images=" + images +
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
