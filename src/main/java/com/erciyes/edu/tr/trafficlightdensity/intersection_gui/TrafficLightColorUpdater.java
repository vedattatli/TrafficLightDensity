package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import javafx.scene.paint.Color;

import java.util.Map;

import static com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase.GREEN;
import static com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase.RED;
import static com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase.YELLOW;

public class TrafficLightColorUpdater {

    private final Color defaultGreenColor  = Color.web("#074c0a");
    private final Color defaultYellowColor = Color.web("#66790a");
    private final Color defaultRedColor    = Color.web("#551111");

    private final Color activeGreenColor   = Color.web("#21ed39");
    private final Color activeYellowColor  = Color.web("#e0ff0d");
    private final Color activeRedColor     = Color.web("#ff0000");

    /** Tüm lambaları varsayılan renklere döndürür. */
    public void resetTrafficLightsColors(UserInterfaceController ui) {
        ui.yellowLightUp.setFill(defaultYellowColor);
        ui.yellowLightDown.setFill(defaultYellowColor);
        ui.yellowLightRight.setFill(defaultYellowColor);
        ui.yellowLightLeft.setFill(defaultYellowColor);

        ui.redLightDown.setFill(defaultRedColor);
        ui.redLightRight.setFill(defaultRedColor);
        ui.redLightLeft.setFill(defaultRedColor);
        ui.redLightUp.setFill(defaultRedColor);

        ui.greenLightDown.setFill(defaultGreenColor);
        ui.greenLightUp.setFill(defaultGreenColor);
        ui.greenLightLeft.setFill(defaultGreenColor);
        ui.greenLightRight.setFill(defaultGreenColor);
    }

    /** Haritadaki HER yön–faz ikilisini işleyerek aktif renkleri uygular. */
    public void updateTrafficLightsColors(Map<Direction, LightPhase> currentLightPhase,
                                          UserInterfaceController ui) {
        // Önce hepsini sıfırla
        resetTrafficLightsColors(ui);

        // Ardından mevcut fazlara göre ilgili ışığı boyayalım
        for (Map.Entry<Direction, LightPhase> entry : currentLightPhase.entrySet()) {
            Direction direction = entry.getKey();
            LightPhase phase   = entry.getValue();

            switch (direction) {
                case NORTH -> {
                    if (phase == GREEN)   ui.greenLightDown.setFill(activeGreenColor);
                    else if (phase == YELLOW) ui.yellowLightDown.setFill(activeYellowColor);
                    else if (phase == RED)    ui.redLightDown.setFill(activeRedColor);
                }
                case EAST -> {
                    if (phase == GREEN)   ui.greenLightLeft.setFill(activeGreenColor);
                    else if (phase == YELLOW) ui.yellowLightLeft.setFill(activeYellowColor);
                    else if (phase == RED)    ui.redLightLeft.setFill(activeRedColor);
                }
                case WEST -> {
                    if (phase == GREEN)   ui.greenLightRight.setFill(activeGreenColor);
                    else if (phase == YELLOW) ui.yellowLightRight.setFill(activeYellowColor);
                    else if (phase == RED)    ui.redLightRight.setFill(activeRedColor);
                }
                case SOUTH -> {
                    if (phase == GREEN)   ui.greenLightUp.setFill(activeGreenColor);
                    else if (phase == YELLOW) ui.yellowLightUp.setFill(activeYellowColor);
                    else if (phase == RED)    ui.redLightUp.setFill(activeRedColor);
                }
            }
        }
    }
}
