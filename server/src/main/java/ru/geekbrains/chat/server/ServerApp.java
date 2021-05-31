package ru.geekbrains.chat.server;

import ru.geekbrains.chat.server.server.ChatServer;

public class ServerApp {
    public static void main(String[] args) {
        new ChatServer().start();
    }
}

