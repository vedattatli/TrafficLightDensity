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
import javafx.scene.shape.Rectangle;

import java.util.*;

public class UserInterfaceController {

    SimulationManager simulationManager;
    TrafficController trafficController;
    TimerDisplay timerDisplay;
    VehicleAnimation vehicleAnimator;

    public UserInterfaceController(SimulationManager simulationManager, TrafficController trafficController,TimerDisplay timerDisplay)
    {
        this.trafficController = trafficController;
        this.simulationManager = simulationManager;
        this.timerDisplay = timerDisplay;
    }

    public UserInterfaceController()
    {

    }

    boolean isRandom;
    boolean simulationIsCurrentlyPaused = false;

    @FXML private Pane mainPane;
//    @FXML private Button random_select_button;
//    @FXML private Button user_input_button;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button rerunButton;
    @FXML public Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;
    @FXML private VBox topVBox;
    @FXML public Label displayNorthCarCount,displayNorthGreenTime,displayNorthRedTime,displaySouthCarCount;
    @FXML public Label displaySouthGreenTime,displaySouthRedTime,displayEastCarCount,displayEastGreenTime;
    @FXML public Label displayEastRedTime,displayWestCarCount,displayWestGreenTime,displayWestRedTime;


    private Direction currentDirectionForLabelUpdate;

    public void initialize() {
        simulationManager = new SimulationManager();
        vehicleAnimator = new VehicleAnimation(simulationManager);
        trafficController = new TrafficController();
        this.timerDisplay = new TimerDisplay(UserInterfaceController.this, trafficController);
        simulationManager.setTimerDisplay(this.timerDisplay);


        topVBox.setVisible(true);
        mainPane.setVisible(false);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
        rerunButton.setVisible(false);
        timerDisplay.resetTimerLabels();
        timerDisplay.resetLabelDisplay();

        isRandom = false;
        simulationIsCurrentlyPaused = false;
        pauseButton.setText("Pause");

        simulationManager.setOnTick(kalanSure -> {
            if (this.currentDirectionForLabelUpdate != null) {
                timerDisplay.labeliGuncelle(this.currentDirectionForLabelUpdate, kalanSure);
            } else {
                timerDisplay.resetTimerLabels();
            }
        });

        simulationManager.setOnPhaseInfoChange(direction -> {
            this.currentDirectionForLabelUpdate = direction;
            if (direction == null) { // Simülasyon durduysa veya hata varsa
               timerDisplay.resetTimerLabels();
            }
            // Işık renklerini de güncellemek için (Yeşil, Sarı, Kırmızı gösterimi)
            // burada her bir label'ın rengini de SimulationManager'dan alacağımız
            // LightPhase bilgisine göre ayarlayabiliriz. Bu kısım eklenmedi.
        });
    }

    @FXML
    private void onRandomSelect(ActionEvent e) {
        isRandom = true;
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
        startButton.setDisable(false);


        System.out.println("Rastgele araç sayısı modu seçildi.");
    }

    @FXML
    private void onUserInputSelect(ActionEvent e) {
        isRandom = false;
        trafficController.getVehicleCounts().clear();

        for (Direction yon : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            TextInputDialog dialog = new TextInputDialog("5"); // Varsayılan 5 araç olsun
            dialog.setTitle("Araç Girişi");
            dialog.setHeaderText(yon.getTurkishName() + " yönü için araç sayısı:");
            dialog.setContentText("Sayı:");
            Optional<String> sonuc = dialog.showAndWait();
            if (sonuc.isPresent() && !sonuc.get().trim().isEmpty()) {
                try {
                    int sayi = Integer.parseInt(sonuc.get().trim());
                    trafficController.getVehicleCounts().put(yon, Math.max(0, sayi));
                } catch (NumberFormatException ex) {
                    trafficController.getVehicleCounts().put(yon, 0);
                }
            } else {
                trafficController.getVehicleCounts().put(yon, 0);
            }
        }
        trafficController.updateDurations();
        timerDisplay.labelTimerBaslangic();
        timerDisplay.labelDisplayBaslangic();

        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
        startButton.setDisable(false);
        System.out.println("Manuel araç sayısı modu seçildi ve veriler girildi.");
    }

    @FXML
    private void onStartSimulation(ActionEvent e) {
        System.out.println("Simülasyon başlatılıyor...");
        simulationIsCurrentlyPaused = false;
        startButton.setDisable(true);

        TrafficController activeTrafficController;
        if (isRandom) {
            simulationManager.startAutoMode();
        } else {
            simulationManager.startManualMode(this.trafficController.getVehicleCounts()); // Bu, trafficController'ı günceller
        }
        activeTrafficController = simulationManager.getTrafficController();
        vehicleAnimator.initializeVehicles(activeTrafficController, mainPane); // Güncel trafficController ile araçları oluştur
        vehicleAnimator.startAnimation(); // Animasyonu başlat
    }

    @FXML
    private void onPauseSimulation(ActionEvent e) {
        if (!simulationManager.isRunning()) {
            System.out.println("Pause: Simülasyon çalışmıyor.");
            return;
        }

        if (simulationIsCurrentlyPaused) {
            simulationManager.resumeSimulation();
            vehicleAnimator.startAnimation(); // Animasyonu da devam ettir
            pauseButton.setText("Duraklat");
            simulationIsCurrentlyPaused = false;
        } else {
            simulationManager.pauseSimulation();
            vehicleAnimator.stopAnimation(); // Animasyonu da duraklat (durdur)
            pauseButton.setText("Devam et");
            simulationIsCurrentlyPaused = true;
        }
    }

    @FXML
    private void onRerunSimulation(ActionEvent e) { // Reset butonu
        System.out.println("Simülasyon sıfırlanıyor (Reset)...");
        simulationManager.stopSimulation(); // Beyin simülasyonunu durdur
        vehicleAnimator.clearAllVehicles();   // Araçları ve animasyonu temizle/durdur

        trafficController.getVehicleCounts().clear();
        trafficController.updateDurations();

        initialize(); // UI ve bayrakları başlangıç durumuna getir
    }

}