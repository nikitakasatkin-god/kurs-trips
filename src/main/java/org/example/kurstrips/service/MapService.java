package org.example.kurstrips.service;

import javafx.scene.web.WebView;
import org.example.kurstrips.model.City;

import java.util.List;

public interface MapService {
    void initializeMap(WebView webView);
    void buildRoute(WebView webView, String from, String to);
    void addTestRoute(WebView webView);
    void addTestMarker(WebView webView);
    void displayRouteWithCities(WebView webView, City start, City end, List<City> routeCities);
    City geocode(String address) throws Exception;
}