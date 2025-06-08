package org.example.kurstrips;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.TimerTask;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class HelloController {
    private final String API_KEY = "6c7d2a81-a74d-4900-bf75-abcecf1d2661";

    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Button buildRouteBtn;
    @FXML private WebView webView;

    @FXML
    public void initialize() {
        if (!checkAPIKey()) {
            return;
        }

        enableWebViewConsole();
        initMap();

        // Добавляем задержку перед первым использованием
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    System.out.println("Проверка загрузки карты...");
                    webView.getEngine().executeScript("console.log('Проверка: карта ' + (window.map ? 'загружена' : 'не загружена'))");
                });
            }
        }, 3000); // 3 секунды задержки

        buildRouteBtn.setOnAction(event -> {
            String from = startField.getText().trim();
            String to = endField.getText().trim();

            if (from.isEmpty() || to.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Введите оба адреса").show();
                return;
            }

            buildRoute(from, to);
        });
    }

    private void enableWebViewConsole() {
        webView.getEngine().setOnAlert(event ->
                System.out.println("WebView Alert: " + event.getData()));

        webView.getEngine().getLoadWorker().exceptionProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        System.err.println("WebView Error: " + newVal.getMessage());
                        Platform.runLater(() ->
                                new Alert(Alert.AlertType.ERROR, "WebView Error: " + newVal.getMessage()).show());
                    }
                });

        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        System.out.println("WebView content loaded");
                    }
                });
    }

    private void initMap() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>Яндекс.Карты</title>\n" +
                "    <script src=\"https://api-maps.yandex.ru/2.1/?apikey=" + API_KEY + "&lang=ru_RU&load=package.full\"></script>\n" +
                "    <script>\n" +
                "        var map;\n" +
                "        var mapLoaded = false;\n" +  // Добавляем флаг загрузки
                "        \n" +
                "        function init() {\n" +
                "            map = new ymaps.Map('map', {\n" +
                "                center: [55.75, 37.62],\n" +
                "                zoom: 10\n" +
                "            });\n" +
                "            \n" +
                "            // Тестовая метка\n" +
                "            var placemark = new ymaps.Placemark([55.75, 37.62], {\n" +
                "                hintContent: 'Тест',\n" +
                "                balloonContent: 'Карта работает!'\n" +
                "            });\n" +
                "            map.geoObjects.add(placemark);\n" +
                "            console.log('Карта инициализирована');\n" +
                "            mapLoaded = true;\n" +  // Устанавливаем флаг
                "        }\n" +
                "        \n" +
                "        ymaps.ready(init);\n" +
                "    </script>\n" +
                "    <style>\n" +
                "        body, html { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
                "        #map { width: 100%; height: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "</body>\n" +
                "</html>";

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("WebView content loaded successfully");
            }
        });

        webView.getEngine().loadContent(html);
    }

    private boolean checkAPIKey() {
        try {
            String testUrl = "https://geocode-maps.yandex.ru/1.x/?apikey=" + API_KEY + "&format=json&geocode=Москва";
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                                    .uri(URI.create(testUrl))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());

            if (response.body().contains("Invalid Key")) {
                Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Неверный API-ключ Яндекс.Карт").show());
                return false;
            }
            return true;
        } catch (Exception e) {
            Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, "Ошибка проверки API: " + e.getMessage()).show());
            return false;
        }
    }

    private void buildRoute(String from, String to) {
        try {
            // Проверяем, загружена ли карта
            Object result = webView.getEngine().executeScript("window.mapLoaded");
            if (result == null || !(Boolean)result) {
                new Alert(Alert.AlertType.WARNING, "Карта еще не загружена. Пожалуйста, подождите.").show();
                return;
            }

            String script = String.format("""
        (function() {
            try {
                if (!window.map) {
                    alert('Карта не инициализирована');
                    return;
                }
                
                if (window.route) {
                    window.map.geoObjects.remove(window.route);
                }
                
                ymaps.geocode('%s', { results: 1 }).then(function(res1) {
                    var fromObj = res1.geoObjects.get(0);
                    if (!fromObj) {
                        alert('Адрес "%s" не найден');
                        return;
                    }
                    
                    ymaps.geocode('%s', { results: 1 }).then(function(res2) {
                        var toObj = res2.geoObjects.get(0);
                        if (!toObj) {
                            alert('Адрес "%s" не найден');
                            return;
                        }
                        
                        window.route = new ymaps.multiRouter.MultiRoute({
                            referencePoints: [
                                fromObj.geometry.getCoordinates(),
                                toObj.geometry.getCoordinates()
                            ],
                            params: { routingMode: 'auto' }
                        }, {
                            boundsAutoApply: true,
                            routeStrokeWidth: 5,
                            routeStrokeColor: '0066ff'
                        });
                        
                        window.map.geoObjects.add(window.route);
                        console.log('Маршрут построен');
                        
                    }).catch(function(err) {
                        console.error('Ошибка геокодирования конечной точки:', err);
                        alert('Ошибка: ' + err.message);
                    });
                    
                }).catch(function(err) {
                    console.error('Ошибка геокодирования начальной точки:', err);
                    alert('Ошибка: ' + err.message);
                });
            } catch (e) {
                console.error('Общая ошибка:', e);
                alert('Ошибка: ' + e.message);
            }
        })();
        """, from, from, to, to);

            webView.getEngine().executeScript(script);
        } catch (Exception e) {
            System.err.println("Ошибка выполнения скрипта:");
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка при построении маршрута: " + e.getMessage()).show();
        }
    }

    @FXML
    private void testButtonClicked() {
        try {
            String script = """
            (function() {
                try {
                    if (!window.map) {
                        alert('Карта не инициализирована');
                        return;
                    }
                    
                    if (window.route) {
                        window.map.geoObjects.remove(window.route);
                    }
                    
                    window.route = new ymaps.multiRouter.MultiRoute({
                        referencePoints: [
                            [55.755814, 37.617635], // Москва
                            [59.938630, 30.314130]  // СПб
                        ],
                        params: { routingMode: 'auto' }
                    }, {
                        boundsAutoApply: true,
                        routeStrokeWidth: 5,
                        routeStrokeColor: 'ff0000'
                    });
                    
                    window.map.geoObjects.add(window.route);
                    console.log('Тестовый маршрут построен');
                } catch (e) {
                    console.error('Ошибка:', e);
                    alert('Ошибка: ' + e.message);
                }
            })();
            """;

            webView.getEngine().executeScript(script);
        } catch (Exception e) {
            System.err.println("Ошибка выполнения тестового скрипта:");
            e.printStackTrace();
        }
    }
    @FXML
    private void testMap() {
        webView.getEngine().executeScript("""
        if (window.map) {
            var placemark = new ymaps.Placemark(
                [55.75, 37.62], 
                { hintContent: 'Тест', balloonContent: 'Работает!' }
            );
            map.geoObjects.add(placemark);
            alert('Метка добавлена');
        } else {
            alert('Карта не инициализирована');
        }
    """);
    }
}