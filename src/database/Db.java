package database;

import data.Article;
import data.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Db {

    private Connection connection;
    private Statement statement;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:Formula1News.db");
            statement = connection.createStatement();
            System.out.println("DataBase connect");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
            System.out.println("DataBase disconnect");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addArticleToDb(Article article) {
        String query = String.format("INSERT INTO articles (date, title, link, text) VALUES ('%s','%s','%s','%s')",
                article.getDate(), article.getTitle(), article.getLink(), article.getText());
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

}
