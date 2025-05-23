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
import java.util.List;
import java.util.Optional;

public class UserInterfaceController {
    TrafficController trafficController = new TrafficController();
    SimulationManager simulationManager = new SimulationManager();
    VehicleAnimation vehicleAnimator;

    boolean isRandom;
    boolean simulationIsCurrentlyPaused = false;

    @FXML private Pane mainPane;
    @FXML private Button random_select_button;
    @FXML private Button user_input_button;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button rerunButton;
    @FXML private Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;
    @FXML private VBox topVBox;

    private Direction currentDirectionForLabelUpdate;

    public void initialize() {
        vehicleAnimator = new VehicleAnimation(simulationManager); // VehicleAnimation başlatılır

        topVBox.setVisible(true);
        mainPane.setVisible(false);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
        rerunButton.setVisible(false);
        resetTimerLabels();

        isRandom = false;
        simulationIsCurrentlyPaused = false;
        pauseButton.setText("Pause");

        simulationManager.setOnTick(kalanSure -> {
            if (this.currentDirectionForLabelUpdate != null) {
                labeliGuncelle(this.currentDirectionForLabelUpdate, kalanSure);
            } else {
                resetTimerLabels();
            }
        });

        // onPhaseChange -> onPhaseInfoChange olarak SimulationManager'da değiştirilmişti, burada da güncelleyelim.
        simulationManager.setOnPhaseInfoChange(direction -> { // 'direction' burada Yeşil veya Sarı olan yön
            this.currentDirectionForLabelUpdate = direction;
            if (direction == null) { // Simülasyon durduysa veya hata varsa
                resetTimerLabels();
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
        resetTimerLabels();
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
        labelSureleriniGuncelle();

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
        pauseButton.setText("Pause");
        startButton.setDisable(true);

        if (isRandom) {
            simulationManager.startAutoMode();
        } else {
            simulationManager.startManualMode(trafficController.getVehicleCounts()); // Bu, trafficController'ı günceller
        }
        vehicleAnimator.initializeVehicles(trafficController, mainPane); // Güncel trafficController ile araçları oluştur
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
            pauseButton.setText("Pause");
            simulationIsCurrentlyPaused = false;
        } else {
            simulationManager.pauseSimulation();
            vehicleAnimator.stopAnimation(); // Animasyonu da duraklat (durdur)
            pauseButton.setText("Resume");
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

    private void labelSureleriniGuncelle() {
        if (trafficController == null) return;
        northTimerLabel.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");
        eastTimerLabel.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        southTimerLabel.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        westTimerLabel.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
    }

    private void labeliGuncelle(Direction aktifYon, int sure) {
        resetTimerLabels();
        if (aktifYon == null) return;

        // Işığın rengini de göstermek için (opsiyonel)
        // LightPhase phase = simulationManager.getLightPhaseForDirection(aktifYon);
        // String phaseText = " (" + phase.name().substring(0,1) + ")";

        String sureText = (sure >= 0 ? sure : "0") + " sn"; // + phaseText;
        switch (aktifYon) {
            case NORTH -> northTimerLabel.setText(sureText);
            case SOUTH -> southTimerLabel.setText(sureText);
            case EAST  -> eastTimerLabel.setText(sureText);
            case WEST  -> westTimerLabel.setText(sureText);
        }
    }

    private void resetTimerLabels() {
        northTimerLabel.setText("—");
        southTimerLabel.setText("—");
        eastTimerLabel.setText("—");
        westTimerLabel.setText("—");
    }
}