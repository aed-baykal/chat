package ru.geekbrains.chat.server.data_base;

import ru.geekbrains.chat.server.auth.AuthService;
import ru.geekbrains.chat.server.auth.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientsDatabaseService implements AuthService {

    private static ClientsDatabaseService instance;
    private static final String CONNECTION = "jdbc:sqlite:/home/andrey/IdeaProjects/chat/server/src/main/resources/chat_users.db";
    private static Connection connection;
    private final String GET_USERNAME = "select userid from users where userlogin = ? and userpassword = ?;";
    private final String CHANGE_USERNAME = "update users set userlogin = ? where userlogin = ?;";
    private final String CHANGE_PASSWORD = "update users set userpassword = ? where userpassword = ? and userlogin = ?;";
    private final String ADD_NEW_USER = "INSERT OR IGNORE INTO users (UserLogin, userpassword) VALUES (UserLogin = ?, userpassword = ?);";
    private List<User> users = new ArrayList<>();
    private Statement statement;

    public ClientsDatabaseService() {

        try {
            this.statement = connect();
            instance = this;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        dbRead();

//        disconnect();
    }

    public static ClientsDatabaseService getInstance() {
        if (instance != null) return instance;
        instance = new ClientsDatabaseService();
        return instance;
    }

    static Statement connect() throws SQLException {
        connection = DriverManager.getConnection(CONNECTION);
        return connection.createStatement();
    }

//    private void dbRead() {
//        try (ResultSet rs = statement.executeQuery("select UserID, UserLogin, UserPassword from users;")){
//            while (rs.next()) {
//                this.users.add(new User(("User" + rs.getString(1)), rs.getString(2), rs.getString(3)));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private void disconnect() {
        try {
            if(statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        System.out.println("Auth service started");
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(GET_USERNAME)) {
            ps.setString(1, login);
            ps.setString(2, password);
            System.out.println(login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.close();
                return login;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";

    }

    @Override
    public String changeUsername(String oldName, String newName) {
        try(PreparedStatement ps = connection.prepareStatement(CHANGE_USERNAME)){
            ps.setString(2, oldName);
            ps.setString(1, newName);
            if (ps.executeUpdate() > 0) return newName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oldName;
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        try(PreparedStatement ps = connection.prepareStatement(CHANGE_PASSWORD)){
            ps.setString(1, newPassword);
            ps.setString(2,oldPassword);
            ps.setString(3, username);
            if (ps.executeUpdate() > 0) return newPassword;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oldPassword;
    }

    public User newUser(String newUserName, String newPassword) {
        try(PreparedStatement ps = connection.prepareStatement(ADD_NEW_USER)){
            ps.setString(1, newUserName);
            ps.setString(2, newPassword);
            if (ps.executeUpdate() > 0) {
                return new User(newUserName, newPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection() {

    }
}
