package org.example.kurstrips.model;

import javafx.beans.property.*;

public class Review {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty tripId = new SimpleIntegerProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty();
    private final StringProperty comment = new SimpleStringProperty();

    public Review(int id, int tripId, int rating, String comment) {
        this.id.set(id);
        this.tripId.set(tripId);
        this.rating.set(rating);
        this.comment.set(comment);
    }

    // Геттеры
    public int getId() { return id.get(); }
    public int getTripId() { return tripId.get(); }
    public int getRating() { return rating.get(); }
    public String getComment() { return comment.get(); }

    // JavaFX свойства
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty tripIdProperty() { return tripId; }
    public IntegerProperty ratingProperty() { return rating; }
    public StringProperty commentProperty() { return comment; }

    // Сеттеры
    public void setId(int id) { this.id.set(id); }
    public void setTripId(int tripId) { this.tripId.set(tripId); }
    public void setRating(int rating) { this.rating.set(rating); }
    public void setComment(String comment) { this.comment.set(comment); }
}