package org.example.kurstrips.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.converter.LocalDateStringConverter;
import org.example.kurstrips.model.City;
import org.example.kurstrips.model.Review;
import org.example.kurstrips.model.Trip;
import org.example.kurstrips.service.*;
import org.example.kurstrips.dao.TripDAO;
import org.example.kurstrips.dao.impl.SQLiteTripDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.kurstrips.util.LogUtil;

public class MainController {
    private static final Logger logger = LogUtil.getLogger(MainController.class);

    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Button buildRouteBtn;
    @FXML private Button buildOptimalRouteBtn;
    @FXML private WebView webView;
    @FXML private VBox citiesListContainer;
    @FXML private Label routeInfoLabel;
    @FXML private TextField budgetField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button addTripButton;
    @FXML private TabPane tabPane;
    @FXML private TableView<Trip> tripsTable;
    @FXML private TableColumn<Trip, String> fromCityColumn;
    @FXML private TableColumn<Trip, String> toCityColumn;
    @FXML private TableColumn<Trip, String> datesColumn;
    @FXML private TableColumn<Trip, Number> budgetColumn;
    @FXML private TableColumn<Trip, String> statusColumn;
    @FXML private Slider ratingSlider;
    @FXML private TextArea reviewTextArea;
    @FXML private Button submitReviewButton;
    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, String> tripRouteColumn;
    @FXML private TableColumn<Review, Number> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewCommentColumn;
    @FXML private TableColumn<Trip, Number> ratingColumn;
    @FXML private DatePicker filterStartDatePicker;
    @FXML private DatePicker filterEndDatePicker;
    @FXML private Button applyFilterButton;
    @FXML private Button resetFilterButton;

