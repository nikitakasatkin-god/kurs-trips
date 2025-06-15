/*
 * Интерфейс для работы с данными о поездках (Trip) и отзывах (Review).
 * Определяет контракт для DAO-классов, работающих с хранилищем данных.
 * Все методы должны быть реализованы в конкретных реализациях DAO.
 */
package org.example.kurstrips.dao;

import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;

import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

public interface TripDAO {
    /*
     * Получает список поездок в указанном диапазоне дат.
     * @param startDate начальная дата диапазона (может быть null)
     * @param endDate конечная дата диапазона (может быть null)
     * @return список поездок, удовлетворяющих условиям фильтрации
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    List<Trip> getTripsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException;

    /*
     * Получает все поездки из хранилища данных.
     * @return список всех поездок, отсортированный по дате начала (новые сначала)
     */
    List<Trip> getAllTrips();

    /*
     * Добавляет новую поездку в хранилище данных.
     * @param trip объект поездки для добавления
     */
    void addTrip(Trip trip);

    /*
     * Обновляет существующую поездку в хранилище данных.
     * @param trip объект поездки с обновленными данными
     */
    void updateTrip(Trip trip);

    /*
     * Удаляет поездку по идентификатору.
     * @param id идентификатор поездки для удаления
     */
    void deleteTrip(int id);

    /*
     * Добавляет отзыв для указанной поездки.
     * @param tripId идентификатор поездки
     * @param rating оценка поездки (обычно от 1 до 5)
     * @param comment текстовый комментарий к поездке
     */
    void addReview(int tripId, int rating, String comment);

    /*
     * Обновляет существующий отзыв.
     * @param reviewId идентификатор отзыва
     * @param rating новая оценка
     * @param comment новый комментарий
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    void updateReview(int reviewId, int rating, String comment) throws SQLException;

    /*
     * Получает поездку по идентификатору.
     * @param id идентификатор поездки
     * @return объект поездки или null, если поездка не найдена
     */
    Trip getTripById(int id);
}