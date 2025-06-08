module org.example.kurstrips {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;

    opens org.example.kurstrips to javafx.fxml;
    exports org.example.kurstrips;
}