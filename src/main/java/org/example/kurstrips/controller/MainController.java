package org.example.kurstrips.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;
import org.example.kurstrips.service.*;

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

    private final MapService mapService = new YandexMapServiceImpl();
    private final CityService cityService = new CityService();

    @FXML
    public void initialize() {
        mapService.initializeMap(webView);

        buildRouteBtn.setOnAction(event -> buildSimpleRoute());
        buildOptimalRouteBtn.setOnAction(event -> buildOptimalRoute());
    }

    private void buildSimpleRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            showAlert("Введите оба адреса");
            return;
        }

        try {
            mapService.buildRoute(webView, from, to);
            citiesListContainer.getChildren().clear();
            routeInfoLabel.setText("Прямой маршрут построен");
        } catch (Exception e) {
            showAlert("Ошибка построения маршрута: " + e.getMessage());
        }
    }

    private void buildOptimalRoute() {
        String from = startField.getText().trim();
        String to = endField.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            showAlert("Введите оба адреса");
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
            showAlert("Ошибка построения маршрута: " + e.getMessage());
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

    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message).show();
    }
}