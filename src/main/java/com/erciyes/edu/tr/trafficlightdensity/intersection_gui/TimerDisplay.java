package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

//Işık fazlarının geri sayımını sayısal olarak gösterir.
public class TimerDisplay {
    private final UserInterfaceController userInterfaceController;


    private Direction currentDirectionForLabelUpdate;

    public TimerDisplay(UserInterfaceController userInterfaceController, SimulationManager simulationManager) {
        this.userInterfaceController = userInterfaceController;

        simulationManager.setOnTick(kalanSure -> {
            if (this.currentDirectionForLabelUpdate != null) {
                labelTrafficLightPerSecond(this.currentDirectionForLabelUpdate, kalanSure);
            } else {
                resetTimerLabels();
            }
        });

        simulationManager.setOnPhaseInfoChange(direction -> {
            this.currentDirectionForLabelUpdate = direction;
            if (direction == null) { // Simülasyon durduysa veya hata varsa
                resetTimerLabels();
            }
        });
    }

    public void labelTimerBaslangic(TrafficController trafficController) {

        if (trafficController == null) return;
        userInterfaceController.northTimerLabel.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");
        userInterfaceController.eastTimerLabel.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        userInterfaceController.southTimerLabel.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        userInterfaceController.westTimerLabel.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
    }

    public void resetTimerLabels() {
        userInterfaceController.northTimerLabel.setText("—");
        userInterfaceController.southTimerLabel.setText("—");
        userInterfaceController.eastTimerLabel.setText("—");
        userInterfaceController.westTimerLabel.setText("—");


    }

    public void labelDisplayBaslangic(TrafficController trafficController) {
        userInterfaceController.displayEastGreenTime.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        userInterfaceController.displayWestGreenTime.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
        userInterfaceController.displaySouthGreenTime.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        userInterfaceController.displayNorthGreenTime.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");

        userInterfaceController.displayNorthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.NORTH)));
        userInterfaceController.displaySouthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.SOUTH)));
        userInterfaceController.displayWestCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.WEST)));
        userInterfaceController.displayEastCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.EAST)));

        userInterfaceController.displayEastRedTime.setText(calculateRedDuration(Direction.EAST, trafficController));
        userInterfaceController.displayWestRedTime.setText(calculateRedDuration(Direction.WEST, trafficController));
        userInterfaceController.displayNorthRedTime.setText(calculateRedDuration(Direction.NORTH, trafficController));
        userInterfaceController.displaySouthRedTime.setText(calculateRedDuration(Direction.SOUTH, trafficController));
    }

    public void resetLabelDisplay() {
        userInterfaceController.displayEastGreenTime.setText("--");
        userInterfaceController.displayWestGreenTime.setText("--");
        userInterfaceController.displaySouthGreenTime.setText("--");
        userInterfaceController.displayNorthGreenTime.setText("--");

        userInterfaceController.displayNorthCarCount.setText("--");
        userInterfaceController.displaySouthCarCount.setText("--");
        userInterfaceController.displayWestCarCount.setText("--");
        userInterfaceController.displayEastCarCount.setText("--");

        userInterfaceController.displayEastRedTime.setText("--");
        userInterfaceController.displayWestRedTime.setText("--");
        userInterfaceController.displaySouthRedTime.setText("--");
        userInterfaceController.displayNorthRedTime.setText("--");

    }

    public String calculateRedDuration(Direction direction, TrafficController trafficController) {

        int westGreenDuration = trafficController.getGreenDuration(Direction.WEST);
        int eastGreenDuration = trafficController.getGreenDuration(Direction.EAST);
        int southGreenDuration = trafficController.getGreenDuration(Direction.SOUTH);
        int northGreenDuration = trafficController.getGreenDuration(Direction.NORTH);

        int yellowDuration = TrafficController.YELLOW_DURATION;
        int totalCycleTime = TrafficController.TOTAL_CYCLE_TIME;
        String northRedDuration = String.valueOf(totalCycleTime - northGreenDuration -  yellowDuration);
        String eastRedDuration = String.valueOf(totalCycleTime - eastGreenDuration - yellowDuration);
        String southRedDuration = String.valueOf(totalCycleTime - southGreenDuration - yellowDuration);
        String westRedDuration = String.valueOf(totalCycleTime - westGreenDuration - yellowDuration);

        if (direction.equals(Direction.NORTH)) return northRedDuration;
        else if (direction.equals(Direction.SOUTH)) return southRedDuration;
        else if (direction.equals(Direction.EAST)) return eastRedDuration;
        else if (direction.equals(Direction.WEST)) return  westRedDuration;
        return ("0");
    }

    public void labelTrafficLightPerSecond(Direction aktifYon, int sure) {
        resetTimerLabels();
        if (aktifYon == null) return;

        String sureText = (sure >= 0 ? sure : "0") + " sn";
        switch (aktifYon) {
            case NORTH -> {
                userInterfaceController.northTimerLabel.setText(sureText);
            }
            case SOUTH -> {
                userInterfaceController.southTimerLabel.setText(sureText);
            }
            case EAST -> {
                userInterfaceController.eastTimerLabel.setText(sureText);
            }
            case WEST -> {
                userInterfaceController.westTimerLabel.setText(sureText);
            }
        }
    }
}
