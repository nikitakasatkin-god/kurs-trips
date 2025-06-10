package org.example.kurstrips;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.kurstrips.controller.MainController;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/kurstrips/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            primaryStage.setOnHidden(e -> {
                if (controller != null) {
                    controller.shutdown();
                }
            });

            primaryStage.setTitle("Дневник путешествий");
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Фатальная ошибка при запуске приложения:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        // Настройка обработчика непойманных исключений
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Непойманное исключение в потоке " + thread.getName());
            throwable.printStackTrace();
        });

        // Настройка файлового логгера
        try {
            FileHandler fileHandler = new FileHandler("travel_diary.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler); // Для всех логгеров
        } catch (IOException e) {
            System.err.println("Не удалось создать файл лога: " + e.getMessage());
        }

        launch(args);
    }
}