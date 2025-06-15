/*
 * Сервис для работы с городами и построения маршрутов.
 * Содержит методы для поиска промежуточных городов и построения оптимальных маршрутов.
 * Использует предопределенные списки городов России с их координатами.
 */
package org.example.kurstrips.service;

import org.example.kurstrips.model.City;

import java.util.*;

public class CityService {
    /*
     * Список крупных и средних городов России с координатами.
     * Включает города всех регионов страны, отсортированные по значимости.
     */
    private static final List<City> MAJOR_CITIES = Arrays.asList(
            // Крупные города
            new City("Москва", 55.755814, 37.617635),
            new City("Санкт-Петербург", 59.938630, 30.314130),
            new City("Новосибирск", 55.008353, 82.935732),
            new City("Екатеринбург", 56.838011, 60.597465),
            new City("Казань", 55.796127, 49.106405),
            new City("Нижний Новгород", 56.296505, 43.936058),
            new City("Челябинск", 55.164442, 61.436843),
            new City("Самара", 53.195533, 50.101801),
            new City("Омск", 54.989342, 73.368212),
            new City("Ростов-на-Дону", 47.235713, 39.701505),

            // Города поменьше
            new City("Воронеж", 51.660598, 39.200585),
            new City("Краснодар", 45.035470, 38.975313),
            new City("Уфа", 54.735152, 55.958736),
            new City("Пермь", 58.010455, 56.229434),
            new City("Волгоград", 48.707103, 44.516939),
            new City("Красноярск", 56.009657, 92.852416),
            new City("Саратов", 51.533557, 46.034257),
            new City("Тюмень", 57.153033, 65.534328),
            new City("Тольятти", 53.507836, 49.420393),
            new City("Ижевск", 56.852744, 53.211396),
            new City("Барнаул", 53.356132, 83.749619),
            new City("Ульяновск", 54.314192, 48.403132),
            new City("Иркутск", 52.286387, 104.280660),
            new City("Хабаровск", 48.480223, 135.071917),
            new City("Ярославль", 57.626569, 39.893787),
            new City("Владивосток", 43.115536, 131.885485),
            new City("Махачкала", 42.984913, 47.504646),
            new City("Томск", 56.495116, 84.972128),
            new City("Оренбург", 51.768205, 55.096955),
            new City("Кемерово", 55.359594, 86.087781),

            // Малые города
            new City("Подольск", 55.424190, 37.554720),
            new City("Балашиха", 55.809450, 37.958060),
            new City("Химки", 55.897190, 37.429690),
            new City("Королёв", 55.916670, 37.816670),
            new City("Мытищи", 55.910000, 37.730000),
            new City("Люберцы", 55.677830, 37.893220),
            new City("Электросталь", 55.789590, 38.446710),
            new City("Коломна", 55.079440, 38.778330),
            new City("Одинцово", 55.677980, 37.277730),
            new City("Домодедово", 55.436390, 37.766110),
            new City("Серпухов", 54.915780, 37.411140),
            new City("Щёлково", 55.927780, 37.972220),
            new City("Раменское", 55.568530, 38.230280),
            new City("Долгопрудный", 55.904110, 37.560410),
            new City("Жуковский", 55.595280, 38.120280),
            new City("Реутов", 55.761110, 37.857500),
            new City("Клин", 56.333330, 36.733330),
            new City("Сергиев Посад", 56.300000, 38.133330),
            new City("Орехово-Зуево", 55.806720, 38.961780),
            new City("Ногинск", 55.866670, 38.433330)
    );

    /*
     * Полный список городов для построения маршрутов.
     * Включает как крупные, так и небольшие города.
     */
    private static final List<City> ALL_CITIES = Arrays.asList(
            // Крупные города
            new City("Москва", 55.755814, 37.617635),
            new City("Санкт-Петербург", 59.938630, 30.314130),
            new City("Новгород", 58.522900, 31.269800),
            new City("Тверь", 56.858700, 35.917600),
            new City("Великие Луки", 56.339400, 30.545500),
            new City("Псков", 57.819400, 28.331900),
            new City("Торжок", 57.041300, 34.960100),
            new City("Вышний Волочек", 57.591300, 34.564500),
            new City("Бологое", 57.885700, 34.053800),
            new City("Осташков", 57.152800, 33.111400)
    );

    /*
     * Находит промежуточные города между начальной и конечной точкой маршрута.
     * @param start начальная точка маршрута
     * @param end конечная точка маршрута
     * @return список промежуточных городов, отсортированный по удалению от начальной точки
     */
    public List<City> findIntermediateCities(City start, City end) {
        List<City> route = new ArrayList<>();

        // Находим все города, которые находятся между начальной и конечной точкой
        for (City city : ALL_CITIES) {
            if (isBetween(start, end, city) && !city.equals(start) && !city.equals(end)) {
                route.add(city);
            }
        }

        // Сортируем города по расстоянию от начальной точки
        route.sort(Comparator.comparingDouble(c -> start.distanceTo(c)));

        return route;
    }

    /*
     * Проверяет, находится ли город в коридоре между двумя точками маршрута.
     * Использует формулу треугольного неравенства для определения положения города.
     * @param start начальная точка маршрута
     * @param end конечная точка маршрута
     * @param checkCity проверяемый город
     * @return true если город находится между точками маршрута
     */
    private boolean isBetween(City start, City end, City checkCity) {
        // Проверяем, что город находится в "коридоре" между началом и концом
        double totalDistance = start.distanceTo(end);
        double distanceToStart = start.distanceTo(checkCity);
        double distanceToEnd = end.distanceTo(checkCity);

        // Город считается промежуточным, если он не слишком далеко от линии маршрута
        return distanceToStart + distanceToEnd <= totalDistance * 1.3;
    }

    /*
     * Находит оптимальные города для маршрута между двумя точками.
     * @param start начальная точка
     * @param end конечная точка
     * @return список городов, через которые проходит маршрут
     */
    public List<City> findRouteCities(City start, City end) {
        List<City> routeCities = new ArrayList<>();

        // Находим ближайший город к началу
        City nearestStart = findNearestCity(start);
        // Находим ближайший город к концу
        City nearestEnd = findNearestCity(end);

        // Если начальная точка уже город из списка, не добавляем его снова
        if (!isMajorCity(start)) {
            routeCities.add(nearestStart);
        }

        // Если конечная точка уже город из списка, не добавляем его снова
        if (!isMajorCity(end) && !nearestStart.equals(nearestEnd)) {
            routeCities.add(nearestEnd);
        }

        return routeCities;
    }

    /*
     * Находит ближайший крупный город к заданной точке.
     * @param target точка для поиска
     * @return ближайший город из списка MAJOR_CITIES
     */
    public City findNearestCity(City target) {
        return MAJOR_CITIES.stream()
                .min(Comparator.comparingDouble(c -> c.distanceTo(target)))
                .orElse(MAJOR_CITIES.get(0));
    }

    /*
     * Проверяет, является ли город крупным (находится в списке MAJOR_CITIES).
     * @param city проверяемый город
     * @return true если город есть в списке крупных городов
     */
    private boolean isMajorCity(City city) {
        return MAJOR_CITIES.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(city.getName()));
    }
}