import org.example.kurstrips.dao.impl.SQLiteTripDAO;
import org.example.kurstrips.model.Trip;
import org.example.kurstrips.model.Review;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteTripDAOTest {
    private static SQLiteTripDAO dao;
    private Trip testTrip;

    @BeforeAll
    static void setUpAll() {
        dao = new SQLiteTripDAO();
    }

    @BeforeEach
    void setUp() {
        cleanTestData();
        testTrip = new Trip(999, "Москва", "Санкт-Петербург",
                LocalDate.now(), LocalDate.now().plusDays(7), 1000.0);
        dao.addTrip(testTrip);
    }

    @AfterEach
    void cleanTestData() {
        try {
            if (testTrip != null) {
                dao.deleteTrip(testTrip.getId());
            }
            // Очистка тестовых отзывов
            dao.updateReview(999, 0, "");
        } catch (Exception e) {
            System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownAll() {
        dao.close();
    }

    @Test
    void testGetAllTrips() {
        List<Trip> trips = dao.getAllTrips();
        assertFalse(trips.isEmpty(), "Список поездок не должен быть пустым");
        assertTrue(trips.size() >= 1, "Должна быть хотя бы одна тестовая поездка");
    }

    @Test
    void testGetTripById() {
        Trip retrieved = dao.getTripById(testTrip.getId());
        assertNotNull(retrieved, "Поездка должна быть найдена");
        assertEquals("Москва", retrieved.getFromCity(), "Город отправления должен совпадать");
        assertEquals("Санкт-Петербург", retrieved.getToCity(), "Город назначения должен совпадать");
        assertEquals(1000.0, retrieved.getBudget(), "Бюджет должен совпадать");
    }

    @Test
    void testAddAndGetReview() {
        dao.addReview(testTrip.getId(), 5, "Отличная поездка!");

        Trip tripWithReview = dao.getTripById(testTrip.getId());
        assertNotNull(tripWithReview.getReview(), "Отзыв должен существовать");
        assertEquals(5, tripWithReview.getReview().getRating(), "Рейтинг должен совпадать");
        assertEquals("Отличная поездка!", tripWithReview.getReview().getComment(), "Комментарий должен совпадать");
    }

    @Test
    void testUpdateTrip() {
        Trip updatedTrip = new Trip(
                testTrip.getId(),
                "Москва",
                "Казань",
                testTrip.getStartDate(),
                testTrip.getEndDate(),
                1500.0
        );

        dao.updateTrip(updatedTrip);

        Trip retrieved = dao.getTripById(testTrip.getId());
        assertEquals("Казань", retrieved.getToCity(), "Город назначения должен обновиться");
        assertEquals(1500.0, retrieved.getBudget(), "Бюджет должен обновиться");
    }

    @Test
    void testGetTripsByDateRange() throws SQLException {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        List<Trip> filteredTrips = dao.getTripsByDateRange(start, end);
        assertFalse(filteredTrips.isEmpty(), "Должна найтись тестовая поездка в этом диапазоне");
    }

    @Test
    void testDeleteTrip() {
        int tripId = testTrip.getId();
        dao.deleteTrip(tripId);
        assertNull(dao.getTripById(tripId), "Поездка должна быть удалена");
    }
}