package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserInterfaceController {
    // Arayüz buton vs. burda çizilecek
    private Timeline countdownTimeline;
    TrafficController trafficController = new TrafficController();
    SimulationManager simulationManager = new SimulationManager();
    boolean isRandom,isInput;

    @FXML
    private Pane mainPane;

    @FXML
    private Button random_select_button;

    @FXML
    private Button user_input_button;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button rerunButton;

    @FXML
    private Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;

    @FXML
    private VBox topVBox;

    private Direction currentDirection;

    public void initialize() {
        // buton/arayüz ayarları

        topVBox.setVisible(true);
        mainPane.setVisible(false);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
        rerunButton.setVisible(false);

        simulationManager.setOnTick(kalanSure -> {
            labeliGuncelle(currentDirection, kalanSure);
        });

        // ➕ Aktif yön değiştiğinde GUI'ye bildir
        simulationManager.setOnPhaseChange(direction -> {
            currentDirection = direction; // aktif yönü tutalım
        });
    }

    @FXML
    private void onRandomSelect(ActionEvent e) {
        isRandom=true;
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
    }

    @FXML
    private void onUserInputSelect(ActionEvent e) {
        isInput=true;
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);

        for (Direction yon : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Araç Girişi");
            dialog.setHeaderText(yon.getTurkishName() + " yönü için:");
            Optional<String> sonuc = dialog.showAndWait();
            if (sonuc.isPresent()) {
                int sayi = Integer.parseInt(sonuc.get());
                trafficController.getVehicleCounts().put(yon, sayi);
            }
        }
        trafficController.updateDurations();
        labelSureleriniGuncelle();
    }

    @FXML
    private void onStartSimulation(ActionEvent e) {
        System.out.println("Simulasyon başlatıldı!");
        if (isInput) {
            simulationManager.startManualMode();

        }
        else if (isRandom) {
            return;
        }


    }

    @FXML
    private void onPauseSimulation(ActionEvent e) {

    }

    @FXML
    private void onRerunSimulation(ActionEvent e) {

    }
    private void labelSureleriniGuncelle() {
        northTimerLabel.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");
        eastTimerLabel.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        southTimerLabel.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        westTimerLabel.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
    }

    private void labeliGuncelle(Direction aktifYon, int sure) {
        // Önce hepsini temizle
        northTimerLabel.setText("—");
        southTimerLabel.setText("—");
        eastTimerLabel.setText("—");
        westTimerLabel.setText("—");

        // Aktif olan yöne süreyi yaz
        switch (aktifYon) {
            case NORTH -> northTimerLabel.setText(sure + " sn");
            case SOUTH -> southTimerLabel.setText(sure + " sn");
            case EAST  -> eastTimerLabel.setText(sure + " sn");
            case WEST  -> westTimerLabel.setText(sure + " sn");
        }
        //labeliGuncelle(activeDirection, trafficController.getGreenDuration(activeDirection));
    }

}
