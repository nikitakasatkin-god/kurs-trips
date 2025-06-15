/*
 * Класс, представляющий отзыв о поездке.
 * Использует JavaFX свойства для интеграции с графическим интерфейсом.
 * Позволяет хранить и управлять данными об оценках и комментариях к поездкам.
 */
package org.example.kurstrips.model;

import javafx.beans.property.*;

public class Review {
    // Свойства JavaFX для двустороннего связывания с UI
    private final IntegerProperty id = new SimpleIntegerProperty();         // ID отзыва
    private final IntegerProperty tripId = new SimpleIntegerProperty();    // ID связанной поездки
    private final IntegerProperty rating = new SimpleIntegerProperty();    // Оценка (например, от 1 до 5)
    private final StringProperty comment = new SimpleStringProperty();     // Текст комментария

    /*
     * Создает новый объект отзыва.
     * @param id уникальный идентификатор отзыва
     * @param tripId идентификатор связанной поездки
     * @param rating числовая оценка поездки
     * @param comment текстовый комментарий к поездке
     */
    public Review(int id, int tripId, int rating, String comment) {
        this.id.set(id);
        this.tripId.set(tripId);
        this.rating.set(rating);
        this.comment.set(comment);
    }

    /*
     * Возвращает ID отзыва.
     * @return числовой идентификатор отзыва
     */
    public int getId() { return id.get(); }

    /*
     * Возвращает ID связанной поездки.
     * @return числовой идентификатор поездки
     */
    public int getTripId() { return tripId.get(); }

    /*
     * Возвращает оценку поездки.
     * @return числовая оценка (обычно в диапазоне 1-5)
     */
    public int getRating() { return rating.get(); }

    /*
     * Возвращает текстовый комментарий.
     * @return строка с комментарием к поездке
     */
    public String getComment() { return comment.get(); }

    /*
     * Возвращает свойство ID для JavaFX связывания.
     * @return IntegerProperty объекта id
     */
    public IntegerProperty idProperty() { return id; }

    /*
     * Возвращает свойство ID поездки для JavaFX связывания.
     * @return IntegerProperty объекта tripId
     */
    public IntegerProperty tripIdProperty() { return tripId; }

    /*
     * Возвращает свойство оценки для JavaFX связывания.
     * @return IntegerProperty объекта rating
     */
    public IntegerProperty ratingProperty() { return rating; }

    /*
     * Возвращает свойство комментария для JavaFX связывания.
     * @return StringProperty объекта comment
     */
    public StringProperty commentProperty() { return comment; }

    /*
     * Устанавливает новый ID отзыва.
     * @param id новый числовой идентификатор
     */
    public void setId(int id) { this.id.set(id); }

    /*
     * Устанавливает новый ID связанной поездки.
     * @param tripId новый числовой идентификатор поездки
     */
    public void setTripId(int tripId) { this.tripId.set(tripId); }

    /*
     * Устанавливает новую оценку поездки.
     * @param rating новая числовая оценка
     */
    public void setRating(int rating) { this.rating.set(rating); }

    /*
     * Устанавливает новый текстовый комментарий.
     * @param comment новый текст комментария
     */
    public void setComment(String comment) { this.comment.set(comment); }
}