package com.erciyes.edu.tr.trafficlightdensity;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TrafficSimApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        // 1) FXML’i classpath’teki mutlak (başında / olan) yoluyla gösteriyoruz
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/erciyes/edu/tr/trafficlightdensity/main.fxml")
        );

        // 2) Artık loader konumu bildiği için sorunsuzca yükler
        Parent root = loader.load();

        // 3) FXML içinde tanımlı genişlik/yükseklik değerlerini kullan; ayrıca el ile 320×240 vermen gerekmez
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Traffic Light Density");
        stage.show();
    }
}
