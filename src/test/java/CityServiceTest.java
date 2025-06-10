import org.example.kurstrips.model.City;
import org.example.kurstrips.service.CityService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CityServiceTest {
    private final CityService cityService = new CityService();

    @Test
    void testFindIntermediateCities() {
        City start = new City("Москва", 55.755814, 37.617635);
        City end = new City("Санкт-Петербург", 59.938630, 30.314130);

        List<City> intermediateCities = cityService.findIntermediateCities(start, end);

        assertFalse(intermediateCities.isEmpty(),
                "Между Москвой и Питером должны быть промежуточные города");

        // Более гибкая проверка с допуском
        for (City city : intermediateCities) {
            double minLat = Math.min(start.getLatitude(), end.getLatitude());
            double maxLat = Math.max(start.getLatitude(), end.getLatitude());

            assertTrue(
                    (city.getLatitude() >= minLat - 0.5 && city.getLatitude() <= maxLat + 0.5),
                    "Город " + city.getName() + " (широта: " + city.getLatitude() +
                            ") должен быть между Москвой (" + start.getLatitude() +
                            ") и Питером (" + end.getLatitude() + ")"
            );
        }
    }

    @Test
    void testFindNearestCity() {
        City target = new City("Тестовая локация", 55.916670, 37.816670); // Координаты Королёва
        City nearest = cityService.findNearestCity(target);

        assertNotNull(nearest, "Должен быть найден ближайший город");
        assertEquals("Королёв", nearest.getName(),
                "Ближайший город к координатам Королёва должен быть Королёв");
    }

    @Test
    void testDistanceCalculation() {
        City moscow = new City("Москва", 55.755814, 37.617635);
        City petersburg = new City("Санкт-Петербург", 59.938630, 30.314130);

        double distance = moscow.distanceTo(petersburg);
        assertTrue(distance > 600 && distance < 700,
                "Расстояние между Москвой и Питером должно быть около 634 км");
    }
}