package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.TimerDisplay;
import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.TrafficLightColorUpdater;
import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.UserInterfaceController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class SimulationManager {
    private UserInterfaceController userInterfaceController;
    private TimerDisplay timerDisplay;
    private final TrafficController trafficController = new TrafficController();
    private final CycleManager cycleManager;
    private final TrafficLightColorUpdater lightColorUpdater = new TrafficLightColorUpdater();

    private Timeline phaseTimeline;
    private boolean isAutoMode = false;
    private boolean isRunning  = false;
    private boolean isPaused   = false;

    private final Map<Direction, LightPhase> currentLightPhases = new EnumMap<>(Direction.class);
    private Direction currentlyActiveDirection; // Başlangıçta null
    private int timeLeftInCurrentPhaseForActiveDir;

    private Consumer<Map<Direction, Integer>> onTickForAllDirections;
    private Consumer<Direction> onPhaseInfoChange;

    public SimulationManager() {
        this.cycleManager = new CycleManager(trafficController);
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, LightPhase.RED);
        }
    }

    public void setUserInterfaceController(UserInterfaceController ui) {
        this.userInterfaceController = ui;
    }
    public void setTimerDisplay(TimerDisplay timerDisplay) {
        this.timerDisplay = timerDisplay;
    }
    public TrafficController getTrafficController() {
        return trafficController;
    }

    public void setOnTick(Consumer<Map<Direction, Integer>> tickCallbackForAllDirections) {
        this.onTickForAllDirections = tickCallbackForAllDirections;
    }

    public void setOnPhaseInfoChange(Consumer<Direction> phaseInfoCallback) {
        this.onPhaseInfoChange = phaseInfoCallback;
    }

    public Map<Direction, LightPhase> getCurrentLightPhasesMap() {
        return currentLightPhases;
    }


    public void startSimulation() {
        isRunning = true;
        isPaused  = false;
        cycleManager.startCycle(); // Sets up the first direction in cycleManager

        for (Direction d : Direction.values()) {
            currentLightPhases.put(d, LightPhase.RED);
        }
        if (userInterfaceController != null) {
            lightColorUpdater.resetTrafficLightsColors(userInterfaceController);
        }

        // İlk updateAndNotifyTimers çağrısı burada, currentlyActiveDirection hala null
        updateAndNotifyTimers();


        Timeline initialRed = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (isRunning) startGreenPhase();
        }));
        initialRed.setCycleCount(1);
        initialRed.play();
    }

    private void startGreenPhase() {
        if (!isRunning) return;
        currentlyActiveDirection = cycleManager.getCurrentDirection(); // Şimdi atanıyor
        if (currentlyActiveDirection == null) {
            System.err.println("SimulationManager Error: No current direction from CycleManager. Stopping.");
            stopSimulation();
            return;
        }
        int greenDuration = trafficController.getGreenDuration(currentlyActiveDirection);
        System.out.println("Starting simulation with: " + currentlyActiveDirection + " Green Phase, Duration: " + greenDuration + "s");


        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir,
                    (dir == currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED
            );
        }
        runPhaseTimer(greenDuration, LightPhase.GREEN);
    }

    private void runPhaseTimer(int durationSeconds, LightPhase phaseForActiveDirection) {
        if (!isRunning) return;

        currentLightPhases.put(currentlyActiveDirection, phaseForActiveDirection);
        for (Direction d : Direction.values()) {
            if (d != currentlyActiveDirection) {
                currentLightPhases.put(d, LightPhase.RED);
            }
        }

        if (userInterfaceController != null) {
            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);
        }

        if (phaseTimeline != null) {
            phaseTimeline.stop();
        }

        timeLeftInCurrentPhaseForActiveDir = durationSeconds;
        updateAndNotifyTimers(); // currentlyActiveDirection artık null değil


        phaseTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (!isRunning || isPaused) return;
                    timeLeftInCurrentPhaseForActiveDir--;
                    updateAndNotifyTimers();

                    if (timeLeftInCurrentPhaseForActiveDir < 0) {
                        handlePhaseTimerEnd(phaseForActiveDirection);
                    }
                })
        );
        phaseTimeline.setCycleCount(durationSeconds + 1);
        phaseTimeline.play();
    }

    private void updateAndNotifyTimers() {
        if (onTickForAllDirections == null) return;

        Map<Direction, Integer> remainingTimes = new EnumMap<>(Direction.class);
        List<Direction> cycleOrder = cycleManager.getDirectionOrder();

        if (!isRunning) {
            for (Direction dir : Direction.values()) {
                remainingTimes.put(dir, 0);
            }
            onTickForAllDirections.accept(remainingTimes);
            return;
        }

        if (currentlyActiveDirection == null) {
            // Simülasyon başlangıcı, ilk yeşil ışık öncesi (örn: 1 saniyelik tüm kırmızılar)
            int initialAllRedDelay = 1; // startSimulation'daki Timeline gecikmesi
            int accumulatedTimeToStartNextGreen = initialAllRedDelay;

            Map<Direction, Boolean> processedDirections = new EnumMap<>(Direction.class);

            for (Direction dirInCycleOrder : cycleOrder) {
                remainingTimes.put(dirInCycleOrder, accumulatedTimeToStartNextGreen);
                processedDirections.put(dirInCycleOrder, true);
                // Durations must be available from TrafficController here
                accumulatedTimeToStartNextGreen += trafficController.getGreenDuration(dirInCycleOrder);
                accumulatedTimeToStartNextGreen += TrafficController.YELLOW_DURATION;
            }
            // Eğer Direction.values() cycleOrder'dan fazla yön içeriyorsa (olmamalı)
            for(Direction d : Direction.values()){
                if(!processedDirections.containsKey(d)){
                    remainingTimes.put(d, accumulatedTimeToStartNextGreen); // Veya bir hata/varsayılan değer
                }
            }

        } else { // Normal operasyon, bir yön aktif (Yeşil veya Sarı)
            int cycleOrderIndexCurrent = cycleOrder.indexOf(currentlyActiveDirection);

            if (cycleOrderIndexCurrent == -1) { // Güvenlik kontrolü
                System.err.println("Error: currentlyActiveDirection ("+currentlyActiveDirection+") not found in cycleOrder. Resetting timers.");
                for (Direction dir : Direction.values()) {
                    remainingTimes.put(dir, 0);
                }
                onTickForAllDirections.accept(remainingTimes);
                return;
            }

            for (Direction dirToCalc : Direction.values()) {
                if (dirToCalc == currentlyActiveDirection) {
                    remainingTimes.put(dirToCalc, Math.max(0, timeLeftInCurrentPhaseForActiveDir));
                } else { // dirToCalc için Kırmızı Işık zamanı hesapla
                    int timeUntilNextGreenForRedDir = 0;
                    timeUntilNextGreenForRedDir += Math.max(0, timeLeftInCurrentPhaseForActiveDir);

                    if (currentLightPhases.get(currentlyActiveDirection) == LightPhase.GREEN) {
                        timeUntilNextGreenForRedDir += TrafficController.YELLOW_DURATION;
                    }

                    for (int i = 1; i <= cycleOrder.size(); i++) {
                        int nextConsideredIndex = (cycleOrderIndexCurrent + i) % cycleOrder.size();
                        Direction nextDirInCycle = cycleOrder.get(nextConsideredIndex);

                        if (nextDirInCycle == dirToCalc) {
                            break;
                        }
                        timeUntilNextGreenForRedDir += trafficController.getGreenDuration(nextDirInCycle);
                        timeUntilNextGreenForRedDir += TrafficController.YELLOW_DURATION;
                    }
                    remainingTimes.put(dirToCalc, timeUntilNextGreenForRedDir);
                }
            }
        }
        onTickForAllDirections.accept(remainingTimes);
    }

    private int calculateTotalRedForDir(Direction targetDir) { // Bu metod artık ana zamanlayıcı için kullanılmıyor
        if (cycleManager == null || trafficController == null || !trafficController.getVehicleCounts().containsKey(targetDir) ) return 0;
        int totalRed = 0;
        List<Direction> order = cycleManager.getDirectionOrder();
        if(order == null || order.isEmpty()) return 0;

        for (Direction d : order) {
            if (d != targetDir) {
                totalRed += trafficController.getGreenDuration(d);
                totalRed += TrafficController.YELLOW_DURATION;
            }
        }
        return totalRed;
    }


    private void handlePhaseTimerEnd(LightPhase finishedPhase) {
        if (!isRunning) return;

        if (finishedPhase == LightPhase.GREEN) {
            System.out.println(currentlyActiveDirection + " Yellow Phase starting.");
            runPhaseTimer(
                    TrafficController.YELLOW_DURATION,
                    LightPhase.YELLOW
            );
        }
        else if (finishedPhase == LightPhase.YELLOW) {
            System.out.println(currentlyActiveDirection + " Red Phase starting.");
            currentLightPhases.put(currentlyActiveDirection, LightPhase.RED);
            processNextInCycle();
        }
    }

    private void processNextInCycle() {
        if (!isRunning) return;

        // isEndOfCycle() aktif yönün döngünün sonuncusu olup olmadığını kontrol eder.
        // Eğer manuel moddaysak ve döngünün sonuna geldiysek dur.
        if (!isAutoMode && cycleManager.isEndOfCycle()) {
            System.out.println("Manual mode: Full cycle completed. Simulation stopping.");
            stopSimulation();
            updateAndNotifyTimers();
            return;
        }

        cycleManager.switchToNextDirection();
        currentlyActiveDirection = cycleManager.getCurrentDirection();

        if (currentlyActiveDirection == null) {
            System.err.println("SimulationManager Error: Next direction is null. Simulation stopping.");
            stopSimulation();
            return;
        }

        int nextGreenDuration = trafficController.getGreenDuration(currentlyActiveDirection);
        System.out.println("Next Green Phase: " + currentlyActiveDirection + ", Duration: " + nextGreenDuration + "s");

        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir,
                    (dir == currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED
            );
        }
        runPhaseTimer(nextGreenDuration, LightPhase.GREEN);
    }

    private Map<Direction, Integer> createZeroTimesMap() {
        Map<Direction, Integer> zeroTimes = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            zeroTimes.put(d, 0);
        }
        return zeroTimes;
    }

    public void stopSimulation() {
        if (!isRunning && !isPaused) return;
        isRunning = false;
        isPaused  = false;

        if (phaseTimeline != null) {
            phaseTimeline.stop();
            phaseTimeline = null;
        }
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, LightPhase.RED);
        }
        if (userInterfaceController != null) {
            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);
        }
        // currentlyActiveDirection'ı null yapmadan önce son bir kez timer'ları sıfırla
        // ya da updateAndNotifyTimers içindeki !isRunning kontrolüne güven
        Direction oldActive = currentlyActiveDirection; // Sakla
        currentlyActiveDirection = null; // Şimdi null yap
        timeLeftInCurrentPhaseForActiveDir = 0;
        System.out.println("Simulation stopped.");
        updateAndNotifyTimers(); // Bu çağrı !isRunning bloğuna girecek ve 0'ları gönderecek
        currentlyActiveDirection = oldActive; // Sadece bir anlık, aslında gerek yok çünkü zaten durdu
    }

    public void pauseSimulation() {
        if (isRunning && !isPaused) {
            isPaused = true;
            if (phaseTimeline != null) phaseTimeline.pause();
            System.out.println("Simulation paused.");
        }
    }

    public void resumeSimulation() {
        if (isRunning && isPaused) {
            isPaused = false;
            if (phaseTimeline != null) phaseTimeline.play();
            System.out.println("Simulation resuming.");
        }
    }

    public boolean isRunning() { return isRunning; }
    public boolean isPaused()  { return isPaused;  }

    public LightPhase getLightPhaseForDirection(Direction direction) {
        return currentLightPhases.getOrDefault(direction, LightPhase.RED);
    }

    public void startAutoMode() {
        isAutoMode = true;
        Map<Direction, Integer> randomCounts = new HashMap<>();
        Random rand = new Random();
        for (Direction dir : Direction.values()) {
            randomCounts.put(dir, rand.nextInt(15) +1);
        }
        trafficController.setVehicleCounts(randomCounts);
        trafficController.updateDurations();
        if(timerDisplay != null) timerDisplay.labelDisplayBaslangic(trafficController);
        startSimulation();
    }

    public void startManualMode(Map<Direction, Integer> manualCounts) {
        isAutoMode = false;
        trafficController.setVehicleCounts(manualCounts);
        trafficController.updateDurations();
        if(timerDisplay != null) timerDisplay.labelDisplayBaslangic(trafficController);
        startSimulation();
    }
}