    private final MapService mapService = new YandexMapServiceImpl();
    private final CityService cityService = new CityService();
    private final TripDAO tripDAO = new SQLiteTripDAO();
    private final ObservableList<Trip> trips = FXCollections.observableArrayList();
    private final ObservableList<Review> allReviews = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        logger.log(Level.INFO, "Инициализация MainController");
        try {
            mapService.initializeMap(webView);
            logger.log(Level.INFO, "Сервис карт успешно инициализирован");

            buildRouteBtn.setOnAction(event -> buildSimpleRoute());
            buildOptimalRouteBtn.setOnAction(event -> buildOptimalRoute());
            addTripButton.setOnAction(event -> addTrip());
            submitReviewButton.setOnAction(event -> submitReview());

            fromCityColumn.setCellValueFactory(cellData -> cellData.getValue().fromCityProperty());
            toCityColumn.setCellValueFactory(cellData -> cellData.getValue().toCityProperty());
            datesColumn.setCellValueFactory(cellData -> {
                Trip trip = cellData.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                String startDate = trip.getStartDate().format(formatter);
                String endDate = trip.getEndDate().format(formatter);
                return new SimpleStringProperty(startDate + " - " + endDate);
            });
            budgetColumn.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            ratingColumn.setCellValueFactory(cellData -> {
                Review review = cellData.getValue().getReview();
                return review != null ? review.ratingProperty() : new SimpleIntegerProperty(0);
            });

            tripsTable.setItems(trips);
            loadTrips();

            tripRouteColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(getTripRoute(cellData.getValue().getTripId())));
            reviewRatingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty());
            reviewCommentColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());

            loadAllData();

            if (!trips.isEmpty()) {
                tripsTable.getSelectionModel().selectFirst();
                logger.log(Level.FINE, "Первая поездка выбрана по умолчанию");
            }

            refreshAllData();

            tripsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> updateReviewsForSelectedTrip()
            );

            applyFilterButton.setOnAction(event -> applyDateFilter());
            resetFilterButton.setOnAction(event -> resetDateFilter());

            filterStartDatePicker.setConverter(new LocalDateStringConverter());
            filterEndDatePicker.setConverter(new LocalDateStringConverter());

            logger.log(Level.INFO, "MainController успешно инициализирован");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при инициализации MainController", e);
            throw new RuntimeException("Не удалось инициализировать контроллер", e);
        }
    }

    private void applyDateFilter() {
        LocalDate startDate = filterStartDatePicker.getValue();
        LocalDate endDate = filterEndDatePicker.getValue();
        logger.log(Level.INFO, "Применение фильтра по датам: {0} - {1}", new Object[]{startDate, endDate});

        if (startDate == null && endDate == null) {
            logger.log(Level.WARNING, "Не выбраны даты для фильтрации");
            showAlert("Ошибка", "Не выбраны даты", "Выберите хотя бы одну дату для фильтрации");
            return;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            logger.log(Level.WARNING, "Некорректный диапазон дат: {0} > {1}", new Object[]{startDate, endDate});
            showAlert("Ошибка", "Некорректный диапазон", "Дата 'От' не может быть позже даты 'До'");
            return;
        }

        try {
            List<Trip> filteredTrips = tripDAO.getTripsByDateRange(startDate, endDate);
            trips.setAll(filteredTrips);
            logger.log(Level.INFO, "Фильтр применен. Найдено поездок: {0}", filteredTrips.size());
            showAlert("Фильтр применен", "Отфильтровано поездок: " + filteredTrips.size(), "");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при фильтрации поездок", e);
            showAlert("Ошибка", "Ошибка фильтрации", e.getMessage());
        }
    }

    private void resetDateFilter() {
        logger.log(Level.INFO, "Сброс фильтра по датам");
        filterStartDatePicker.setValue(null);
        filterEndDatePicker.setValue(null);
        loadTrips();
        showAlert("Фильтр сброшен", "Показаны все поездки", "");
    }

    private void refreshAllData() {
        logger.log(Level.INFO, "Обновление всех данных");
        try {
            List<Trip> allTrips = tripDAO.getAllTrips();
            trips.setAll(allTrips);

            allReviews.clear();
            for (Trip trip : allTrips) {
                if (trip.getReview() != null) {
                    allReviews.add(trip.getReview());
                }
            }

            tripsTable.refresh();
            reviewsTable.setItems(allReviews);

            logger.log(Level.INFO, "Данные обновлены. Поездок: {0}, Отзывов: {1}",
                    new Object[]{trips.size(), allReviews.size()});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении данных", e);
            showAlert("Ошибка", "Ошибка загрузки данных",
                    "Не удалось загрузить данные из базы данных");
        }
    }

    private void loadAllData() {
        logger.log(Level.FINE, "Загрузка всех данных");
        loadTrips();
        loadAllReviews();
    }

    private void loadAllReviews() {
        logger.log(Level.FINE, "Загрузка всех отзывов");
        allReviews.clear();
        for (Trip trip : trips) {
            if (trip.getReview() != null) {
                allReviews.add(trip.getReview());
            }
        }
        reviewsTable.setItems(allReviews);
    }

    private void updateReviewsForSelectedTrip() {
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            logger.log(Level.FINE, "Обновление отзывов для поездки ID: {0}", selectedTrip.getId());
            ObservableList<Review> filteredReviews = FXCollections.observableArrayList();
            if (selectedTrip.getReview() != null) {
                filteredReviews.add(selectedTrip.getReview());
            }
            reviewsTable.setItems(filteredReviews);
        }
    }

    private String getTripRoute(int tripId) {
        return trips.stream()
                .filter(trip -> trip.getId() == tripId)
                .findFirst()
                .map(trip -> trip.getFromCity() + " → " + trip.getToCity())
                .orElse("Неизвестный маршрут");
    }

    private void addTrip() {
        logger.log(Level.INFO, "Добавление новой поездки");
        try {
            String from = startField.getText().trim();
            String to = endField.getText().trim();
            double budget = Double.parseDouble(budgetField.getText().trim());
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (from.isEmpty() || to.isEmpty()) {
                logger.log(Level.WARNING, "Не заполнены города маршрута");
                showAlert("Ошибка", "Заполните города", "Введите начальный и конечный город");
                return;
            }

            if (startDate == null || endDate == null) {
                logger.log(Level.WARNING, "Не заполнены даты поездки");
                showAlert("Ошибка", "Заполните даты", "Выберите даты начала и окончания поездки");
                return;
            }

            if (startDate.isAfter(endDate)) {
                logger.log(Level.WARNING, "Некорректные даты: {0} > {1}",
                        new Object[]{startDate, endDate});
                showAlert("Ошибка", "Некорректные даты", "Дата начала должна быть раньше даты окончания");
                return;
            }

            Trip trip = new Trip(0, from, to, startDate, endDate, budget);
            tripDAO.addTrip(trip);
            trips.add(trip);

            logger.log(Level.INFO, "Поездка добавлена: ID={0}, {1} -> {2}, {3} - {4}, бюджет={5}",
                    new Object[]{trip.getId(), from, to, startDate, endDate, budget});
            showAlert("Успех", "Поездка добавлена", "Поездка успешно добавлена в список");
            clearTripFields();
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Некорректный формат бюджета: {0}", budgetField.getText());
            showAlert("Ошибка", "Некорректный бюджет", "Введите корректную сумму бюджета");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении поездки", e);
            showAlert("Ошибка", "Ошибка добавления", "Не удалось добавить поездку");
        }
    }

    private void submitReview() {
        logger.log(Level.INFO, "Отправка отзыва");
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            logger.log(Level.WARNING, "Не выбрана поездка для отзыва");
            showAlert("Ошибка", "Не выбрана поездка", "Выберите поездку из списка");
            return;
        }

        int rating = (int) ratingSlider.getValue();
        String comment = reviewTextArea.getText().trim();

        try {
            if (selectedTrip.getReview() != null) {
                logger.log(Level.FINE, "Обновление существующего отзыва для поездки ID: {0}", selectedTrip.getId());
                tripDAO.updateReview(selectedTrip.getReview().getId(), rating, comment);
            } else {
                logger.log(Level.FINE, "Добавление нового отзыва для поездки ID: {0}", selectedTrip.getId());
                tripDAO.addReview(selectedTrip.getId(), rating, comment);
            }

            refreshAllData();

            for (Trip trip : trips) {
                if (trip.getId() == selectedTrip.getId()) {
                    tripsTable.getSelectionModel().select(trip);
                    break;
                }
            }

            ratingSlider.setValue(3);
            reviewTextArea.clear();

            logger.log(Level.INFO, "Отзыв сохранен: поездка ID={0}, рейтинг={1}, комментарий={2}",
                    new Object[]{selectedTrip.getId(), rating, comment});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении отзыва", e);
            showAlert("Ошибка", "Ошибка базы данных",
                    "Не удалось сохранить отзыв: " + e.getMessage());
        }
    }

    private void loadTrips() {
        logger.log(Level.INFO, "Загрузка списка поездок");
        try {
            trips.setAll(tripDAO.getAllTrips());
            tripsTable.setItems(trips);
            logger.log(Level.INFO, "Загружено поездок: {0}", trips.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке поездок", e);
        }
    }

    private void clearTripFields() {
        logger.log(Level.FINE, "Очистка полей ввода поездки");
        budgetField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void buildSimpleRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();
        logger.log(Level.INFO, "Построение простого маршрута: {0} -> {1}", new Object[]{from, to});

        if (from.isEmpty() || to.isEmpty()) {
            logger.log(Level.WARNING, "Не заполнены поля маршрута");
            showAlert("Ошибка", "Не заполнены поля", "Введите оба адреса");
            return;
        }

        try {
            mapService.buildRoute(webView, from, to);
            citiesListContainer.getChildren().clear();
            routeInfoLabel.setText("Прямой маршрут построен");
            logger.log(Level.INFO, "Прямой маршрут успешно построен");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при построении маршрута", e);
            showAlert("Ошибка", "Ошибка построения маршрута", e.getMessage());
        }
    }

    private void buildOptimalRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();
        logger.log(Level.INFO, "Построение оптимального маршрута: {0} -> {1}", new Object[]{from, to});

        if (from.isEmpty() || to.isEmpty()) {
            logger.log(Level.WARNING, "Не заполнены поля маршрута");
            showAlert("Ошибка", "Не заполнены поля", "Введите оба адреса");
            return;
        }

        try {
            City start = mapService.geocode(from);
            City end = mapService.geocode(to);
            logger.log(Level.FINE, "Координаты получены: старт={0}, конец={1}", new Object[]{start, end});

            List<City> intermediateCities = cityService.findIntermediateCities(start, end);
            logger.log(Level.FINE, "Найдено промежуточных городов: {0}", intermediateCities.size());

            List<City> fullRoute = new ArrayList<>();
            fullRoute.add(start);
            fullRoute.addAll(intermediateCities);
            fullRoute.add(end);

            mapService.displayRouteWithCities(webView, start, end, intermediateCities);
            showCitiesList(intermediateCities);
            logger.log(Level.INFO, "Оптимальный маршрут построен через {0} городов", intermediateCities.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при построении оптимального маршрута", e);
            showAlert("Ошибка", "Ошибка построения маршрута", e.getMessage());
        }
    }

    private void showCitiesList(List<City> cities) {
        logger.log(Level.FINE, "Отображение списка городов (количество: {0})", cities.size());
        citiesListContainer.getChildren().clear();

        if (cities.isEmpty()) {
            routeInfoLabel.setText("Прямой маршрут без промежуточных городов");
            logger.log(Level.FINE, "Маршрут без промежуточных городов");
            return;
        }

        routeInfoLabel.setText("Маршрут проходит через:");
        for (City city : cities) {
            Label cityLabel = new Label(city.getName());
            cityLabel.setStyle("-fx-padding: 5; -fx-font-size: 14;");
            citiesListContainer.getChildren().add(cityLabel);
        }
    }

    private void showAlert(String title, String header, String content) {
        logger.log(Level.INFO, "Показ предупреждения: {0} - {1} - {2}",
                new Object[]{title, header, content});
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        logger.log(Level.INFO, "Завершение работы контроллера");
        try {
            ((SQLiteTripDAO)tripDAO).close();
            logger.log(Level.INFO, "Ресурсы освобождены");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при завершении работы", e);
        }
    }
}