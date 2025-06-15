/*
 * Класс, представляющий географический город с координатами.
 * Содержит методы для работы с географическими координатами
 * и расчета расстояний между городами.
 */
package org.example.kurstrips.model;

public class City {
    private final String name;       // Название города
    private final double latitude;   // Географическая широта
    private final double longitude;  // Географическая долгота

    /*
     * Создает новый объект города.
     * @param name название города
     * @param latitude широта в градусах
     * @param longitude долгота в градусах
     */
    public City(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /*
     * Возвращает название города.
     * @return название города
     */
    public String getName() { return name; }

    /*
     * Возвращает географическую широту города.
     * @return широта в градусах
     */
    public double getLatitude() { return latitude; }

    /*
     * Возвращает географическую долготу города.
     * @return долгота в градусах
     */
    public double getLongitude() { return longitude; }

    /*
     * Вычисляет расстояние между этим городом и другим городом
     * по формуле гаверсинусов (Haversine formula).
     * @param other другой город для расчета расстояния
     * @return расстояние между городами в километрах
     */
    public double distanceTo(City other) {
        final int R = 6371; // Радиус Земли в км
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDistance/2) * Math.sin(latDistance/2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance/2) * Math.sin(lonDistance/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}