package org.example.kurstrips.service;

import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class YandexMapServiceImpl implements MapService {
    private static final String API_KEY = "6c7d2a81-a74d-4900-bf75-abcecf1d2661";

    public String getApiKey(){
        return API_KEY;
    }

    @Override
    public void initializeMap(WebView webView) {
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
    }

    @Override
    public void buildRoute(WebView webView, String from, String to) {
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
                        console.log('Прямой маршрут построен');
                        
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
    }

    @Override
    public void addTestRoute(WebView webView) {
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
                    alert('Ошибка: ' + e.message);
                }
            })();
            """;

        webView.getEngine().executeScript(script);
    }

    @Override
    public void addTestMarker(WebView webView) {
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

    public void buildOptimalRoute(WebView webView, String from, String to) {
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
                    
                    // Геокодируем оба адреса параллельно
                    Promise.all([
                        ymaps.geocode('%s', { results: 1 }),
                        ymaps.geocode('%s', { results: 1 })
                    ]).then(function(results) {
                        var fromObj = results[0].geoObjects.get(0);
                        var toObj = results[1].geoObjects.get(0);
                        
                        if (!fromObj || !toObj) {
                            alert('Один из адресов не найден');
                            return;
                        }
                        
                        var fromCoords = fromObj.geometry.getCoordinates();
                        var toCoords = toObj.geometry.getCoordinates();
                        
                        // Находим ближайшие крупные города
                        ymaps.geocode(fromCoords, { kind: 'locality', results: 1 }).then(function(resFromCity) {
                            var fromCity = resFromCity.geoObjects.get(0);
                            ymaps.geocode(toCoords, { kind: 'locality', results: 1 }).then(function(resToCity) {
                                var toCity = resToCity.geoObjects.get(0);
                                
                                var routePoints = [fromCoords];
                                
                                // Если города разные, добавляем промежуточные точки
                                if (fromCity && toCity && 
                                    fromCity.getAddressLine() !== toCity.getAddressLine()) {
                                    routePoints.push(fromCity.geometry.getCoordinates());
                                    routePoints.push(toCity.geometry.getCoordinates());
                                }
                                
                                routePoints.push(toCoords);
                                
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
                                
                                // Добавляем информационные метки
                                if (fromCity) {
                                    window.map.geoObjects.add(new ymaps.Placemark(
                                        fromCity.geometry.getCoordinates(),
                                        { 
                                            hintContent: 'Ближайший город: ' + fromCity.getAddressLine(),
                                            balloonContent: 'Начальный пункт: ' + fromObj.getAddressLine() + 
                                                          '<br>Ближайший город: ' + fromCity.getAddressLine()
                                        }
                                    ));
                                }
                                
                                if (toCity) {
                                    window.map.geoObjects.add(new ymaps.Placemark(
                                        toCity.geometry.getCoordinates(),
                                        { 
                                            hintContent: 'Ближайший город: ' + toCity.getAddressLine(),
                                            balloonContent: 'Конечный пункт: ' + toObj.getAddressLine() + 
                                                          '<br>Ближайший город: ' + toCity.getAddressLine()
                                        }
                                    ));
                                }
                                
                                console.log('Оптимальный маршрут построен через ближайшие города');
                            });
                        });
                    }).catch(function(err) {
                        console.error('Ошибка:', err);
                        alert('Ошибка построения маршрута: ' + err.message);
                    });
                } catch (e) {
                    console.error('Общая ошибка:', e);
                    alert('Ошибка: ' + e.message);
                }
            })();
            """, from, to);

        webView.getEngine().executeScript(script);
    }

    @Override
    public void displayRouteWithCities(WebView webView, City start, City end, List<City> routeCities) {
        StringBuilder script = new StringBuilder("""
            (function() {
                try {
                    if (!window.map) return;
                    
                    // Удаляем старый маршрут
                    if (window.route) {
                        window.map.geoObjects.remove(window.route);
                    }
                    
                    // Строим точки маршрута
                    var routePoints = [
                        [%f, %f] // Начальная точка
            """.formatted(start.getLatitude(), start.getLongitude()));

        // Добавляем промежуточные города
        for (City city : routeCities) {
            script.append(",[%f, %f] // %s\n".formatted(
                    city.getLatitude(), city.getLongitude(), city.getName()));
        }

        script.append("""
                    ,[%f, %f] // Конечная точка
                    ];
                    
                    // Создаем маршрут
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
                    
                    // Добавляем метки для городов
            """.formatted(end.getLatitude(), end.getLongitude()));

        // Добавляем метки для каждого города
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
                    // Центрируем карту на маршруте
                    window.map.setBounds(window.route.getBounds());
                } catch (e) {
                    console.error('Ошибка:', e);
                }
            })();
            """);

        webView.getEngine().executeScript(script.toString());
    }

    @Override
    public City geocode(String address) throws Exception {
        String lowerAddress = address.toLowerCase();

        // Проверяем крупные города
        if (lowerAddress.contains("москва")) return new City("Москва", 55.755814, 37.617635);
        if (lowerAddress.contains("санкт-петербург") || lowerAddress.contains("петербург"))
            return new City("Санкт-Петербург", 59.938630, 30.314130);
        if (lowerAddress.contains("воронеж")) return new City("Воронеж", 51.660598, 39.200585);
        if (lowerAddress.contains("казань")) return new City("Казань", 55.796127, 49.106405);

        // Проверяем города поменьше
        if (lowerAddress.contains("подольск")) return new City("Подольск", 55.424190, 37.554720);
        if (lowerAddress.contains("балашиха")) return new City("Балашиха", 55.809450, 37.958060);
        if (lowerAddress.contains("химки")) return new City("Химки", 55.897190, 37.429690);
        if (lowerAddress.contains("королёв") || lowerAddress.contains("королев"))
            return new City("Королёв", 55.916670, 37.816670);
        if (lowerAddress.contains("мытищи")) return new City("Мытищи", 55.910000, 37.730000);

        // Для неизвестных адресов используем геокодирование через API
        try {
            String url = String.format(
                    "https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&geocode=%s",
                    API_KEY,
                    address.replace(" ", "+")
            );

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());

            // Парсим JSON ответа и извлекаем координаты
            // Это упрощенный пример - в реальном приложении нужен полноценный парсинг JSON
            if (response.body().contains("pos")) {
                String pos = response.body().split("pos")[1].split("\"")[2];
                String[] coords = pos.split(" ");
                double lon = Double.parseDouble(coords[0]);
                double lat = Double.parseDouble(coords[1]);
                return new City(address, lat, lon);
            }
        } catch (Exception e) {
            System.err.println("Ошибка геокодирования: " + e.getMessage());
        }

        throw new Exception("Адрес не распознан: " + address);
    }
}