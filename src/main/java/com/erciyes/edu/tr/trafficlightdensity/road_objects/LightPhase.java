package com.erciyes.edu.tr.trafficlightdensity.road_objects;
import javafx.scene.paint.Color;

import java.time.Duration;

public enum LightPhase {

    GREEN,
    YELLOW,
    RED;


    LightPhase next() {
        return switch (this) {
            case GREEN -> YELLOW;
            case YELLOW -> RED;
            case RED -> GREEN;
        };
    }
    private boolean isGreen ()
    {
        return this == GREEN;
    }
    private boolean isRed ()
    {
        return this == RED;
    }
    private boolean isYellow ()
    {
        return this == YELLOW;
    }

    public void updateTrafficLightsColor(LightPhase lightPhase)
    {
        if (isGreen())
        {

        }
        else if (isRed())
        {

        } else if (isYellow())
        {


        }
    }
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
