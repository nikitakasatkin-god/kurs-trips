package org.example.kurstrips.service;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.kurstrips.util.LogUtil;

public class YandexMapServiceImpl implements MapService {
    private static final Logger logger = LogUtil.getLogger(YandexMapServiceImpl.class);
    private final String API_KEY;

    public YandexMapServiceImpl() {
        // Загружаем переменные окружения из .env файла
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Игнорировать если файла нет
                .load();

        this.API_KEY = dotenv.get("YANDEX_MAPS_API_KEY");

        if (this.API_KEY == null || this.API_KEY.isEmpty()) {
            logger.log(Level.SEVERE, "Yandex Maps API key not found in .env file");
            throw new IllegalStateException("Yandex Maps API key not configured in .env file");
        }

        logger.log(Level.INFO, "Yandex Maps API key loaded successfully");
    }

    public String getApiKey() {
        return API_KEY;
    }

    @Override
    public void initializeMap(WebView webView) {
        logger.log(Level.INFO, "Начало инициализации карты в WebView");
        try {
            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>Яндекс.Карты</title>\n" +
                    "    <script src=\"https://api-maps.yandex.ru/2.1/?apikey=" + API_KEY + "&lang=ru_RU&load=package.full\"></script>\n" +
                    "    <script>\n" +
                    "        var map;\n" +
                    "        var mapLoaded = false;\n" +
                    "        \n" +
                    "        function init() {\n" +
                    "            map = new ymaps.Map('map', {\n" +
                    "                center: [55.75, 37.62],\n" +
                    "                zoom: 10\n" +
                    "            });\n" +
                    "            \n" +
                    "            var placemark = new ymaps.Placemark([55.75, 37.62], {\n" +
                    "                hintContent: 'Тест',\n" +
                    "                balloonContent: 'Карта работает!'\n" +
                    "            });\n" +
                    "            map.geoObjects.add(placemark);\n" +
                    "            console.log('Карта инициализирована');\n" +
                    "            mapLoaded = true;\n" +
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

            webView.getEngine().loadContent(html);
            logger.log(Level.INFO, "Карта успешно загружена в WebView");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при инициализации карты", e);
            throw new RuntimeException("Не удалось инициализировать карту", e);
        }
    }

    @Override
    public void buildRoute(WebView webView, String from, String to) {
        logger.log(Level.INFO, "Начало построения маршрута от '{0}' до '{1}'", new Object[]{from, to});
        try {
            String script = String.format("""
                (function() {
                    try {
                        if (!window.map) {
                            console.error('Карта не инициализирована');
                            return;
                        }
                        
                        if (window.route) {
                            window.map.geoObjects.remove(window.route);
                        }
                        
                        ymaps.geocode('%s', { results: 1 }).then(function(res1) {
                            var fromObj = res1.geoObjects.get(0);
                            if (!fromObj) {
                                console.error('Адрес "%s" не найден');
                                return;
                            }
                            
                            ymaps.geocode('%s', { results: 1 }).then(function(res2) {
                                var toObj = res2.geoObjects.get(0);
                                if (!toObj) {
                                    console.error('Адрес "%s" не найден');
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
                                console.log('Прямой маршрут построен');
                                
                            }).catch(function(err) {
                                console.error('Ошибка геокодирования конечной точки:', err);
                            });
                            
                        }).catch(function(err) {
                            console.error('Ошибка геокодирования начальной точки:', err);
                        });
                    } catch (e) {
                        console.error('Общая ошибка:', e);
                    }
                })();
                """, from, from, to, to);

            webView.getEngine().executeScript(script);
            logger.log(Level.INFO, "Скрипт построения маршрута успешно выполнен");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении скрипта построения маршрута", e);
            throw new RuntimeException("Не удалось построить маршрут", e);
        }
    }

    @Override
    public void addTestRoute(WebView webView) {
        logger.log(Level.INFO, "Добавление тестового маршрута Москва-Санкт-Петербург");
        try {
            String script = """
                (function() {
                    try {
                        if (!window.map) {
                            console.error('Карта не инициализирована');
                            return;
                        }
                        
                        if (window.route) {
                            window.map.geoObjects.remove(window.route);
                        }
                        
                        window.route = new ymaps.multiRouter.MultiRoute({
                            referencePoints: [
                                [55.755814, 37.617635],
                                [59.938630, 30.314130]
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
                    }
                })();
                """;

            webView.getEngine().executeScript(script);
            logger.log(Level.INFO, "Тестовый маршрут успешно добавлен");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении тестового маршрута", e);
        }
    }

    @Override
    public void addTestMarker(WebView webView) {
        logger.log(Level.INFO, "Добавление тестовой метки на карту");
        try {
            webView.getEngine().executeScript("""
                if (window.map) {
                    var placemark = new ymaps.Placemark(
                        [55.75, 37.62], 
                        { hintContent: 'Тест', balloonContent: 'Работает!' }
                    );
                    map.geoObjects.add(placemark);
                    console.log('Метка добавлена');
                } else {
                    console.error('Карта не инициализирована');
                }
            """);
            logger.log(Level.INFO, "Тестовая метка успешно добавлена");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении тестовой метки", e);
        }
    }

