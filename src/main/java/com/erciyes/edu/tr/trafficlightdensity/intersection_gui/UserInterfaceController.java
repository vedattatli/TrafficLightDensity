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
    TrafficController trafficController;
    TimerDisplay timerDisplay;
    VehicleAnimation vehicleAnimator;

    public UserInterfaceController()
    {
    }


    boolean isRandom;
    boolean simulationIsCurrentlyPaused = false;

    @FXML private Pane mainPane;
    @FXML private Button random_select_button;
    @FXML private Button user_input_button;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button rerunButton;
    @FXML public Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;
    @FXML private VBox topVBox;
    @FXML public Label displayNorthCarCount,displayNorthGreenTime,displayNorthRedTime,displaySouthCarCount;
    @FXML public Label displaySouthGreenTime,displaySouthRedTime,displayEastCarCount,displayEastGreenTime;
    @FXML public Label displayEastRedTime,displayWestCarCount,displayWestGreenTime,displayWestRedTime;
    @FXML public Circle greenLightUp,greenLightRight,greenLightLeft,greenLightDown;
    @FXML public Circle yellowLightUp,yellowLightRight,yellowLightLeft,yellowLightDown;
    @FXML public Circle redLightUp,redLightRight,redLightLeft,redLightDown;


    @FXML
    public void initialize() { // FXML elements are available here
        simulationManager = new SimulationManager();
        simulationManager.setUserInterfaceController(this); // Pass this controller to SimManager

        vehicleAnimator = new VehicleAnimation(simulationManager);
        trafficController = simulationManager.getTrafficController(); // Get the TC instance from SimManager


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

        System.out.println("Random vehicle count mode selected.");
    }

    @FXML
    private void onUserInputSelect(ActionEvent e) {
        isRandom = false;
        simulationManager.getTrafficController().getVehicleCounts().clear();

        for (Direction yon : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            TextInputDialog dialog = new TextInputDialog("5");
            dialog.setTitle("Vehicle Input");
            dialog.setHeaderText("Number of vehicles for " + yon + " direction:");
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
        System.out.println("Manual vehicle count mode selected and data entered.");
    }

    @FXML
    private void onStartSimulation(ActionEvent e) {
        System.out.println("Starting simulation...");
        simulationIsCurrentlyPaused = false;
        if (startButton != null) startButton.setDisable(true);


        if (isRandom) {
            simulationManager.startAutoMode();
        } else {
            simulationManager.startManualMode(simulationManager.getTrafficController().getVehicleCounts());
        }
        vehicleAnimator.initializeVehicles(simulationManager.getTrafficController(), mainPane);
        vehicleAnimator.startAnimation();
    }

    @FXML
    private void onPauseSimulation(ActionEvent e) {
        if (!simulationManager.isRunning()) {

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
        System.out.println("Resetting simulation...");
        simulationManager.stopSimulation();
        vehicleAnimator.clearAllVehicles();
        initialize();
    }
}