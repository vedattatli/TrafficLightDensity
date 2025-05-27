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
import javafx.scene.shape.Circle;

import java.util.*;

public class UserInterfaceController {

    SimulationManager simulationManager;
    TrafficController trafficController; // Keep this for initial display logic
    TimerDisplay timerDisplay;
    VehicleAnimation vehicleAnimator;

    // Constructor for direct instantiation if needed (e.g. from FXML if no-arg is required by FXMLLoader)
    // However, your current setup seems to initialize it in initialize() or pass it.
    // For FXML, a no-arg constructor is standard.
    public UserInterfaceController()
    {
        // Initialization of members will happen in initialize() if they depend on FXML elements
        // or other logic executed after FXML loading.
    }


    boolean isRandom;
    boolean simulationIsCurrentlyPaused = false;

    @FXML private Pane mainPane;
    //    @FXML private Button random_select_button; // Assuming these are in FXML
//    @FXML private Button user_input_button;  // Assuming these are in FXML
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button rerunButton;
    @FXML public Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;
    @FXML private VBox topVBox; // Make sure this is correctly injected if used
    @FXML public Label displayNorthCarCount,displayNorthGreenTime,displayNorthRedTime,displaySouthCarCount;
    @FXML public Label displaySouthGreenTime,displaySouthRedTime,displayEastCarCount,displayEastGreenTime;
    @FXML public Label displayEastRedTime,displayWestCarCount,displayWestGreenTime,displayWestRedTime;
    @FXML public Circle greenLightUp,greenLightRight,greenLightLeft,greenLightDown;
    @FXML public Circle yellowLightUp,yellowLightRight,yellowLightLeft,yellowLightDown;
    @FXML public Circle redLightUp,redLightRight,redLightLeft,redLightDown;


    // private Direction currentDirectionForLabelUpdate; // No longer needed here

    @FXML
    public void initialize() { // FXML elements are available here
        simulationManager = new SimulationManager();
        simulationManager.setUserInterfaceController(this); // Pass this controller to SimManager

        vehicleAnimator = new VehicleAnimation(simulationManager);
        trafficController = simulationManager.getTrafficController(); // Get the TC instance from SimManager
        // to ensure consistency

        this.timerDisplay = new TimerDisplay(this, simulationManager); // Pass this and SimManager
        simulationManager.setTimerDisplay(this.timerDisplay); // Link back if SimManager needs it explicitly

        if (topVBox != null) topVBox.setVisible(true); // Check for null if FXML might not have it
        if (mainPane != null) mainPane.setVisible(false);
        if (startButton != null) startButton.setVisible(false);
        if (pauseButton != null) pauseButton.setVisible(false);
        if (rerunButton != null) rerunButton.setVisible(false);

        timerDisplay.resetTimerLabels();    // Reset visual timers
        timerDisplay.resetLabelDisplay();   // Reset info display table

        isRandom = false;
        simulationIsCurrentlyPaused = false;
        if(pauseButton!=null) pauseButton.setText("Pause");
    }

    @FXML
    private void onRandomSelect(ActionEvent e) {
        isRandom = true;
        if (topVBox != null) topVBox.setVisible(false);
        if (mainPane != null) mainPane.setVisible(true);
        if (startButton != null) { startButton.setVisible(true); startButton.setDisable(false); }
        if (pauseButton != null) pauseButton.setVisible(true);
        if (rerunButton != null) rerunButton.setVisible(true);

        // System.out.println("Rastgele araç sayısı modu seçildi.");
        System.out.println("Random vehicle count mode selected.");
    }

