package ru.geekbrains.chat.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static final Logger LOGGER_CLIENT = LogManager.getLogger();

    public static void main(String[] args) {
        LOGGER_CLIENT.info("Info");
        App.main(args);
    }
}