    @Override
    public void displayRouteWithCities(WebView webView, City start, City end, List<City> routeCities) {
        logger.log(Level.INFO, "Отображение маршрута через {0} промежуточных городов", routeCities.size());
        try {
            StringBuilder script = new StringBuilder("""
                (function() {
                    try {
                        if (!window.map) return;
                        
                        if (window.route) {
                            window.map.geoObjects.remove(window.route);
                        }
                        
                        var routePoints = [
                            [%f, %f]
                """.formatted(start.getLatitude(), start.getLongitude()));

            for (City city : routeCities) {
                script.append(",[%f, %f] // %s\n".formatted(
                        city.getLatitude(), city.getLongitude(), city.getName()));
            }

            script.append("""
                        ,[%f, %f]
                        ];
                        
                        window.route = new ymaps.multiRouter.MultiRoute({
                            referencePoints: routePoints,
                            params: { routingMode: 'auto' }
                        }, {
                            boundsAutoApply: true,
                            routeStrokeWidth: 5,
                            routeStrokeColor: '0066ff',
                            viaPointVisible: true
                        });
                        
                        window.map.geoObjects.add(window.route);
                        
                """.formatted(end.getLatitude(), end.getLongitude()));

            for (City city : routeCities) {
                script.append("""
                    window.map.geoObjects.add(new ymaps.Placemark(
                        [%f, %f],
                        {
                            hintContent: '%s',
                            balloonContent: 'Промежуточный пункт: %s'
                        }
                    ));
                    """.formatted(city.getLatitude(), city.getLongitude(),
                        city.getName(), city.getName()));
            }

            script.append("""
                        window.map.setBounds(window.route.getBounds());
                    } catch (e) {
                        console.error('Ошибка:', e);
                    }
                })();
                """);

            webView.getEngine().executeScript(script.toString());
            logger.log(Level.INFO, "Маршрут с промежуточными городами успешно отображен");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при отображении маршрута с городами", e);
        }
    }

    @Override
    public City geocode(String address) throws Exception {
        logger.log(Level.INFO, "Начало геокодирования адреса: {0}", address);
        String lowerAddress = address.toLowerCase();

        if (lowerAddress.contains("москва")) {
            logger.log(Level.FINE, "Адрес распознан как Москва");
            return new City("Москва", 55.755814, 37.617635);
        }
        if (lowerAddress.contains("санкт-петербург") || lowerAddress.contains("петербург")) {
            logger.log(Level.FINE, "Адрес распознан как Санкт-Петербург");
            return new City("Санкт-Петербург", 59.938630, 30.314130);
        }
        if (lowerAddress.contains("воронеж")) {
            logger.log(Level.FINE, "Адрес распознан как Воронеж");
            return new City("Воронеж", 51.660598, 39.200585);
        }
        if (lowerAddress.contains("казань")) {
            logger.log(Level.FINE, "Адрес распознан как Казань");
            return new City("Казань", 55.796127, 49.106405);
        }
        if (lowerAddress.contains("подольск")) {
            logger.log(Level.FINE, "Адрес распознан как Подольск");
            return new City("Подольск", 55.424190, 37.554720);
        }
        if (lowerAddress.contains("балашиха")) {
            logger.log(Level.FINE, "Адрес распознан как Балашиха");
            return new City("Балашиха", 55.809450, 37.958060);
        }
        if (lowerAddress.contains("химки")) {
            logger.log(Level.FINE, "Адрес распознан как Химки");
            return new City("Химки", 55.897190, 37.429690);
        }
        if (lowerAddress.contains("королёв") || lowerAddress.contains("королев")) {
            logger.log(Level.FINE, "Адрес распознан как Королёв");
            return new City("Королёв", 55.916670, 37.816670);
        }
        if (lowerAddress.contains("мытищи")) {
            logger.log(Level.FINE, "Адрес распознан как Мытищи");
            return new City("Мытищи", 55.910000, 37.730000);
        }

        try {
            String url = String.format(
                    "https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&geocode=%s",
                    API_KEY,
                    address.replace(" ", "+")
            );

            logger.log(Level.FINE, "Выполнение запроса к API геокодирования: {0}", url);
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());

            if (response.body().contains("pos")) {
                String pos = response.body().split("pos")[1].split("\"")[2];
                String[] coords = pos.split(" ");
                double lon = Double.parseDouble(coords[0]);
                double lat = Double.parseDouble(coords[1]);
                logger.log(Level.INFO, "Координаты найдены: широта={0}, долгота={1}", new Object[]{lat, lon});
                return new City(address, lat, lon);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при геокодировании адреса", e);
            throw new Exception("Не удалось геокодировать адрес: " + address, e);
        }

        logger.log(Level.WARNING, "Адрес не распознан: {0}", address);
        throw new Exception("Адрес не распознан: " + address);
    }
}