package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

public class SimulationManager {

    private final TrafficController trafficController = new TrafficController();
    private final CycleManager cycleManager = new CycleManager(trafficController);

    private Timeline countdownTimeline;

    private boolean isAutoMode = false;
    private boolean isRunning = false;

    // 👇 GUI'ye bilgi vermek için
    private Consumer<Integer> onTick;
    private Consumer<Direction> onPhaseChange;

    public void setOnTick(Consumer<Integer> tickCallback) {
        this.onTick = tickCallback;
    }

    public void setOnPhaseChange(Consumer<Direction> phaseCallback) {
        this.onPhaseChange = phaseCallback;
    }

    public void startSimulation() {
        
        if (isRunning) return;

        isRunning = true;
        cycleManager.startCycle();

        Direction current = cycleManager.getCurrentDirection();
        int duration = cycleManager.getCurrentDuration();

        if (onPhaseChange != null) onPhaseChange.accept(current);
        startCountdown(duration, current);
    }

    private void startCountdown(int durationInSeconds, Direction direction) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        final int[] kalan = {durationInSeconds};
        if (onTick != null) onTick.accept(kalan[0]);

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    kalan[0]--;
                    if (onTick != null && kalan[0] >= 0) onTick.accept(kalan[0]);

                    if (kalan[0] < 0) {
                        countdownTimeline.stop();
                        onCycleComplete();
                    }
                })
        );
        countdownTimeline.setCycleCount(durationInSeconds + 1);
        countdownTimeline.play();
    }

    private void onCycleComplete() {
        if (!isRunning) return;

        if (cycleManager.hasRemainingVehicle()) {
            int duration = cycleManager.getCurrentDuration();
            Direction direction = cycleManager.getCurrentDirection();
            if (onPhaseChange != null) onPhaseChange.accept(direction);
            startCountdown(duration, direction);
            return;
        }

        cycleManager.switchToNextDirection();
        Direction newDir = cycleManager.getCurrentDirection();
        int newDuration = cycleManager.getCurrentDuration();

        if (onPhaseChange != null) onPhaseChange.accept(newDir);
        startCountdown(newDuration, newDir);

        if (!isAutoMode) {
            stopSimulation();
        }
    }

    public void stopSimulation() {
        if (!isRunning) return;

        isRunning = false;
        isAutoMode = false;

        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }

        System.out.println("Simülasyon durduruldu.");
    }

    public void startAutoMode() {
        if (isRunning) return;

        isAutoMode = true;

        Map<Direction, Integer> randomCounts = new HashMap<>();
        Random rand = new Random();
        for (Direction dir : Direction.values()) {
            randomCounts.put(dir, rand.nextInt(30));
        }

        trafficController.setVehicleCounts(randomCounts);
        trafficController.updateDurations();

        startSimulation();
    }

    public void startManualMode(Map<Direction, Integer> manualCounts) {
        if (isRunning) return;

        isAutoMode = false;

        trafficController.setVehicleCounts(manualCounts);
        trafficController.updateDurations();

        startSimulation();
    }
}
