package ru.geekbrains.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    public static Stage stage1;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/scene.fxml")));
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("April_Chat");
        stage1 = stage;
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
