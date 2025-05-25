package com.erciyes.edu.tr.trafficlightdensity.road_objects;
import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.UserInterfaceController;
import javafx.scene.paint.Color;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

public enum LightPhase {

    GREEN,
    YELLOW,
    RED;

    public Duration getDuration() {
        return getDuration();
    }

    public static Duration getDefaultPhaseDuration(LightPhase phase) {
        return switch (phase) {
            case YELLOW -> Duration.ofSeconds(3); // sabit
            case RED, GREEN -> Duration.ofSeconds(0); // dışarıdan atanacak
        };
    }
}
