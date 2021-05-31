package ru.geekbrains.chat.server.auth;

import ru.geekbrains.chat.server.data_base.ClientsDatabaseService;

public class DatabaseAuthService implements AuthService{
    private ClientsDatabaseService dbService;

    @Override
    public void start() {
        dbService = ClientsDatabaseService.getInstance();
    }

    @Override
    public void stop() {
        dbService.closeConnection();
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        return dbService.getUsernameByLoginAndPassword(login, password);
    }

    @Override
    public String changeUsername(String oldName, String newName) {
        return dbService.changeUsername(oldName, newName);
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        return dbService.changePassword(username, oldPassword, newPassword);
    }

    @Override
    public User newUser(String UserLogin, String UserPassword) {
        return dbService.newUser(UserLogin, UserPassword);
    }
}
