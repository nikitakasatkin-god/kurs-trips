module org.example.kurstrips {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;
    requires java.sql;
    requires java.logging;

    opens org.example.kurstrips to javafx.fxml;
    opens org.example.kurstrips.model to org.junit.jupiter;
    opens org.example.kurstrips.service to org.junit.jupiter;
    opens org.example.kurstrips.dao.impl to org.junit.jupiter;

    exports org.example.kurstrips;
    exports org.example.kurstrips.service;
    exports org.example.kurstrips.dao;
    exports org.example.kurstrips.controller;
    exports org.example.kurstrips.model;
    exports org.example.kurstrips.dao.impl;


    opens org.example.kurstrips.controller to javafx.fxml;
}