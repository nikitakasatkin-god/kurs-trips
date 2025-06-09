package org.example.kurstrips.dao.impl;

import org.example.kurstrips.dao.TripDAO;
import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SQLiteTripDAO implements TripDAO {
    private Connection conn;

    public SQLiteTripDAO() {
        try {
            Class.forName("org.sqlite.JDBC");
            // Указываем явный путь к файлу БД
            String dbPath = System.getProperty("user.dir") + "/trips.db";
            System.out.println("Database path: " + dbPath);
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            if (conn != null) {
                System.out.println("Connected to database");
                initializeDatabase();
            }
        } catch (Exception e) {
            System.err.println("Error connecting to database:");
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        String createTripsTable = """
            CREATE TABLE IF NOT EXISTS trips (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                from_city TEXT NOT NULL,
                to_city TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                budget REAL NOT NULL,
                status TEXT NOT NULL)""";

        String createReviewsTable = """
            CREATE TABLE IF NOT EXISTS reviews (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trip_id INTEGER NOT NULL,
                rating INTEGER NOT NULL,
                comment TEXT,
                FOREIGN KEY(trip_id) REFERENCES trips(id))""";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTripsTable);
            stmt.execute(createReviewsTable);
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц:");
            e.printStackTrace();
        }
    }

    @Override
    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        if (conn == null) return trips;

        String sql = """
        SELECT t.id, t.from_city, t.to_city, t.start_date, t.end_date, t.budget, t.status,
               r.id as review_id, r.trip_id as review_trip_id, r.rating, r.comment 
        FROM trips t LEFT JOIN reviews r ON t.id = r.trip_id
        ORDER BY t.start_date DESC""";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip trip = mapResultSetToTrip(rs);

                if (rs.getInt("review_id") != 0) {
                    trip.setReview(new Review(
                            rs.getInt("review_id"),
                            rs.getInt("review_trip_id"),  // Используем правильное имя столбца
                            rs.getInt("rating"),
                            rs.getString("comment")
                    ));
                }
                trips.add(trip);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении поездок:");
            e.printStackTrace();
        }
        return trips;
    }

    @Override
    public void addTrip(Trip trip) {
        String sql = "INSERT INTO trips (from_city, to_city, start_date, end_date, budget, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, trip.getFromCity());
            pstmt.setString(2, trip.getToCity());
            pstmt.setString(3, trip.getStartDate().toString());
            pstmt.setString(4, trip.getEndDate().toString());
            pstmt.setDouble(5, trip.getBudget());
            pstmt.setString(6, trip.getStatus());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    trip.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении поездки:");
            e.printStackTrace();
        }
    }

    @Override
    public void updateTrip(Trip trip) {
        String sql = "UPDATE trips SET from_city = ?, to_city = ?, start_date = ?, " +
                "end_date = ?, budget = ?, status = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trip.getFromCity());
            pstmt.setString(2, trip.getToCity());
            pstmt.setString(3, trip.getStartDate().toString());
            pstmt.setString(4, trip.getEndDate().toString());
            pstmt.setDouble(5, trip.getBudget());
            pstmt.setString(6, trip.getStatus());
            pstmt.setInt(7, trip.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении поездки:");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTrip(int id) {
        // Сначала удаляем связанные отзывы
        String deleteReviewsSql = "DELETE FROM reviews WHERE trip_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteReviewsSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении отзывов:");
            e.printStackTrace();
            return;
        }

        // Затем удаляем саму поездку
        String deleteTripSql = "DELETE FROM trips WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteTripSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении поездки:");
            e.printStackTrace();
        }
    }

    @Override
    public void addReview(int tripId, int rating, String comment) {
        String sql = "INSERT INTO reviews (trip_id, rating, comment) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении отзыва:");
            e.printStackTrace();
        }
    }

    @Override
    public void updateReview(int reviewId, int rating, String comment) throws SQLException {
        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating);
            pstmt.setString(2, comment);
            pstmt.setInt(3, reviewId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public Trip getTripById(int id) {
        String sql = """
        SELECT t.id, t.from_city, t.to_city, t.start_date, t.end_date, t.budget, t.status,
               r.id as review_id, r.trip_id as review_trip_id, r.rating, r.comment 
        FROM trips t LEFT JOIN reviews r ON t.id = r.trip_id 
        WHERE t.id = ?""";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Trip trip = mapResultSetToTrip(rs);

                    if (rs.getInt("review_id") != 0) {
                        trip.setReview(new Review(
                                rs.getInt("review_id"),
                                rs.getInt("review_trip_id"),
                                rs.getInt("rating"),
                                rs.getString("comment")
                        ));
                    }
                    return trip;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении поездки по ID:");
            e.printStackTrace();
        }
        return null;
    }

    private Trip mapResultSetToTrip(ResultSet rs) throws SQLException {
        Trip trip = new Trip(
                rs.getInt("id"),
                rs.getString("from_city"),
                rs.getString("to_city"),
                LocalDate.parse(rs.getString("start_date")),
                LocalDate.parse(rs.getString("end_date")),
                rs.getDouble("budget")
        );
        return trip;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Соединение с SQLite закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии соединения:");
            e.printStackTrace();
        }
    }
}