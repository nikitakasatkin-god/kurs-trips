/*
 * Реализация TripDAO для работы с SQLite базой данных.
 * Обеспечивает CRUD операции для поездок и связанных с ними отзывов.
 * Использует JDBC для взаимодействия с базой данных.
 */
package org.example.kurstrips.dao.impl;

import org.example.kurstrips.dao.TripDAO;
import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.kurstrips.util.LogUtil;
import io.github.cdimascio.dotenv.Dotenv;

public class SQLiteTripDAO implements TripDAO {
    private static final Logger logger = LogUtil.getLogger(SQLiteTripDAO.class);
    private Connection conn;
    private static final Dotenv dotenv = Dotenv.configure().load();

    /*
     * Инициализирует подключение к базе данных SQLite.
     * Параметры подключения загружаются из .env файла.
     * Создает необходимые таблицы, если они не существуют.
     */
    public SQLiteTripDAO() {
        logger.log(Level.INFO, LogUtil.getMessage("dao.init"));
        try {
            String driver = dotenv.get("DB_DRIVER", "org.sqlite.JDBC");
            String url = dotenv.get("DB_URL", "jdbc:sqlite:trips.db");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");

            url = url.replace("${user.dir}", System.getProperty("user.dir"));

            Class.forName(driver);
            logger.log(Level.CONFIG,
                    MessageFormat.format(LogUtil.getMessage("dao.db.path"), url));

            if (user != null && !user.isEmpty() && password != null) {
                conn = DriverManager.getConnection(url, user, password);
            } else {
                conn = DriverManager.getConnection(url);
            }

            if (conn != null) {
                logger.log(Level.INFO, LogUtil.getMessage("dao.connected"));
                initializeDatabase();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, LogUtil.getMessage("error"), e);
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
    }

    /*
     * Создает таблицы trips и reviews в базе данных, если они не существуют.
     * Таблица reviews связана с trips через внешний ключ trip_id.
     */
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

        logger.log(Level.INFO, LogUtil.getMessage("dao.tables.init"));
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTripsTable);
            stmt.execute(createReviewsTable);
            logger.log(Level.INFO, LogUtil.getMessage("dao.tables.created"));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, LogUtil.getMessage("error"), e);
            System.err.println("Ошибка при создании таблиц:");
            e.printStackTrace();
        }
    }

    /*
     * Получает все поездки из базы данных вместе с связанными отзывами.
     * Возвращает список объектов Trip, отсортированный по дате начала поездки (новые сначала).
     * Для поездок без отзывов поле review будет равно null.
     */
    @Override
    public List<Trip> getAllTrips() {
        logger.log(Level.INFO, LogUtil.getMessage("dao.get.trips"));
        List<Trip> trips = new ArrayList<>();
        if (conn == null) {
            logger.severe(LogUtil.getMessage("error") + ": Нет подключения к БД");
            return trips;
        }

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
                            rs.getInt("review_trip_id"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    ));
                }
                trips.add(trip);
            }
            logger.log(Level.INFO,
                    MessageFormat.format(LogUtil.getMessage("dao.trips.retrieved"), trips.size()));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, LogUtil.getMessage("error"), e);
            System.err.println("Ошибка при получении поездок:");
            e.printStackTrace();
        }
        return trips;
    }

    /*
     * Добавляет новую поездку в базу данных.
     * Устанавливает сгенерированный ID в переданный объект Trip.
     * Статус поездки рассчитывается автоматически на основе дат.
     */
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

    /*
     * Обновляет существующую поездку в базе данных.
     * Обновляет все поля поездки, включая статус.
     */
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

    /*
     * Удаляет поездку и все связанные с ней отзывы.
     * Сначала удаляет отзывы, затем саму поездку.
     */
    @Override
    public void deleteTrip(int id) {
        String deleteReviewsSql = "DELETE FROM reviews WHERE trip_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteReviewsSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении отзывов:");
            e.printStackTrace();
            return;
        }

        String deleteTripSql = "DELETE FROM trips WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteTripSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении поездки:");
            e.printStackTrace();
        }
    }

    /*
     * Добавляет отзыв для указанной поездки.
     * Связь между отзывом и поездкой устанавливается через trip_id.
     */
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

    /*
     * Обновляет существующий отзыв.
     * Изменяет оценку и комментарий отзыва с указанным ID.
     */
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

    /*
     * Получает поездку по ID вместе с связанным отзывом (если есть).
     * Возвращает null, если поездка с указанным ID не найдена.
     */
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

    /*
     * Получает поездки в указанном диапазоне дат.
     * Если startDate или endDate равны null, соответствующие условия фильтрации не применяются.
     * Возвращает список поездок, отсортированный по дате начала (новые сначала).
     */
    @Override
    public List<Trip> getTripsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Trip> trips = new ArrayList<>();
        if (conn == null) return trips;

        StringBuilder sql = new StringBuilder("""
        SELECT t.id, t.from_city, t.to_city, t.start_date, t.end_date, t.budget, t.status,
               r.id as review_id, r.trip_id as review_trip_id, r.rating, r.comment 
        FROM trips t LEFT JOIN reviews r ON t.id = r.trip_id
        WHERE 1=1""");

        if (startDate != null) {
            sql.append(" AND t.start_date >= '").append(startDate).append("'");
        }
        if (endDate != null) {
            sql.append(" AND t.end_date <= '").append(endDate).append("'");
        }
        sql.append(" ORDER BY t.start_date DESC");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                Trip trip = mapResultSetToTrip(rs);

                if (rs.getInt("review_id") != 0) {
                    trip.setReview(new Review(
                            rs.getInt("review_id"),
                            rs.getInt("review_trip_id"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    ));
                }
                trips.add(trip);
            }
        }
        return trips;
    }

    /*
     * Преобразует строку ResultSet в объект Trip.
     * Не заполняет поле review - это должно быть сделано отдельно.
     */
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

    /*
     * Закрывает соединение с базой данных.
     * Должен быть вызван перед уничтожением объекта DAO.
     */
    public void close() {
        logger.log(Level.INFO, LogUtil.getMessage("dao.close"));
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Соединение с SQLite закрыто");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, LogUtil.getMessage("error"), e);
            System.err.println("Ошибка при закрытии соединения:");
            e.printStackTrace();
        }
    }
}