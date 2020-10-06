package database;

import data.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Db {

    private Connection connection;
    private Statement statement;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
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

    public void addToDb(Article article) {
        String query = String.format("INSERT INTO articles (date, title, link, text) VALUES ('%s','%s','%s','%s')",
                article.getDate(), article.getTitle(), article.getLink(), article.getText());
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<Article> getFromDb(int i) {
        List<Article> articles = new ArrayList<>();
        String query = String.format("SELECT date, title, link, text FROM articles ORDER BY id DESC LIMIT %d", i);
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                articles.add(new Article(
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

}
