package org.example.kurstrips.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;
import org.example.kurstrips.model.Review;
import org.example.kurstrips.model.Trip;
import org.example.kurstrips.service.*;
import org.example.kurstrips.dao.TripDAO;
import org.example.kurstrips.dao.impl.SQLiteTripDAO;

import java.sql.SQLException;
import java.time.LocalDate;
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

    private final MapService mapService = new YandexMapServiceImpl();
    private final CityService cityService = new CityService();
    private final TripDAO tripDAO = new SQLiteTripDAO();
    private final ObservableList<Trip> trips = FXCollections.observableArrayList();

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
        datesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartDate() + " - " +
                        cellData.getValue().getEndDate()));
        budgetColumn.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tripsTable.setItems(trips);
        loadTrips();
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

        if (rating < 1 || rating > 5) {
            showAlert("Ошибка", "Некорректная оценка", "Оценка должна быть от 1 до 5");
            return;
        }

        try {
            // Добавляем отзыв в базу данных
            tripDAO.addReview(selectedTrip.getId(), rating, comment);

            // Обновляем отзыв в выбранной поездке
            Review review = new Review(0, selectedTrip.getId(), rating, comment);
            selectedTrip.setReview(review);

            // Обновляем отображение таблицы
            tripsTable.refresh();

            // Очищаем поля ввода
            ratingSlider.setValue(3);
            reviewTextArea.clear();

            showAlert("Успех", "Отзыв добавлен", "Ваш отзыв успешно сохранен");
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка базы данных", "Не удалось сохранить отзыв: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTrips() {
        trips.setAll(tripDAO.getAllTrips());
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