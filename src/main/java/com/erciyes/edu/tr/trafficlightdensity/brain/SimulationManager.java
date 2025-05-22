package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class SimulationManager {

    private final TrafficController trafficController = new TrafficController();
    private final CycleManager cycleManager = new CycleManager(trafficController);

    private Timeline countdownTimeline;

    private boolean isAutoMode = false;
    private boolean isRunning = false;
    private boolean isPaused = false; // Duraklatma durumu için bayrak

    private Consumer<Integer> onTick;
    private Consumer<Direction> onPhaseChange;

    public void setOnTick(Consumer<Integer> tickCallback) {
        this.onTick = tickCallback;
    }

    public void setOnPhaseChange(Consumer<Direction> phaseCallback) {
        this.onPhaseChange = phaseCallback;
    }

    public void startSimulation() {
        if (isRunning && !isPaused) { // Zaten çalışıyorsa ve duraklatılmamışsa bir şey yapma
            System.out.println("Simülasyon zaten aktif olarak çalışıyor.");
            return;
        }
        if (isRunning && isPaused) { // Duraklatılmışsa, devam ettirmek yerine tam durdur ve yeniden başlat
            System.out.println("Simülasyon duraklatılmıştı, şimdi tamamen durdurulup yeniden başlatılıyor.");
            stopSimulation(); // Önce tamamen durdur
        }

        isRunning = true;
        isPaused = false; // Başlarken duraklatılmış değil
        cycleManager.startCycle();

        Direction current = cycleManager.getCurrentDirection();
        int duration = cycleManager.getCurrentDuration();

        if (current == null) {
            System.err.println("SimulationManager Hata: Başlatılacak mevcut yön bulunamadı. Simülasyon durduruluyor.");
            stopSimulation();
            return;
        }

        System.out.println("Simülasyon başlatıldı. İlk yön: " + current.getTurkishName() + ", Süre: " + duration + "sn");

        if (onPhaseChange != null) {
            onPhaseChange.accept(current);
        }
        startCountdown(duration, current);
    }

    private void startCountdown(int durationInSeconds, Direction direction) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        final int[] kalanSure = {durationInSeconds};
        if (onTick != null && direction != null) { // Yön null değilse ilk tick'i gönder
            onTick.accept(kalanSure[0]);
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    kalanSure[0]--;
                    if (onTick != null && direction != null && kalanSure[0] >= 0) {
                        onTick.accept(kalanSure[0]);
                    }

                    if (kalanSure[0] < 0) {
                        onCycleComplete();
                    }
                })
        );
        countdownTimeline.setCycleCount(durationInSeconds + 1);
        countdownTimeline.play();
    }

    private void onCycleComplete() {
        if (!isRunning) return;

        if (!isAutoMode && cycleManager.isEndOfCycle()) {
            System.out.println("Manuel mod: Tam döngü tamamlandı (" +
                    (cycleManager.getCurrentDirection() != null ? cycleManager.getCurrentDirection().getTurkishName() : "SON YÖN") +
                    " için). Simülasyon durduruluyor.");
            stopSimulation();
            // GUI'nin son durumu (örn. tüm labelların "-" olması) stopSimulation sonrası UIController'da sağlanabilir.
            return;
        }

        cycleManager.switchToNextDirection();
        Direction newDir = cycleManager.getCurrentDirection();
        int newDuration = cycleManager.getCurrentDuration();

        if (newDir == null) {
            System.err.println("SimulationManager Hata: Bir sonraki yön null. Simülasyon durduruluyor.");
            stopSimulation();
            return;
        }
        System.out.println("Sıradaki yöne geçiliyor: " + newDir.getTurkishName() + ", Süre: " + newDuration + "sn");

        if (onPhaseChange != null) {
            onPhaseChange.accept(newDir);
        }
        startCountdown(newDuration, newDir);
    }

    public void stopSimulation() {
        if (!isRunning && !isPaused) { // Zaten durmuş ve duraklatılmamışsa bir şey yapma
            return;
        }

        isRunning = false;
        isPaused = false; // Durdurulduğunda duraklatılma durumunu da sıfırla

        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        System.out.println("Simülasyon durduruldu.");
    }

    public void pauseSimulation() {
        if (isRunning && !isPaused && countdownTimeline != null) {
            countdownTimeline.pause();
            isPaused = true;
            System.out.println("Simülasyon duraklatıldı.");
        }
    }

    public void resumeSimulation() {
        if (isRunning && isPaused && countdownTimeline != null) {
            countdownTimeline.play();
            isPaused = false;
            System.out.println("Simülasyon devam ettiriliyor.");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void startAutoMode() {
        // if (isRunning) return; // startSimulation içinde bu kontrol var
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
        // if (isRunning) return; // startSimulation içinde bu kontrol var
        isAutoMode = false;
        trafficController.setVehicleCounts(manualCounts);
        trafficController.updateDurations();
        startSimulation();
    }
}