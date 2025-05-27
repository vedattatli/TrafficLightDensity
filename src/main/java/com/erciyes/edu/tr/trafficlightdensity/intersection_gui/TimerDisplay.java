package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase; // Import LightPhase

import java.util.Map; // Import Map

//Işık fazlarının geri sayımını sayısal olarak gösterir.
public class TimerDisplay {
    private final UserInterfaceController userInterfaceController;
    private final SimulationManager simulationManager; // Keep a reference


    // private Direction currentDirectionForLabelUpdate; // No longer needed this way

    public TimerDisplay(UserInterfaceController userInterfaceController, SimulationManager simulationManager) {
        this.userInterfaceController = userInterfaceController;
        this.simulationManager = simulationManager; // Store simulationManager

        // The old onTick and onPhaseInfoChange are replaced by a single callback
        // that provides all necessary info for all timers.
        simulationManager.setOnTick(remainingTimesMap -> {
            // This map contains the time left for each direction
            // If a direction is GREEN or YELLOW, it's the time left in that phase.
            // If a direction is RED, it's the time until it turns GREEN/YELLOW again.
            updateAllTimerLabels(remainingTimesMap);
        });

        // No longer needed as onTick handles all updates
        // simulationManager.setOnPhaseInfoChange(direction -> {
        //     this.currentDirectionForLabelUpdate = direction;
        //     if (direction == null) {
        //         resetTimerLabels();
        //     }
        // });
    }

    public void labelTimerBaslangic(TrafficController trafficController) {
        if (trafficController == null) return;
        // This method might be less relevant now as timers are fully dynamic.
        // It could still show initial calculated green times before simulation starts.
        updateLabel(userInterfaceController.northTimerLabel, trafficController.getGreenDuration(Direction.NORTH));
        updateLabel(userInterfaceController.eastTimerLabel, trafficController.getGreenDuration(Direction.EAST));
        updateLabel(userInterfaceController.southTimerLabel, trafficController.getGreenDuration(Direction.SOUTH));
        updateLabel(userInterfaceController.westTimerLabel, trafficController.getGreenDuration(Direction.WEST));
    }

    public void resetTimerLabels() {
        String resetText = "0s"; // Use "0s" or "N/A" when reset or stopped
        userInterfaceController.northTimerLabel.setText(resetText);
        userInterfaceController.southTimerLabel.setText(resetText);
        userInterfaceController.eastTimerLabel.setText(resetText);
        userInterfaceController.westTimerLabel.setText(resetText);
    }

    private void updateAllTimerLabels(Map<Direction, Integer> remainingTimesMap) {
        if (remainingTimesMap == null) {
            resetTimerLabels();
            return;
        }
        updateLabel(userInterfaceController.northTimerLabel, remainingTimesMap.getOrDefault(Direction.NORTH, 0));
        updateLabel(userInterfaceController.southTimerLabel, remainingTimesMap.getOrDefault(Direction.SOUTH, 0));
        updateLabel(userInterfaceController.eastTimerLabel, remainingTimesMap.getOrDefault(Direction.EAST, 0));
        updateLabel(userInterfaceController.westTimerLabel, remainingTimesMap.getOrDefault(Direction.WEST, 0));
    }


    private void updateLabel(javafx.scene.control.Label label, int timeSeconds) {
        if (timeSeconds < 0) timeSeconds = 0; // Ensure non-negative
        label.setText(timeSeconds + "s");
    }


    public void labelDisplayBaslangic(TrafficController trafficController) {
        userInterfaceController.displayEastGreenTime.setText(trafficController.getGreenDuration(Direction.EAST) + "s");
        userInterfaceController.displayWestGreenTime.setText(trafficController.getGreenDuration(Direction.WEST) + "s");
        userInterfaceController.displaySouthGreenTime.setText(trafficController.getGreenDuration(Direction.SOUTH) + "s");
        userInterfaceController.displayNorthGreenTime.setText(trafficController.getGreenDuration(Direction.NORTH) + "s");

        userInterfaceController.displayNorthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().getOrDefault(Direction.NORTH, 0)));
        userInterfaceController.displaySouthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().getOrDefault(Direction.SOUTH, 0)));
        userInterfaceController.displayWestCarCount.setText(String.valueOf(trafficController.getVehicleCounts().getOrDefault(Direction.WEST, 0)));
        userInterfaceController.displayEastCarCount.setText(String.valueOf(trafficController.getVehicleCounts().getOrDefault(Direction.EAST, 0)));

        userInterfaceController.displayEastRedTime.setText(calculateRedDuration(Direction.EAST, trafficController) + "s");
        userInterfaceController.displayWestRedTime.setText(calculateRedDuration(Direction.WEST, trafficController) + "s");
        userInterfaceController.displayNorthRedTime.setText(calculateRedDuration(Direction.NORTH, trafficController) + "s");
        userInterfaceController.displaySouthRedTime.setText(calculateRedDuration(Direction.SOUTH, trafficController) + "s");
    }

    public void resetLabelDisplay() {
        String resetText = "--";
        userInterfaceController.displayEastGreenTime.setText(resetText);
        userInterfaceController.displayWestGreenTime.setText(resetText);
        userInterfaceController.displaySouthGreenTime.setText(resetText);
        userInterfaceController.displayNorthGreenTime.setText(resetText);

        userInterfaceController.displayNorthCarCount.setText(resetText);
        userInterfaceController.displaySouthCarCount.setText(resetText);
        userInterfaceController.displayWestCarCount.setText(resetText);
        userInterfaceController.displayEastCarCount.setText(resetText);

        userInterfaceController.displayEastRedTime.setText(resetText);
        userInterfaceController.displayWestRedTime.setText(resetText);
        userInterfaceController.displaySouthRedTime.setText(resetText);
        userInterfaceController.displayNorthRedTime.setText(resetText);
    }

    // This calculates the total red duration for a direction in a cycle,
    // which is still useful for the summary display table.
    public String calculateRedDuration(Direction direction, TrafficController tc) {
        int totalRedTime = 0;
        if (tc == null || tc.getVehicleCounts().isEmpty()) return "0";

        for(Direction d : Direction.values()){
            if(d != direction){
                totalRedTime += tc.getGreenDuration(d);
                totalRedTime += TrafficController.YELLOW_DURATION;
            }
        }
        // Add the yellow phase of the direction itself, as it's red during its own yellow before others go.
        // No, this is not correct for "total red". Total red for a direction is sum of G+Y of OTHERS.
        return String.valueOf(totalRedTime);
    }


    // This method is no longer called directly by SimulationManager's onTick.
    // updateAllTimerLabels is used instead.
    public void labelTrafficLightPerSecond(Direction aktifYon, int sure) {
        // This was the old way, keeping it for reference or if needed by other parts,
        // but onTick callback to updateAllTimerLabels is the new primary mechanism.
        // resetTimerLabels(); // Resetting all might cause flickering if only one is changing
        if (aktifYon == null) {
            resetTimerLabels(); // If no active direction (e.g. sim stopped), reset all.
            return;
        }

        String sureText = (sure >= 0 ? sure : "0") + "s"; // use "s"
        switch (aktifYon) {
            case NORTH -> userInterfaceController.northTimerLabel.setText(sureText);
            case SOUTH -> userInterfaceController.southTimerLabel.setText(sureText);
            case EAST  -> userInterfaceController.eastTimerLabel.setText(sureText);
            case WEST  -> userInterfaceController.westTimerLabel.setText(sureText);
        }
    }
}