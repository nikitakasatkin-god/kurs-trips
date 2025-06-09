package org.example.kurstrips.dao;

import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;
import java.util.List;
import java.sql.SQLException;

public interface TripDAO {
    List<Trip> getAllTrips();
    void addTrip(Trip trip);
    void updateTrip(Trip trip);
    void deleteTrip(int id);
    void addReview(int tripId, int rating, String comment);
    void updateReview(int reviewId, int rating, String comment) throws SQLException;
    Trip getTripById(int id);
}