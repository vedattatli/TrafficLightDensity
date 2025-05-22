package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.HashMap;
import java.util.Map;

//Sensörlerden gelen yoğunluk verisiyle her yöne yeşil süresi hesaplar.
public class TrafficController {

    private final int TOTAL_CYCLE_TIME=120;
    private final int YELLOW_DURATION=3;

    Map<Direction,Integer> vehicleCount;
    HashMap<Direction,Integer> greenDurations;

    private int totalVehicleCount = 0;


    public int getGreenDuration(Direction direction)
    {
        return greenDurations.getOrDefault(direction,0);
    }

    public void setVehicleCounts(Map<Direction, Integer> vehicleCount) {
        this.vehicleCount = vehicleCount;
    }


    private int calculateTotalVehicleCount()
    {
        for(int count : vehicleCount.values())
        {
            totalVehicleCount += count;
        }
        return totalVehicleCount;
    }

    private void calculateGreenDurations() {
        greenDurations = new HashMap<>();

        if (totalVehicleCount == 0) {
            for (Direction direction : vehicleCount.keySet()) {
                greenDurations.put(direction, 0); // hiç yeşil yanmasın
            }
            return;
        }

        for (Direction direction : vehicleCount.keySet()) {
            int count = vehicleCount.get(direction);
            int duration = (int) ((count / (double) totalVehicleCount) * (TOTAL_CYCLE_TIME - 4 * YELLOW_DURATION));
            greenDurations.put(direction, duration);
        }
    }


    public void updateDurations()
    {
        calculateTotalVehicleCount();
        calculateGreenDurations();
    }

}