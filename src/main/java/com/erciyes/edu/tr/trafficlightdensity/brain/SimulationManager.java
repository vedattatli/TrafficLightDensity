package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.TimerDisplay;
import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.TrafficLightColorUpdater;
import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.UserInterfaceController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class SimulationManager {

    /** UI katmanından enjekte edilen controller; NPE’yi engellemek için null-kontrollü kullanılıyor. */
    private UserInterfaceController userInterfaceController;

    private TimerDisplay timerDisplay;
    private final TrafficController trafficController = new TrafficController();
    private final CycleManager   cycleManager      = new CycleManager(trafficController);
    private final TrafficLightColorUpdater lightColorUpdater = new TrafficLightColorUpdater();

    private Timeline phaseTimeline;
    private boolean  isAutoMode = false;
    private boolean  isRunning  = false;
    private boolean  isPaused   = false;

    private Map<Direction, LightPhase> currentLightPhases = new HashMap<>();
    private Direction  currentlyActiveDirection;

    private Consumer<Integer>  onTick;
    private Consumer<Direction> onPhaseInfoChange;

    public SimulationManager() {
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, LightPhase.RED);
        }
    }

    public void setUserInterfaceController(UserInterfaceController userInterfaceController) {
        this.userInterfaceController = userInterfaceController;
    }

    public void setTimerDisplay(TimerDisplay timerDisplay) { this.timerDisplay = timerDisplay; }
    public TrafficController getTrafficController()        { return trafficController; }

    public void setOnTick(Consumer<Integer> tickCallback)                { this.onTick = tickCallback; }
    public void setOnPhaseInfoChange(Consumer<Direction> phaseCallback)  { this.onPhaseInfoChange = phaseCallback; }

    /* ------------------ SİMÜLASYON AKIŞI ------------------ */

    public void startSimulation() {
        isRunning = true;
        isPaused  = false;

        cycleManager.startCycle();
        currentlyActiveDirection = cycleManager.getCurrentDirection();
        int greenDuration = cycleManager.getCurrentDuration();

        System.out.println("Simülasyon başlatılıyor: " + currentlyActiveDirection + " Yeşil Fazı ile.");

        for (Direction d : Direction.values()) {
            currentLightPhases.put(d, (d == currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED);
        }
        if (userInterfaceController != null) {
            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);
        }

        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(currentlyActiveDirection);

        runPhaseTimer(greenDuration, LightPhase.GREEN);
    }

    private void runPhaseTimer(int durationSeconds, LightPhase phaseForActiveDirection) {
        if (!isRunning) return;

        currentLightPhases.put(currentlyActiveDirection, phaseForActiveDirection);
        if (phaseForActiveDirection == LightPhase.GREEN || phaseForActiveDirection == LightPhase.YELLOW) {
            for (Direction d : Direction.values()) {
                if (d != currentlyActiveDirection) currentLightPhases.put(d, LightPhase.RED);
            }
        }
        if (userInterfaceController != null)
            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);
        if (phaseTimeline != null) phaseTimeline.stop();

        final int[] timeLeft = { durationSeconds };
        if (onTick != null) onTick.accept(timeLeft[0]);

        phaseTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (!isRunning || isPaused) return;
                    timeLeft[0]--;
                    if (onTick != null && timeLeft[0] >= 0) onTick.accept(timeLeft[0]);
                    if (timeLeft[0] < 0) handlePhaseTimerEnd(phaseForActiveDirection);
                })
        );
        phaseTimeline.setCycleCount(durationSeconds + 1);
        phaseTimeline.play();
    }

    private void handlePhaseTimerEnd(LightPhase finishedPhase) {
        if (!isRunning) return;

        if (finishedPhase == LightPhase.GREEN) {
            System.out.println(currentlyActiveDirection + " için Sarı Faz başlıyor.");

            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);

            runPhaseTimer((int) LightPhase.getDefaultPhaseDuration(LightPhase.YELLOW).getSeconds(),
                    LightPhase.YELLOW);

        } else if (finishedPhase == LightPhase.YELLOW) {
            System.out.println(currentlyActiveDirection + " için Kırmızı Faz başlıyor.");
            currentLightPhases.put(currentlyActiveDirection, LightPhase.RED);
            processNextInCycle();
        }
    }

    private void processNextInCycle() {
        if (!isRunning) return;

        if (!isAutoMode && cycleManager.isEndOfCycle()) {
            System.out.println("Manuel mod: Tam döngü tamamlandı. Simülasyon durduruluyor.");
            stopSimulation();
            if (onPhaseInfoChange != null) onPhaseInfoChange.accept(null);
            if (onTick != null) onTick.accept(0);
            return;
        }

        cycleManager.switchToNextDirection();
        currentlyActiveDirection = cycleManager.getCurrentDirection();
        int nextGreenDuration = cycleManager.getCurrentDuration();

        if (currentlyActiveDirection == null) {
            System.err.println("SimulationManager Hata: Bir sonraki yön null. Simülasyon durduruluyor.");
            stopSimulation();
            return;
        }

        System.out.println("Sıradaki Yeşil Faz: " + currentlyActiveDirection +
                ", Süre: " + nextGreenDuration + "sn");

        for (Direction d : Direction.values()) {
            currentLightPhases.put(d, (d == currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED);
        }

        if (userInterfaceController != null) {
            lightColorUpdater.updateTrafficLightsColors(currentLightPhases, userInterfaceController);
        }

        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(currentlyActiveDirection);

        runPhaseTimer(nextGreenDuration, LightPhase.GREEN);
    }

    /* ------------------ KONTROL METOTLARI ------------------ */

    public void stopSimulation() {
        if (!isRunning && !isPaused) return;
        isRunning = false;
        isPaused  = false;

        if (phaseTimeline != null) {
            phaseTimeline.stop();
            phaseTimeline = null;
        }
        for (Direction d : Direction.values()) currentLightPhases.put(d, LightPhase.RED);
        currentlyActiveDirection = null;

        System.out.println("Simülasyon durduruldu.");
        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(null);
        if (onTick != null) onTick.accept(0);
    }

    public void pauseSimulation()  { if (isRunning && !isPaused) { isPaused = true;  if (phaseTimeline != null) phaseTimeline.pause();  System.out.println("Simülasyon duraklatıldı."); } }
    public void resumeSimulation() { if (isRunning &&  isPaused) { isPaused = false; if (phaseTimeline != null) phaseTimeline.play();   System.out.println("Simülasyon devam ettiriliyor."); } }

    public boolean isRunning() { return isRunning; }
    public boolean isPaused()  { return isPaused;  }

    public LightPhase getLightPhaseForDirection(Direction direction) {
        return currentLightPhases.getOrDefault(direction, LightPhase.RED);
    }

    /* ------------------ MOD BAŞLATICILAR ------------------ */

    public void startAutoMode() {
        isAutoMode = true;

        Map<Direction, Integer> randomCounts = new HashMap<>();
        Random rand = new Random();
        for (Direction dir : Direction.values()) randomCounts.put(dir, rand.nextInt(30));

        trafficController.setVehicleCounts(randomCounts);
        trafficController.updateDurations();

        timerDisplay.labelDisplayBaslangic(trafficController);
        startSimulation();
    }

    public void startManualMode(Map<Direction, Integer> manualCounts) {
        isAutoMode = false;
        trafficController.setVehicleCounts(manualCounts);
        trafficController.updateDurations();

        timerDisplay.labelDisplayBaslangic(trafficController);
        startSimulation();
    }
}
