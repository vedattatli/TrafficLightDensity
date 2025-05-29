package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.Map;


public class TimerDisplay {
    private final UserInterfaceController userInterfaceController;
    private final SimulationManager simulationManager;




    public TimerDisplay(UserInterfaceController userInterfaceController, SimulationManager simulationManager) {
        this.userInterfaceController = userInterfaceController;
        this.simulationManager = simulationManager;

        simulationManager.setOnTick(remainingTimesMap -> {
            updateAllTimerLabels(remainingTimesMap);
        });
    }

    public void labelTimerBaslangic(TrafficController trafficController) {
        if (trafficController == null) return;
        updateLabel(userInterfaceController.northTimerLabel, trafficController.getGreenDuration(Direction.NORTH));
        updateLabel(userInterfaceController.eastTimerLabel, trafficController.getGreenDuration(Direction.EAST));
        updateLabel(userInterfaceController.southTimerLabel, trafficController.getGreenDuration(Direction.SOUTH));
        updateLabel(userInterfaceController.westTimerLabel, trafficController.getGreenDuration(Direction.WEST));
    }

    public void resetTimerLabels() {
        String resetText = "0s";
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
        if (timeSeconds < 0) timeSeconds = 0;
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


    public String calculateRedDuration(Direction direction, TrafficController tc) {
        int totalRedTime = 0;
        if (tc == null || tc.getVehicleCounts().isEmpty()) return "0";

        for(Direction d : Direction.values()){
            if(d != direction){
                totalRedTime += tc.getGreenDuration(d);
                totalRedTime += TrafficController.YELLOW_DURATION;
            }
        }

        return String.valueOf(totalRedTime);
    }
}