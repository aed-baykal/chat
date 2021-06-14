package ru.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import ru.geekbrains.chat.client.network.ChatMessageService;
import ru.geekbrains.chat.client.network.ChatMessageServiceImpl;
import ru.geekbrains.chat.client.network.MessageProcessor;
import ru.geekbrains.chat.common.ChatMessage;
import ru.geekbrains.chat.common.MessageType;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainChatController implements Initializable, MessageProcessor {

    private static final String PUBLIC = "PUBLIC";
    public TextArea chatArea;
    public ListView onlineUsers;
    public TextField inputField;
    public Button btnSendMessage;
    public TextField loginField;
    public PasswordField passwordField;
    public Button btnSendAuth;
//    public MenuItem btnLogIn;
    public MenuItem btnChangeLogIn;
    public MenuItem btnChangePass;
    public MenuItem btnNewUser;
    public Pane loginPane;
    public Pane changeLogin;
    public Pane changePass;
    public Pane newUser;
    public Pane chatPane;
    public TextField changeUserNameField;
    public PasswordField changeUserNamePassField;
    public Button btnNewLogIn;
    public Button btmChangePassword;
    public PasswordField changePassOldPassField;
    public PasswordField changePassnewPassField;
    public PasswordField changePassConfirmPassField;
    public TextField newUserNameField;
    public Button buttonNewUser;
    public PasswordField passNewUserField;
    private ChatMessageService messageService;
    private String currentName;
    private HistoryMaker historyMaker;



    public void mockAction(ActionEvent actionEvent) {

    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void showAbout(ActionEvent actionEvent) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/about.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("About");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        String text = inputField.getText();
        if (text.isEmpty()) return;
        ChatMessage msg = new ChatMessage();
        String adressee = (String) this.onlineUsers.getSelectionModel().getSelectedItem();
        if (adressee.equals(PUBLIC)) msg.setMessageType(MessageType.PUBLIC);
        else {
            msg.setMessageType(MessageType.PRIVATE);
            msg.setTo(adressee);
        }
        msg.setFrom(currentName);
        msg.setBody(text);
        messageService.send(msg.marshall());
        chatArea.appendText(String.format("[ME] %s\n", text));
        historyMaker.writeHistory(String.format("[ME] %s\n", text));
        inputField.clear();
    }

    private void appendTextOfChat(ChatMessage msg) {
        if (msg.getFrom().equals(this.currentName)) return;
        String modifier = msg.getMessageType().equals(MessageType.PUBLIC) ? "[pub]" : "[priv]";
        String text = String.format("[%s] %s %s\n", msg.getFrom(), modifier, msg.getBody());
        chatArea.appendText(text);
        historyMaker.writeHistory(text);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.messageService = new ChatMessageServiceImpl("localhost", 12256, this);
        messageService.connect();
    }

    @Override
    public void processMessage(String msg) {
        Platform.runLater(() -> {
            ChatMessage message = ChatMessage.unmarshall(msg);
            Main.LOGGER_CLIENT.log(Level.valueOf("Info"), "From MainChatController - Received message");
            System.out.println("Received message");

            switch (message.getMessageType()) {
                case PRIVATE, PUBLIC -> appendTextOfChat(message);
                case CLIENT_LIST -> refreshOnlineUsers(message);
                case AUTH_CONFIRM -> {
                    this.currentName = message.getBody();
                    App.stage1.setTitle(currentName);
                    loginPane.setVisible(false);
                    chatPane.setVisible(true);
                    this.historyMaker = new HistoryMaker(message.getBody());
                    List<String> history = historyMaker.readHistory();
                    for (String s : history) {
                        chatArea.appendText(s + System.lineSeparator());
                    }
                }
                case CHANGE_USERNAME_CONFIRM -> {
                    changeLogin.setVisible(false);
                    chatPane.setVisible(true);
                    currentName = message.getBody();
                    App.stage1.setTitle(currentName);
                }
                case CHANGE_PASSWORD_CONFIRM -> {
                    changePass.setVisible(false);
                    chatPane.setVisible(true);
                }
                case NEW_USER_CONFIRM -> {
                    newUser.setVisible(false);
                    chatPane.setVisible(true);
                    currentName = message.getBody();
                    App.stage1.setTitle(currentName);
                }
                case ERROR -> showError(message);
            }
        });
    }

    private void refreshOnlineUsers(ChatMessage message) {
        message.getOnlineUsers().add(0, PUBLIC);
        this.onlineUsers.setItems(FXCollections.observableArrayList(message.getOnlineUsers()));
        this.onlineUsers.getSelectionModel().selectFirst();
    }

    public void sendAuth(ActionEvent actionEvent) {

        String log = loginField.getText();
        String pass = passwordField.getText();
        if (log.isEmpty() || pass.isEmpty()) return;
        ChatMessage msg = new ChatMessage();
        msg.setMessageType(MessageType.SEND_AUTH);
        msg.setLogin(log);
        msg.setPassword(pass);
        messageService.send(msg.marshall());
        loginPane.setVisible(false);
        chatPane.setVisible(true);
    }

    public void beginChangeLogIn(ActionEvent actionEvent) {
        chatPane.setVisible(false);
        changeLogin.setVisible(true);
    }

    public void beginChangePass(ActionEvent actionEvent) {
        chatPane.setVisible(false);
        changePass.setVisible(true);
    }

    public void newUser(ActionEvent actionEvent) {
        if (newUserNameField.getText().isEmpty() || passNewUserField.getText().isEmpty()) return;
        ChatMessage message = new ChatMessage();
        message.setMessageType(MessageType.NEW_USER);
        message.setBody(newUserNameField.getText());
        message.setPassword(passNewUserField.getText());
        messageService.send(message.marshall());
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong!");
        alert.setHeaderText(e.getMessage());

        VBox dialog = new VBox();
        Label label = new Label("Trace:");
        TextArea textArea = new TextArea();

        StringBuilder builder = new StringBuilder();
        for (StackTraceElement el : e.getStackTrace()) {
            builder.append(el).append(System.lineSeparator());
        }
        textArea.setText(builder.toString());
        dialog.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialog);
        alert.showAndWait();
    }

    private void showError(ChatMessage msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Main.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From MainChatController - Something went wrong!");
        alert.setTitle("Something went wrong!");
        alert.setHeaderText(msg.getMessageType().toString());
        Main.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From MainChatController - " + msg.getMessageType().toString());
        VBox dialog = new VBox();
        Label label = new Label("Error:");
        TextArea textArea = new TextArea();
        textArea.setText(msg.getBody());
        Main.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From MainChatController - " + msg.getBody());
        dialog.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialog);
        alert.showAndWait();
    }

    public void setNewLogin(ActionEvent actionEvent) {
        if (changeUserNameField.getText().isEmpty() || changeUserNamePassField.getText().isEmpty()) return;
        ChatMessage message = new ChatMessage();
        message.setMessageType(MessageType.CHANGE_USERNAME);
        message.setBody(changeUserNameField.getText());
        message.setFrom(this.currentName);
        message.setPassword(changeUserNamePassField.getText());
        messageService.send(message.marshall());
    }

    public void changePassword(ActionEvent actionEvent) {
        String oldPass = changePassOldPassField.getText();
        String newPass = changePassnewPassField.getText();
        String newPassConfirm = changePassConfirmPassField.getText();

        if (newPass.equals(newPassConfirm)) {
            ChatMessage message = new ChatMessage();
            message.setMessageType(MessageType.CHANGE_PASSWORD);
            message.setBody(newPass);
            message.setFrom(currentName);
            messageService.send(message.marshall());
        } else {
            changePassOldPassField.clear();
            changePassnewPassField.clear();
            changePassConfirmPassField.clear();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            Main.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From MainChatController - Changing your password is failed!");
            alert.setTitle("Changing your password is failed!");
            alert.setContentText("Entered passwords are not equal.");
            alert.showAndWait();
        }
    }


    public void beginNewUser(ActionEvent actionEvent) {
        chatPane.setVisible(false);
        newUser.setVisible(true);
    }
}

