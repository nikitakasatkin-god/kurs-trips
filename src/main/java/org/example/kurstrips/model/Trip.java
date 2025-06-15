/*
 * Класс, представляющий поездку в системе.
 * Содержит информацию о маршруте, датах, бюджете и статусе поездки.
 * Использует JavaFX свойства для интеграции с пользовательским интерфейсом.
 * Автоматически обновляет статус поездки на основе текущей даты.
 */
package org.example.kurstrips.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Trip {
    // JavaFX свойства для двустороннего связывания данных
    private final IntegerProperty id = new SimpleIntegerProperty();          // Уникальный идентификатор поездки
    private final StringProperty fromCity = new SimpleStringProperty();     // Город отправления
    private final StringProperty toCity = new SimpleStringProperty();       // Город назначения
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();  // Дата начала поездки
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();    // Дата окончания поездки
    private final DoubleProperty budget = new SimpleDoubleProperty();       // Бюджет поездки
    private final StringProperty status = new SimpleStringProperty();       // Текущий статус поездки
    private final ObjectProperty<Review> review = new SimpleObjectProperty<>(); // Связанный отзыв о поездке

    /*
     * Создает новый объект поездки.
     * @param id уникальный идентификатор поездки
     * @param fromCity город отправления
     * @param toCity город назначения
     * @param startDate дата начала поездки
     * @param endDate дата окончания поездки
     * @param budget бюджет поездки
     */
    public Trip(int id, String fromCity, String toCity, LocalDate startDate,
                LocalDate endDate, double budget) {
        this.id.set(id);
        this.fromCity.set(fromCity);
        this.toCity.set(toCity);
        this.startDate.set(startDate);
        this.endDate.set(endDate);
        this.budget.set(budget);
        updateStatus(); // Автоматически устанавливаем статус при создании
    }

    /*
     * Обновляет статус поездки на основе текущей даты.
     * Статус может быть: "Планируется", "Выполняется" или "Выполнена".
     */
    private void updateStatus() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(startDate.get())) {
            status.set("Планируется");
        } else if (now.isAfter(endDate.get())) {
            status.set("Выполнена");
        } else {
            status.set("Выполняется");
        }
    }

    // Базовые геттеры для получения значений свойств
    public int getId() { return id.get(); }
    public String getFromCity() { return fromCity.get(); }
    public String getToCity() { return toCity.get(); }
    public LocalDate getStartDate() { return startDate.get(); }
    public LocalDate getEndDate() { return endDate.get(); }
    public double getBudget() { return budget.get(); }
    public String getStatus() { return status.get(); }
    public Review getReview() { return review.get(); }

    // Методы для получения JavaFX свойств (используются для связывания с UI)
    public IntegerProperty idProperty() { return id; }
    public StringProperty fromCityProperty() { return fromCity; }
    public StringProperty toCityProperty() { return toCity; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public DoubleProperty budgetProperty() { return budget; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<Review> reviewProperty() { return review; }

    // Сеттеры для обновления значений
    public void setReview(Review review) { this.review.set(review); }
    public void setId(int id) { this.id.set(id); }
}