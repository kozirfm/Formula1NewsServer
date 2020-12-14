package database;

import data.Article;
import data.Driver;
import data.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Db {

    private Connection connection;
    private Statement statement;


    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:Formula1News.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addArticle(Article article) {
        String query = String.format("INSERT INTO articles (date, title, link, text) VALUES ('%s','%s','%s','%s')",
                article.getDate(), article.getTitle(), article.getLink(), article.getText());
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addArticleWithImages(Article article) {
        try {
            String addNewArticleQuery = String.format("INSERT INTO articles (date, title, link, text, images) VALUES ('%s','%s','%s','%s', true)",
                    article.getDate(), article.getTitle(), article.getLink(), article.getText());
            String getArticleIdQuery = String.format("SELECT * FROM articles WHERE title = '%s'", article.getTitle());
            statement.executeUpdate(addNewArticleQuery);
            ResultSet articleIdResultSet = statement.executeQuery(getArticleIdQuery);
            while (articleIdResultSet.next()) {
                long i = articleIdResultSet.getLong("id");
                article.getImages().forEach(image -> {
                    try {
                        //Добавляет ссылки на картинки в БД
                        statement.executeUpdate(String.format("INSERT INTO article_images (articles_id, image_link) VALUES ('%d', '%s')", i, image));
                    } catch (SQLException t) {
                        t.printStackTrace();
                    }
                });
            }
            articleIdResultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Article> getArticlesFromDbForPage(int page) {
        List<Article> articles = new ArrayList<>();
        int pageSize = 20;
        long lastIndex = 0;
        String queryLastIndex = "SELECT id FROM articles ORDER BY id DESC LIMIT 1";
        try {
            ResultSet resultSet = statement.executeQuery(queryLastIndex);
            while (resultSet.next()) {
                lastIndex = resultSet.getLong("id");
            }
            resultSet.close();
        } catch (SQLException t) {
            t.printStackTrace();
        }
        if (lastIndex != 0) {
            int startPosition = (int) (lastIndex - ((page * pageSize) - 1));
            int finishPosition = (int) (lastIndex - ((page - 1) * pageSize));
            String queryArticles = String.format("SELECT * FROM articles WHERE id BETWEEN %d AND %d", startPosition, finishPosition);
            try {
                ResultSet resultSet = statement.executeQuery(queryArticles);
                while (resultSet.next()) {
                    articles.add(new Article(
                            resultSet.getLong("id"),
                            resultSet.getString("date"),
                            resultSet.getString("title"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            resultSet.getBoolean("images")));
                }
                resultSet.close();
            } catch (SQLException t) {
                t.printStackTrace();
            }
        }
        getArticlesFromDbWithImages(articles);
        Collections.reverse(articles);
        return articles;
    }

    public List<Article> getArticlesFromDb(int count) {
        List<Article> articles = new ArrayList<>();
        String query = String.format("SELECT id, date, title, link, text FROM articles ORDER BY id DESC LIMIT %d", count);
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                articles.add(new Article(
                        resultSet.getLong("id"),
                        resultSet.getString("date"),
                        resultSet.getString("title"),
                        resultSet.getString("link"),
                        resultSet.getString("text")));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    public void getArticlesFromDbWithImages(List<Article> articles) {
        List<String> images = new ArrayList<>();
            articles.forEach(article -> {
                if (article.hasImage()){
                    try {
                        ResultSet articleImagesResultSet = statement.executeQuery(String.format("SELECT * FROM article_images WHERE article_id = '%d'", article.getId()));
                        while (articleImagesResultSet.next()) {
                            images.add(articleImagesResultSet.getString("image_link"));
                        }
                        article.setImages(new ArrayList<>(images));
                        images.clear();
                        articleImagesResultSet.close();
                    } catch (SQLException t) {
                        t.printStackTrace();
                    }
                }
            });
    }

    public List<String> getLinksFromDb(int count) {
        List<String> links = new ArrayList<>();
        String query = String.format("SELECT link FROM articles ORDER BY id DESC LIMIT %d", count);
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                links.add(resultSet.getString("link"));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return links;
    }

    public void addNewUserToDb(User user) {
        String query = String.format("INSERT INTO users (username, password, email) VALUES ('%s','%s','%s')",
                user.getUsername(), user.getPassword(), user.getEmail());
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean isContainsUsernameOrEmail(String username, String email) {
        String query = String.format("SELECT * FROM users WHERE username = '%s' OR email = '%s'", username, email);
        try {
            ResultSet result = statement.executeQuery(query);
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateDriversPositionTable(List<Driver> drivers) {
        drivers.forEach(driver -> {
            try {
                String query = String.format("INSERT or REPLACE INTO drivers VALUES ('%d', '%s', '%s', '%s', '%d')",
                        driver.getPosition(), driver.getName(), driver.getSurname(), driver.getTeam(), driver.getPoints());
                statement.executeUpdate(query);
            } catch (SQLException t) {
                t.printStackTrace();
            }
        });
    }

    public List<Driver> getDriversChampionshipTable() {
        List<Driver> drivers = new ArrayList<>();
        try {
            String query = "SELECT * FROM drivers";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                drivers.add(new Driver(resultSet.getInt("position"),
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getString("team"),
                        resultSet.getInt("points")));
            }
        } catch (SQLException t) {
            t.printStackTrace();
        }
        return drivers;
    }
}