    @FXML
    private void onUserInputSelect(ActionEvent e) {
        isRandom = false;
        // trafficController field is already linked to simulationManager's instance.
        // We should modify the counts on that shared instance.
        simulationManager.getTrafficController().getVehicleCounts().clear(); // Clear counts on the shared TC

        for (Direction yon : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            TextInputDialog dialog = new TextInputDialog("5");
            // dialog.setTitle("Araç Girişi");
            dialog.setTitle("Vehicle Input");
            // dialog.setHeaderText(yon + " yönü için araç sayısı:");
            dialog.setHeaderText("Number of vehicles for " + yon + " direction:");
            // dialog.setContentText("Sayı:");
            dialog.setContentText("Count:");

            Optional<String> sonuc = dialog.showAndWait();
            int count = 0;
            if (sonuc.isPresent() && !sonuc.get().trim().isEmpty()) {
                try {
                    count = Integer.parseInt(sonuc.get().trim());
                    count = Math.max(0, count); // Ensure non-negative
                } catch (NumberFormatException ex) {
                    count = 0; // Default to 0 on parse error
                }
            }
            simulationManager.getTrafficController().getVehicleCounts().put(yon, count);
        }
        simulationManager.getTrafficController().updateDurations(); // Recalculate based on new counts
        timerDisplay.labelTimerBaslangic(simulationManager.getTrafficController()); // Update initial timer display
        timerDisplay.labelDisplayBaslangic(simulationManager.getTrafficController()); // Update info table

        if (topVBox != null) topVBox.setVisible(false);
        if (mainPane != null) mainPane.setVisible(true);
        if (startButton != null) { startButton.setVisible(true); startButton.setDisable(false); }
        if (pauseButton != null) pauseButton.setVisible(true);
        if (rerunButton != null) rerunButton.setVisible(true);

        // System.out.println("Manuel araç sayısı modu seçildi ve veriler girildi.");
        System.out.println("Manual vehicle count mode selected and data entered.");
    }

    @FXML
    private void onStartSimulation(ActionEvent e) {
        // System.out.println("Simülasyon başlatılıyor...");
        System.out.println("Starting simulation...");
        simulationIsCurrentlyPaused = false;
        if (startButton != null) startButton.setDisable(true);


        if (isRandom) {
            simulationManager.startAutoMode(); // This will generate counts and update TC
        } else {
            // For manual mode, counts are already set in onUserInputSelect
            // and TC in simulationManager is already updated.
            // We just need to ensure simulationManager uses its current TC.
            simulationManager.startManualMode(simulationManager.getTrafficController().getVehicleCounts());
        }
        // simulationManager.getTrafficController() now has the correct counts and durations.
        vehicleAnimator.initializeVehicles(simulationManager.getTrafficController(), mainPane);
        vehicleAnimator.startAnimation();
    }

    @FXML
    private void onPauseSimulation(ActionEvent e) {
        if (!simulationManager.isRunning()) {
            // System.out.println("Pause: Simülasyon çalışmıyor.");
            System.out.println("Pause: Simulation not running.");
            return;
        }

        if (simulationIsCurrentlyPaused) {
            simulationManager.resumeSimulation();
            vehicleAnimator.startAnimation();
            if (pauseButton != null) pauseButton.setText("Pause");
            simulationIsCurrentlyPaused = false;
        } else {
            simulationManager.pauseSimulation();
            vehicleAnimator.stopAnimation();
            if (pauseButton != null) pauseButton.setText("Resume");
            simulationIsCurrentlyPaused = true;
        }
    }

    @FXML
    private void onRerunSimulation(ActionEvent e) {
        // System.out.println("Simülasyon sıfırlanıyor (Reset)...");
        System.out.println("Resetting simulation...");
        simulationManager.stopSimulation();
        vehicleAnimator.clearAllVehicles();

        // trafficController instance is managed by simulationManager,
        // re-initializing UI controller will set up a new simulationManager.
        // Or, more cleanly, reset the state of the existing simulationManager and TC.
        // For simplicity of this example, re-initialize() re-creates SimulationManager.
        // A better approach might be simManager.reset() if such a method exists.

        // The initialize method re-creates SimulationManager and its TrafficController.
        // So, vehicle counts will be empty or default.
        initialize(); // This brings UI to initial state (selection screen)
    }
}