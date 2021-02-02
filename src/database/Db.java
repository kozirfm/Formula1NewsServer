package database;

import data.News;
import data.Driver;
import data.GrandPrix;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Db {

    private Connection connection;
    private Statement statement;
    PreparedStatement addArticleStatement;
    PreparedStatement addArticleWithImageStatement;
    PreparedStatement getArticleWithImageStatement;
    PreparedStatement addImageStatement;
    PreparedStatement updateCalendarStatement;


    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:Formula1News.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

//    public void disconnect() {
//        try {
//            statement.close();
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private void clearTable(String table) {
        try {
            statement.execute(String.format("DELETE FROM %s", table));
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void prepareStatementForAddArticles() throws SQLException {
        addArticleStatement = connection.prepareStatement("INSERT INTO articles (date, title, link, text) VALUES (?, ?, ?, ?)");
        addArticleWithImageStatement = connection.prepareStatement("INSERT INTO articles (date, title, link, text, images) VALUES (?, ?, ?, ?, true)");
        getArticleWithImageStatement = connection.prepareStatement("SELECT * FROM articles WHERE title = ?");
        addImageStatement = connection.prepareStatement("INSERT INTO article_images (article_id, image_link) VALUES (?, ?)");
    }

    private void prepareStatementForUpdateCalendar() throws SQLException {
        updateCalendarStatement = connection.prepareStatement("INSERT INTO calendar " +
                "(date, flag, grandPrix, track, length, laps, distance, driver, team) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }

    public int addArticles(List<News> news) {
        List<News> addedNews = new ArrayList<>();
        try {
            prepareStatementForAddArticles();
            if (news.size() > 0) {
                news.forEach(article -> {
                    if (!article.getText().startsWith("Sports.ru") && article.getText() != null) {
                        if (article.getImages() == null) {
                            addArticle(article, addArticleStatement);
                        } else {
                            addArticleWithImage(article, addArticleWithImageStatement);
                        }
                        addedNews.add(article);
                    }
                });
                addArticleStatement.executeBatch();
                addArticleWithImageStatement.executeBatch();
                news.forEach(article -> {
                    if (article.getImages() != null) {
                        addImages(article, getArticleWithImageStatement, addImageStatement);
                    }
                });

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addedNews.size();
    }

    private void addArticle(News news, PreparedStatement addArticleStatement) {
        try {
            addArticleStatement.setString(1, news.getDate());
            addArticleStatement.setString(2, news.getTitle());
            addArticleStatement.setString(3, news.getLink());
            addArticleStatement.setString(4, news.getText());
            addArticleStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addArticleWithImage(News news, PreparedStatement addArticleWithImageStatement) {
        addArticle(news, addArticleWithImageStatement);
    }

    private void addImages(News news, PreparedStatement getArticleWithImageStatement, PreparedStatement addImageStatement) {
        try {
            getArticleWithImageStatement.setString(1, news.getTitle());
            ResultSet articleIdResultSet = getArticleWithImageStatement.getResultSet();
            getArticleWithImageStatement.executeQuery();
            while (articleIdResultSet.next()) {
                long id = articleIdResultSet.getLong("id");
                news.getImages().forEach(image -> {
                    try {
                        //Добавляет ссылки на картинки в БД
                        addImageStatement.setLong(1, id);
                        addImageStatement.setString(2, image);
                        addImageStatement.addBatch();
                    } catch (SQLException t) {
                        t.printStackTrace();
                    }
                });
            }
            addImageStatement.executeBatch();
            articleIdResultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<News> getArticlesFromDbForPage(int page) {
        List<News> news = new ArrayList<>();
        int pageSize = 20;
        long lastIndex = 0;
        String queryLastIndex = "SELECT id FROM articles ORDER BY id DESC LIMIT 1";
        try {
            ResultSet resultSet = statement.executeQuery(queryLastIndex);
            while (resultSet.next()) {
                lastIndex = resultSet.getLong("id");
            }
            statement.close();
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
                    news.add(new News(
                            resultSet.getLong("id"),
                            resultSet.getString("date"),
                            resultSet.getString("title"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            resultSet.getBoolean("images")));
                }
                statement.close();
                resultSet.close();
            } catch (SQLException t) {
                t.printStackTrace();
            }
        }
        getArticlesFromDbWithImages(news);
        Collections.reverse(news);
        return news;
    }

    public List<News> getArticlesFromDb(int count) {
        List<News> news = new ArrayList<>();
        String query = String.format("SELECT id, date, title, link, text FROM articles ORDER BY id DESC LIMIT %d", count);
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                news.add(new News(
                        resultSet.getLong("id"),
                        resultSet.getString("date"),
                        resultSet.getString("title"),
                        resultSet.getString("link"),
                        resultSet.getString("text")));
            }
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return news;
    }

    public void getArticlesFromDbWithImages(List<News> news) {
        List<String> images = new ArrayList<>();
        news.forEach(article -> {
            if (article.hasImage()) {
                try {
                    ResultSet articleImagesResultSet = statement.executeQuery(String.format("SELECT * FROM article_images WHERE article_id = '%d'", article.getId()));
                    while (articleImagesResultSet.next()) {
                        images.add(articleImagesResultSet.getString("image_link"));
                    }
                    article.setImages(new ArrayList<>(images));
                    images.clear();
                    statement.close();
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
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return links;
    }

//    public void addNewUserToDb(User user) {
//        String query = String.format("INSERT INTO users (username, password, email) VALUES ('%s','%s','%s')",
//                user.getUsername(), user.getPassword(), user.getEmail());
//        try {
//            statement.executeUpdate(query);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Boolean isContainsUsernameOrEmail(String username, String email) {
//        String query = String.format("SELECT * FROM users WHERE username = '%s' OR email = '%s'", username, email);
//        try {
//            ResultSet result = statement.executeQuery(query);
//            if (result.next()) {
//                return true;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public void updateDriversPositionTable(List<Driver> drivers) {
        clearTable("drivers");
        drivers.forEach(driver -> {
            try {
                String query = String.format("INSERT or REPLACE INTO drivers VALUES ('%d', '%s', '%s', '%s', '%d')",
                        driver.getPosition(), driver.getName(), driver.getSurname(), driver.getTeam(), driver.getPoints());
                statement.executeUpdate(query);
                statement.close();
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
            statement.close();
            resultSet.close();
        } catch (SQLException t) {
            t.printStackTrace();
        }
        return drivers;
    }

    public void updateCalendarTable(List<GrandPrix> grandPrixes) {
        clearTable("calendar");
        try {
            prepareStatementForUpdateCalendar();
            grandPrixes.forEach(grandPrix -> {
                try {
                    updateCalendarStatement.setString(1, grandPrix.getDate());
                    updateCalendarStatement.setString(2, grandPrix.getFlag());
                    updateCalendarStatement.setString(3, grandPrix.getGrandPrix());
                    updateCalendarStatement.setString(4, grandPrix.getTrack());
                    updateCalendarStatement.setString(5, grandPrix.getLength());
                    updateCalendarStatement.setString(6, grandPrix.getLaps());
                    updateCalendarStatement.setString(7, grandPrix.getDistance());
                    updateCalendarStatement.setString(8, grandPrix.getDriver());
                    updateCalendarStatement.setString(9, grandPrix.getTeam());
                    updateCalendarStatement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            updateCalendarStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<GrandPrix> getCalendarTable() {
        List<GrandPrix> grandPrixes = new ArrayList<>();
        try {
            String query = "SELECT * FROM calendar";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                grandPrixes.add(new GrandPrix(
                        resultSet.getString("date"),
                        resultSet.getString("flag"),
                        resultSet.getString("grandPrix"),
                        resultSet.getString("track"),
                        resultSet.getString("length"),
                        resultSet.getString("laps"),
                        resultSet.getString("distance"),
                        resultSet.getString("driver"),
                        resultSet.getString("team")));
            }
            statement.close();
            resultSet.close();
        } catch (SQLException t) {
            t.printStackTrace();
        }
        return grandPrixes;
    }
}
