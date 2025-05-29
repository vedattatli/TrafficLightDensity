package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Sensörlerden gelen yoğunluk verisiyle her yöne yeşil süresi hesaplar.
public class TrafficController {

    public final static int TOTAL_CYCLE_TIME = 120;
    public final static int YELLOW_DURATION = 3;
    public final static int MIN_GREEN_DURATION_IF_CARS = 10; // Araç varsa minimum yeşil süre
    public final static int MAX_GREEN_DURATION = 60;         // Maksimum yeşil süre

    Map<Direction, Integer> vehicleCount;
    HashMap<Direction, Integer> greenDurations;

    private int totalVehicleCount;


    public TrafficController() {
        this.vehicleCount = new HashMap<>();
        this.greenDurations = new HashMap<>();
    }

    public int getGreenDuration(Direction direction) {
        return greenDurations.getOrDefault(direction, 0);
    }

    public void setVehicleCounts(Map<Direction, Integer> vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public Map<Direction, Integer> getVehicleCounts() {
        return vehicleCount;
    }

    public int calculateTotalVehicleCount() {
        totalVehicleCount = 0;
        if (vehicleCount != null) {
            for (int count : vehicleCount.values()) {
                totalVehicleCount += count;
            }
        }
        return totalVehicleCount;
    }

    private void calculateGreenDurations() {
        greenDurations = new HashMap<>();
        Map<Direction, Integer> tempGreenDurations = new LinkedHashMap<>(); // Sırayı korumak için

        if (vehicleCount == null || vehicleCount.isEmpty() || totalVehicleCount == 0) {
            for (Direction direction : Direction.values()) {
                tempGreenDurations.put(direction, 0); // Araç yoksa veya toplam araç 0 ise tümüne 0
            }
            this.greenDurations.putAll(tempGreenDurations);
            return;
        }

        double availableGreenTimePool = TOTAL_CYCLE_TIME - (Direction.values().length * YELLOW_DURATION);
        if (availableGreenTimePool < 0) availableGreenTimePool = 0;

        long sumOfProportionalGreenTimes = 0;
        for (Direction dir : Direction.values()) {
            int count = vehicleCount.getOrDefault(dir, 0);
            if (count > 0) {
                double proportion = (double) count / totalVehicleCount;
                int duration = (int) Math.round(proportion * availableGreenTimePool);
                tempGreenDurations.put(dir, duration);
                sumOfProportionalGreenTimes += duration;
            } else {
                tempGreenDurations.put(dir, 0);
            }
        }


        if (sumOfProportionalGreenTimes > 0 && sumOfProportionalGreenTimes != availableGreenTimePool) {
            for (Direction dir : tempGreenDurations.keySet()) {
                if (tempGreenDurations.get(dir) > 0) { // Only adjust if it had some time
                    tempGreenDurations.put(dir, (int) Math.round(tempGreenDurations.get(dir) * (availableGreenTimePool / sumOfProportionalGreenTimes)));
                }
            }
        }



        List<Direction> directionsWithCars = new ArrayList<>();
        for(Direction dir : Direction.values()){
            if(vehicleCount.getOrDefault(dir,0) > 0){
                directionsWithCars.add(dir);
            } else {
                greenDurations.put(dir, 0); // No cars, 0 green time
            }
        }

        double remainingTimeAfterMins = availableGreenTimePool;
        for(Direction dir : directionsWithCars){
            tempGreenDurations.put(dir, Math.max(MIN_GREEN_DURATION_IF_CARS, tempGreenDurations.getOrDefault(dir,0)));
            tempGreenDurations.put(dir, Math.min(MAX_GREEN_DURATION, tempGreenDurations.get(dir)));
            remainingTimeAfterMins -= tempGreenDurations.get(dir); // This logic needs refinement
        }



        greenDurations.clear();
        double timeUsedByMinsAndZeros = 0;
        int lanesRequiringMinTime = 0;

        for (Direction dir : Direction.values()) {
            if (vehicleCount.getOrDefault(dir, 0) > 0) {
                greenDurations.put(dir, MIN_GREEN_DURATION_IF_CARS);
                timeUsedByMinsAndZeros += MIN_GREEN_DURATION_IF_CARS;
                lanesRequiringMinTime++;
            } else {
                greenDurations.put(dir, 0);
            }
        }

        double remainingPoolForProportional = availableGreenTimePool - timeUsedByMinsAndZeros;

        if (remainingPoolForProportional > 0 && lanesRequiringMinTime > 0) {

            int effectiveTotalVehiclesForProportional = 0;
            Map<Direction, Integer> effectiveVehicleCounts = new HashMap<>();
            for(Direction dir : Direction.values()){
                if(vehicleCount.getOrDefault(dir,0) > 0){

                    effectiveVehicleCounts.put(dir, vehicleCount.get(dir));
                    effectiveTotalVehiclesForProportional += vehicleCount.get(dir);
                }
            }

            if (effectiveTotalVehiclesForProportional > 0) {
                for (Direction dir : effectiveVehicleCounts.keySet()) {
                    double proportion = (double) effectiveVehicleCounts.get(dir) / effectiveTotalVehiclesForProportional;
                    int additionalTime = (int) Math.round(proportion * remainingPoolForProportional);
                    greenDurations.put(dir, greenDurations.get(dir) + additionalTime);
                }
            }
        }


        long currentTotalGreen = 0;
        for(Direction dir : Direction.values()){
            if(vehicleCount.getOrDefault(dir,0) > 0){
                greenDurations.put(dir, Math.min(MAX_GREEN_DURATION, greenDurations.get(dir)));
                greenDurations.put(dir, Math.max(MIN_GREEN_DURATION_IF_CARS, greenDurations.get(dir))); // Re-ensure min if clamping max reduced it
            } else {
                greenDurations.put(dir, 0);
            }
            currentTotalGreen += greenDurations.get(dir);
        }

        if (currentTotalGreen != 0 && currentTotalGreen != availableGreenTimePool && availableGreenTimePool > 0) {
            double adjustmentFactor = availableGreenTimePool / currentTotalGreen;
            for (Direction dir : greenDurations.keySet()) {
                if (greenDurations.get(dir) > 0) { // Only adjust lanes that have green time
                    int adjustedTime = (int) Math.round(greenDurations.get(dir) * adjustmentFactor);
                    if (vehicleCount.getOrDefault(dir, 0) > 0) {
                        adjustedTime = Math.max(MIN_GREEN_DURATION_IF_CARS, adjustedTime);
                    }
                    adjustedTime = Math.min(MAX_GREEN_DURATION, adjustedTime);
                    greenDurations.put(dir, adjustedTime);
                }
            }
        }
    }

    public void updateDurations() {
        calculateTotalVehicleCount();
        calculateGreenDurations();
    }
}