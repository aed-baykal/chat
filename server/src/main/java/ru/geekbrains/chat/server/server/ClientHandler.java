package ru.geekbrains.chat.server.server;

import ru.geekbrains.chat.server.auth.User;
import ru.geekbrains.chat.common.ChatMessage;
import ru.geekbrains.chat.common.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private Socket socket;
    private ChatServer chatServer;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String currentUsername;
    private String currentPassword;
    private boolean exist;
    private static final long TIME_OUT = 30000;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.exist = true;
            System.out.println("Client handler created!!!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        new Thread(() -> {
            try {
                authenticate();
                if (exist) readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void readMessages() throws IOException {
        try {
            while (!Thread.currentThread().isInterrupted() || socket.isConnected()) {
                String msg = inputStream.readUTF();
                ChatMessage message = ChatMessage.unmarshall(msg);
                ChatMessage responce = new ChatMessage();
                message.setFrom(this.currentUsername);
                switch (message.getMessageType()) {
                    case PUBLIC:
                        chatServer.sendBroadcastMessage(message);
                        break;
                    case PRIVATE:
                        chatServer.sendPrivateMessage(message);
                        break;
                    case CHANGE_USERNAME:
                        String newUserName = chatServer.getAuthService().changeUsername(this.getCurrentName(), message.getBody());
                        if (newUserName == null || newUserName.isEmpty()) {
                            responce.setMessageType(MessageType.ERROR);
                            responce.setBody("Somethings went wrong");
                        } else {
                            responce.setMessageType(MessageType.CHANGE_USERNAME_CONFIRM);
                            responce.setBody(newUserName);
                            chatServer.unsubscribe(this);
                            currentUsername = newUserName;
                            chatServer.subscribe(this);
                        }
                        sendMessage(responce);
                        break;
                    case CHANGE_PASSWORD:
                        String newPassword = chatServer.getAuthService().changePassword(this.getCurrentName(), this.getCurrentPassword(), message.getBody());
                        if (newPassword == null || newPassword.isEmpty()) {
                            responce.setMessageType(MessageType.ERROR);
                            responce.setBody("Somethings went wrong");
                        } else {
                            responce.setMessageType(MessageType.CHANGE_PASSWORD_CONFIRM);
                            responce.setBody(newPassword);
                        }
                        sendMessage(responce);
                        break;
                    case NEW_USER:
                        User user = chatServer.getAuthService().newUser(this.getCurrentName(), this.getCurrentPassword());
                        if (user == null) {
                            responce.setMessageType(MessageType.ERROR);
                            responce.setBody("Somethings went wrong");
                        } else {
                            responce.setMessageType(MessageType.NEW_USER_CONFIRM);
                            responce.setBody(user.getLogin());
                        }
                        sendMessage(responce);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeHandler();
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            outputStream.writeUTF(message.marshall());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentName() {
        return this.currentUsername;
    }

    public String getCurrentPassword() {
        return this.currentPassword;
    }

    private void authenticate() throws SocketException {
        System.out.println("Started client  auth...");
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                    closeHandler();
                    exist = false;
                System.out.println("Connection is closed!");
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, TIME_OUT);
        try {
            while (true) {
                String authMessage = inputStream.readUTF();
                System.out.println("Auth received");
                ChatMessage msg = ChatMessage.unmarshall(authMessage);
                String username = chatServer.getAuthService().getUsernameByLoginAndPassword(msg.getLogin(), msg.getPassword());
                ChatMessage response = new ChatMessage();
                if (username == null || username.equals("")) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Wrong username or password!");
                    System.out.println("Wrong credentials");
                } else if (chatServer.isUserOnline(username)) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Double auth!");
                    System.out.println("Double auth!");
                } else {
                    currentPassword = msg.getPassword();
                    response.setMessageType(MessageType.AUTH_CONFIRM);
                    response.setBody(username);
                    currentUsername = username;
                    chatServer.subscribe(this);
                    System.out.println("Subscribed");
                    timer.cancel();
                    sendMessage(response);
                    break;
                }
                sendMessage(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeHandler() {
        try {
            chatServer.unsubscribe(this);
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
