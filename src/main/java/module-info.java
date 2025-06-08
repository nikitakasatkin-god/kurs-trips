module org.example.kurstrips {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.kurstrips to javafx.fxml;
    exports org.example.kurstrips;
}