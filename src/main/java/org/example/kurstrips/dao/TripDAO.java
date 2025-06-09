package org.example.kurstrips.dao;

import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;
import java.util.List;

/**
 * Интерфейс для работы с поездками в базе данных
 */
public interface TripDAO {
    List<Trip> getAllTrips(); // Получить все поездки
    void addTrip(Trip trip); // Добавить новую поездку
    void updateTrip(Trip trip); // Обновить существующую поездку
    void deleteTrip(int id); // Удалить поездку по ID
    void addReview(int tripId, int rating, String comment); // Добавить отзыв к поездке
    Trip getTripById(int id); // Получить поездку по ID
}