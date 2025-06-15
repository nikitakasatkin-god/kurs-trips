/*
 * Главный класс приложения "Дневник путешествий"
 * Наследуется от javafx.application.Application и является точкой входа в приложение
 * Отвечает за:
 * - Инициализацию графического интерфейса
 * - Настройку системы логирования
 * - Обработку неперехваченных исключений
 */
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
    /*
     * Основной метод запуска JavaFX приложения
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Загрузка FXML файла с описанием интерфейса
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/kurstrips/main.fxml"));
            Parent root = loader.load();

            // Получение контроллера и настройка обработчика закрытия окна
            MainController controller = loader.getController();
            primaryStage.setOnHidden(e -> {
                if (controller != null) {
                    controller.shutdown();
                }
            });

            // Настройка и отображение основного окна
            primaryStage.setTitle("Дневник путешествий");
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Фатальная ошибка при запуске приложения:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Точка входа в приложение
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        /*
         * Настройка обработчика неперехваченных исключений
         * Логирует все исключения, которые не были перехвачены в других потоках
         */
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Неперехваченное исключение в потоке " + thread.getName());
            throwable.printStackTrace();
        });

        /*
         * Настройка файлового логгера для записи логов в файл
         * Файл будет создан в корневой директории проекта с именем travel_diary.log
         * Режим добавления (append) установлен в true для сохранения старых записей
         */
        try {
            FileHandler fileHandler = new FileHandler("travel_diary.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler); // Для всех логгеров
        } catch (IOException e) {
            System.err.println("Не удалось создать файл лога: " + e.getMessage());
        }

        // Запуск JavaFX приложения
        launch(args);
    }
}