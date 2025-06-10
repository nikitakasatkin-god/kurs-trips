module org.example.kurstrips {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;
    requires java.sql;
    requires java.logging;

    opens org.example.kurstrips to javafx.fxml;
    exports org.example.kurstrips;
    exports org.example.kurstrips.controller;
    opens org.example.kurstrips.controller to javafx.fxml;
}