package org.example.tripcostlocalization;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("messages_messages", Locale.US);
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load() );
        stage.setTitle("Trip Cost Calculator - Elias Norta");
        stage.setScene(scene);
        stage.show();
    }
}
