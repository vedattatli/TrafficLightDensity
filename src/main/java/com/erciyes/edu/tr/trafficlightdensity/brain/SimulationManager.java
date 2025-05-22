package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.*;

//	Simülasyonu başlatma/durdurma/sıfırlama kontrollerini sağlar.
public class SimulationManager {

    private TrafficController trafficController;
    private CycleManager cycleManager;
    private Timer timer;


    private boolean isAutoMode = false;
    private boolean isRunning = false;


    public void startSimulation()
    {
        if(isRunning) return;

        isRunning=true;
        cycleManager.startCycle();

        int duration = cycleManager.getCurrentDuration();
        startTimer(duration);
    }

    private void startTimer(int durationInSeconds) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onCycleComplete();
            }
        }, durationInSeconds * 1000);
    }

    private void onCycleComplete() {
        if (!isRunning) return;

        // Eğer araç kaldıysa: Aynı yönde kal
        if (cycleManager.hasRemainingVehicle()) {
            startTimer(cycleManager.getCurrentDuration());
            return;
        }

        // Araç kalmadıysa sıradaki yöne geç
        cycleManager.switchToNextDirection();
        startTimer(cycleManager.getCurrentDuration());

        // Eğer manuel moddaysak sadece 1 cycle çalıştır ve dur
        if (!isAutoMode) {
            stopSimulation();
        }
    }
    public void stopSimulation() {
        if (!isRunning) return;

        isRunning = false;
        isAutoMode = false;

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // (İleride GUI reset komutu buraya yazılabilir)
        System.out.println("Simülasyon durduruldu.");
    }
    public void startAutoMode() {
        if (isRunning) return;

        isAutoMode = true;

        // Rastgele veri üret (örnek olarak sabit random burada, Sensor sınıfı eklenince değişir)
        Map<Direction, Integer> randomCounts = new HashMap<>();
        Random rand = new Random();
        for (Direction dir : Direction.values()) {
            randomCounts.put(dir, rand.nextInt(30)); // 0–29 arası random araç
        }

        trafficController.setVehicleCounts(randomCounts);
        trafficController.updateDurations(); // total + green hesaplaması

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