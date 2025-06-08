package org.example.kurstrips;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Исправленный путь к FXML файлу
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/kurstrips/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Яндекс Карты - Оптимальный маршрут");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}