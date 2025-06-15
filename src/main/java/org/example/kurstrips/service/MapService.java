/*
 * Интерфейс сервиса для работы с картографическим API.
 * Определяет контракт для взаимодействия с картографическими сервисами.
 * Поддерживает отображение маршрутов, геокодирование и базовые операции с картой.
 */
package org.example.kurstrips.service;

import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;
import java.util.List;

public interface MapService {
    /*
     * Инициализирует карту в указанном WebView компоненте.
     * @param webView компонент JavaFX WebView для отображения карты
     */
    void initializeMap(WebView webView);

    /*
     * Строит маршрут между двумя адресами.
     * @param webView компонент для отображения карты
     * @param from начальный адрес маршрута
     * @param to конечный адрес маршрута
     */
    void buildRoute(WebView webView, String from, String to);

    /*
     * Добавляет тестовый маршрут (Москва - Санкт-Петербург) для демонстрации.
     * @param webView компонент для отображения карты
     */
    void addTestRoute(WebView webView);

    /*
     * Добавляет тестовую метку на карту.
     * @param webView компонент для отображения карты
     */
    void addTestMarker(WebView webView);

    /*
     * Отображает маршрут через указанные города.
     * @param webView компонент для отображения карты
     * @param start начальная точка маршрута
     * @param end конечная точка маршрута
     * @param routeCities список промежуточных городов
     */
    void displayRouteWithCities(WebView webView, City start, City end, List<City> routeCities);

    /*
     * Преобразует адрес в географические координаты (геокодирование).
     * @param address адрес для геокодирования
     * @return объект City с координатами
     * @throws Exception если адрес не может быть геокодирован
     */
    City geocode(String address) throws Exception;
}