package ru.geekbrains.chat.common;

public enum MessageType {
    PUBLIC,
    PRIVATE,
    CLIENT_LIST,
    SEND_AUTH,
    AUTH_CONFIRM,
    NEW_USER_CONFIRM,
    CHANGE_USERNAME,
    CHANGE_USERNAME_CONFIRM,
    CHANGE_PASSWORD,
    CHANGE_PASSWORD_CONFIRM,
    ERROR,
    NEW_USER;
}
