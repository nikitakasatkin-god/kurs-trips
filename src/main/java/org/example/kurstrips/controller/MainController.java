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

public class MainController {
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
        mapService.initializeMap(webView);

        buildRouteBtn.setOnAction(event -> buildSimpleRoute());
        buildOptimalRouteBtn.setOnAction(event -> buildOptimalRoute());

        addTripButton.setOnAction(event -> addTrip());
        submitReviewButton.setOnAction(event -> submitReview());

        // Настройка таблицы поездок
        fromCityColumn.setCellValueFactory(cellData -> cellData.getValue().fromCityProperty());
        toCityColumn.setCellValueFactory(cellData -> cellData.getValue().toCityProperty());
        // Настройка колонки с датами с новым форматом
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

        // Настройка новой колонки с рейтингом в таблице поездок
        ratingColumn.setCellValueFactory(cellData -> {
            Review review = cellData.getValue().getReview();
            return review != null ? review.ratingProperty() : new SimpleIntegerProperty(0);
        });

        // Настройка таблицы отзывов
        tripRouteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getTripRoute(cellData.getValue().getTripId())));
        reviewRatingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty());
        reviewCommentColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());

        // Загрузка данных при инициализации
        loadAllData();

        // Выделение первой поездки по умолчанию
        if (!trips.isEmpty()) {
            tripsTable.getSelectionModel().selectFirst();
        }

        // Загрузка данных при старте
        refreshAllData();

        // Обновление таблицы отзывов при выборе поездки
        tripsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateReviewsForSelectedTrip()
        );

        // Выделение первой поездки если есть
        if (!trips.isEmpty()) {
            tripsTable.getSelectionModel().selectFirst();
        }

        // Инициализация фильтра
        applyFilterButton.setOnAction(event -> applyDateFilter());
        resetFilterButton.setOnAction(event -> resetDateFilter());

        // Настройка формата даты
        filterStartDatePicker.setConverter(new LocalDateStringConverter());
        filterEndDatePicker.setConverter(new LocalDateStringConverter());
    }

    private void applyDateFilter() {
        LocalDate startDate = filterStartDatePicker.getValue();
        LocalDate endDate = filterEndDatePicker.getValue();

        // Валидация дат
        if (startDate == null && endDate == null) {
            showAlert("Ошибка", "Не выбраны даты", "Выберите хотя бы одну дату для фильтрации");
            return;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showAlert("Ошибка", "Некорректный диапазон", "Дата 'От' не может быть позже даты 'До'");
            return;
        }

        try {
            List<Trip> filteredTrips = tripDAO.getTripsByDateRange(startDate, endDate);
            trips.setAll(filteredTrips);
            showAlert("Фильтр применен", "Отфильтровано поездок: " + filteredTrips.size(), "");
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка фильтрации", e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetDateFilter() {
        filterStartDatePicker.setValue(null);
        filterEndDatePicker.setValue(null);
        loadTrips(); // Загружаем все поездки заново
        showAlert("Фильтр сброшен", "Показаны все поездки", "");
    }

    private void refreshAllData() {
        try {
            // Полная перезагрузка данных из БД
            List<Trip> allTrips = tripDAO.getAllTrips();
            trips.setAll(allTrips);

            // Обновление всех отзывов
            allReviews.clear();
            for (Trip trip : allTrips) {
                if (trip.getReview() != null) {
                    allReviews.add(trip.getReview());
                }
            }

            // Обновление таблиц
            tripsTable.refresh();
            reviewsTable.setItems(allReviews); // Показываем все отзывы

            System.out.println("Data refreshed. Trips: " + trips.size() +
                    ", Reviews: " + allReviews.size());
        } catch (Exception e) {
            System.err.println("Error refreshing data:");
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка загрузки данных",
                    "Не удалось загрузить данные из базы данных");
        }
    }

    private void loadAllData() {
        loadTrips();
        loadAllReviews();
    }

    private void loadAllReviews() {
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

    private void updateReviewsTable() {
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null && selectedTrip.getReview() != null) {
            reviewsTable.setItems(FXCollections.observableArrayList(selectedTrip.getReview()));
        } else {
            reviewsTable.setItems(FXCollections.emptyObservableList());
        }
    }

    private void addTrip() {
        try {
            String from = startField.getText().trim();
            String to = endField.getText().trim();
            double budget = Double.parseDouble(budgetField.getText().trim());
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (from.isEmpty() || to.isEmpty()) {
                showAlert("Ошибка", "Заполните города", "Введите начальный и конечный город");
                return;
            }

            if (startDate == null || endDate == null) {
                showAlert("Ошибка", "Заполните даты", "Выберите даты начала и окончания поездки");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert("Ошибка", "Некорректные даты", "Дата начала должна быть раньше даты окончания");
                return;
            }

            Trip trip = new Trip(0, from, to, startDate, endDate, budget);
            tripDAO.addTrip(trip);
            trips.add(trip);

            showAlert("Успех", "Поездка добавлена", "Поездка успешно добавлена в список");
            clearTripFields();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректный бюджет", "Введите корректную сумму бюджета");
        }
    }

    private void submitReview() {
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            showAlert("Ошибка", "Не выбрана поездка", "Выберите поездку из списка");
            return;
        }

        int rating = (int) ratingSlider.getValue();
        String comment = reviewTextArea.getText().trim();

        try {
            // Добавление/обновление отзыва
            if (selectedTrip.getReview() != null) {
                tripDAO.updateReview(selectedTrip.getReview().getId(), rating, comment);
            } else {
                tripDAO.addReview(selectedTrip.getId(), rating, comment);
            }

            // Полная перезагрузка данных
            refreshAllData();

            // Восстановление выбора
            for (Trip trip : trips) {
                if (trip.getId() == selectedTrip.getId()) {
                    tripsTable.getSelectionModel().select(trip);
                    break;
                }
            }

            // Очистка полей
            ratingSlider.setValue(3);
            reviewTextArea.clear();

        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка базы данных",
                    "Не удалось сохранить отзыв: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTrips() {
        trips.setAll(tripDAO.getAllTrips());
        tripsTable.setItems(trips);
    }

    private void clearTripFields() {
        budgetField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void buildSimpleRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            showAlert("Ошибка", "Не заполнены поля", "Введите оба адреса");
            return;
        }

        try {
            mapService.buildRoute(webView, from, to);
            citiesListContainer.getChildren().clear();
            routeInfoLabel.setText("Прямой маршрут построен");
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка построения маршрута", e.getMessage());
        }
    }

    private void buildOptimalRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            showAlert("Ошибка", "Не заполнены поля", "Введите оба адреса");
            return;
        }

        try {
            City start = mapService.geocode(from);
            City end = mapService.geocode(to);

            List<City> intermediateCities = cityService.findIntermediateCities(start, end);

            // Строим полный маршрут: начало -> промежуточные города -> конец
            List<City> fullRoute = new ArrayList<>();
            fullRoute.add(start);
            fullRoute.addAll(intermediateCities);
            fullRoute.add(end);

            mapService.displayRouteWithCities(webView, start, end, intermediateCities);
            showCitiesList(intermediateCities);

        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка построения маршрута", e.getMessage());
        }
    }

    private void showCitiesList(List<City> cities) {
        citiesListContainer.getChildren().clear();

        if (cities.isEmpty()) {
            routeInfoLabel.setText("Прямой маршрут без промежуточных городов");
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        showAlert("Предупреждение", null, message);
    }

    public void shutdown() {
        ((SQLiteTripDAO)tripDAO).close();
    }
}