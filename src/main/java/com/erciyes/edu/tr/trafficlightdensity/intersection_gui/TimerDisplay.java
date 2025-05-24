package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

//Işık fazlarının geri sayımını sayısal olarak gösterir.
public class TimerDisplay {
    private final UserInterfaceController userInterfaceController;


    public TimerDisplay(UserInterfaceController userInterfaceController)
    {
        this.userInterfaceController=userInterfaceController;
    }

    public void labeliGuncelle(Direction aktifYon, int sure) {
       resetTimerLabels();
        if (aktifYon == null) return;

        String sureText = (sure >= 0 ? sure : "0") + " sn"; // + phaseText;
        switch (aktifYon) {
            case NORTH ->
                    {
                        userInterfaceController.northTimerLabel.setText(sureText);

                    }
            case SOUTH ->
                    {
                        userInterfaceController.southTimerLabel.setText(sureText);

                    }
            case EAST  ->
                    {
                        userInterfaceController.eastTimerLabel.setText(sureText);

                    }
            case WEST  ->
                    {
                        userInterfaceController.westTimerLabel.setText(sureText);

                    }
        }
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

    public void labelDisplayBaslangic(TrafficController trafficController)
    {
        userInterfaceController.displayEastGreenTime.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        userInterfaceController.displayWestGreenTime.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
        userInterfaceController.displaySouthGreenTime.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        userInterfaceController.displayNorthGreenTime.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");

        userInterfaceController.displayNorthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.NORTH)));
        userInterfaceController.displaySouthCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.SOUTH)));
        userInterfaceController.displayWestCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.WEST)));
        userInterfaceController.displayEastCarCount.setText(String.valueOf(trafficController.getVehicleCounts().get(Direction.EAST)));
    }

    public void resetLabelDisplay()
    {
        userInterfaceController.displayEastGreenTime.setText("--");
        userInterfaceController.displayWestGreenTime.setText("--");
        userInterfaceController.displaySouthGreenTime.setText("--");
        userInterfaceController.displayNorthGreenTime.setText("--");

        userInterfaceController.displayNorthCarCount.setText("--");
        userInterfaceController.displaySouthCarCount.setText("--");
        userInterfaceController.displayWestCarCount.setText("--");
        userInterfaceController.displayEastCarCount.setText("--");

    }
}