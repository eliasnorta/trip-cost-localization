module org.example.tripcostlocalization {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.tripcostlocalization to javafx.fxml;
    exports org.example.tripcostlocalization;
}