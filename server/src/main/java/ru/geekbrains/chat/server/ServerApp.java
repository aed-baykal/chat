package ru.geekbrains.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat.server.server.ChatServer;

public class ServerApp {

    public static final Logger LOGGER_SERVER = LogManager.getLogger();

    public static void main(String[] args) {
        LOGGER_SERVER.info("Info");
        new ChatServer().start();
    }
}