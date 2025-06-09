package org.example.kurstrips.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Trip {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty fromCity = new SimpleStringProperty();
    private final StringProperty toCity = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final DoubleProperty budget = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<Review> review = new SimpleObjectProperty<>();

    public Trip(int id, String fromCity, String toCity, LocalDate startDate,
                LocalDate endDate, double budget) {
        this.id.set(id);
        this.fromCity.set(fromCity);
        this.toCity.set(toCity);
        this.startDate.set(startDate);
        this.endDate.set(endDate);
        this.budget.set(budget);
        updateStatus();
    }

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

    // Геттеры и свойства
    public int getId() { return id.get(); }
    public String getFromCity() { return fromCity.get(); }
    public String getToCity() { return toCity.get(); }
    public LocalDate getStartDate() { return startDate.get(); }
    public LocalDate getEndDate() { return endDate.get(); }
    public double getBudget() { return budget.get(); }
    public String getStatus() { return status.get(); }
    public Review getReview() { return review.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty fromCityProperty() { return fromCity; }
    public StringProperty toCityProperty() { return toCity; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public DoubleProperty budgetProperty() { return budget; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<Review> reviewProperty() { return review; }

    public void setReview(Review review) { this.review.set(review); }
    public void setId(int id) { this.id.set(id); }
}