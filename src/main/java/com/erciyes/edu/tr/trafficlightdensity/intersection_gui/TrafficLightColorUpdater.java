package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.Map;

import static com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase.*;

public class TrafficLightColorUpdater {

    private final Color defaultGreenColor = Color.web("#046509");
    private final Color defaultYellowColor = Color.web("#8fa612");
    private final Color defaultRedColor = Color.web("#801414");

    private final  Color activeGreenColor = Color.web("#21ed39");
    private final  Color activeYellowColor = Color.web("#e0ff0d");
    private final  Color activeRedColor = Color.web("#ff0000");

    public void resetTrafficLightsColors(UserInterfaceController userInterfaceController)
    {
        userInterfaceController.yellowLightUp.setFill(defaultYellowColor);
        userInterfaceController.yellowLightDown.setFill(defaultYellowColor);
        userInterfaceController.yellowLightRight.setFill(defaultYellowColor);
        userInterfaceController.yellowLightLeft.setFill(defaultYellowColor);

        userInterfaceController.redLightDown.setFill(defaultRedColor);
        userInterfaceController.redLightRight.setFill(defaultRedColor);
        userInterfaceController.redLightLeft.setFill(defaultRedColor);
        userInterfaceController.redLightUp.setFill(defaultRedColor);

        userInterfaceController.greenLightDown.setFill(defaultGreenColor);
        userInterfaceController.greenLightUp.setFill(defaultGreenColor);
        userInterfaceController.greenLightLeft.setFill(defaultGreenColor);
        userInterfaceController.greenLightRight.setFill(defaultGreenColor);
    }
    public void updateTrafficLightsColors(Map<Direction, LightPhase> currentLightPhase, UserInterfaceController userInterfaceController) {
        Iterator<Map.Entry<Direction, LightPhase>> iterator = currentLightPhase.entrySet().iterator();

        if (iterator.hasNext()) {
            Map.Entry<Direction, LightPhase> entry = iterator.next();
            Direction direction = entry.getKey();
            LightPhase phase = entry.getValue();

            switch (direction) {
                case NORTH -> {
                    if (phase == GREEN) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.greenLightUp.setFill(activeGreenColor);
                    } else if (phase == YELLOW) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.yellowLightUp.setFill(activeYellowColor);
                    } else if (phase == RED) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.redLightUp.setFill(activeRedColor);
                    }
                }
                case EAST -> {
                    if (phase == GREEN) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.greenLightRight.setFill(activeGreenColor);
                    } else if (phase == YELLOW) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.yellowLightRight.setFill(activeYellowColor);
                    } else if (phase == RED) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.redLightRight.setFill(activeRedColor);
                    }
                }
                case WEST -> {
                    if (phase == GREEN) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.greenLightLeft.setFill(activeGreenColor);
                    } else if (phase == YELLOW) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.yellowLightLeft.setFill(activeYellowColor);
                    } else if (phase == RED) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.redLightLeft.setFill(activeRedColor);
                    }
                }
                case SOUTH -> {
                    if (phase == GREEN) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.greenLightDown.setFill(activeGreenColor);
                    } else if (phase == YELLOW) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.yellowLightDown.setFill(activeYellowColor);
                    } else if (phase == RED) {
                        resetTrafficLightsColors(userInterfaceController);
                        userInterfaceController.redLightDown.setFill(activeRedColor);
                    }
                }

            }
        }
    }
}
