package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.intersection_gui.TimerDisplay;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase; // Eklendi
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class SimulationManager {

    private TimerDisplay timerDisplay;
    private final TrafficController trafficController = new TrafficController();
    private final CycleManager cycleManager = new CycleManager(trafficController);
    private Timeline phaseTimeline; // countdownTimeline -> phaseTimeline olarak yeniden adlandırıldı
    private boolean isAutoMode = false;
    private boolean isRunning = false;
    private boolean isPaused = false;

    // Her yön için mevcut ışık fazını tutar
    private Map<Direction, LightPhase> currentLightPhases = new HashMap<>();
    private Direction currentlyActiveDirection; // Yeşil veya Sarı olan yön

    private Consumer<Integer> onTick; // Kalan süreyi GUI'ye bildirmek için
    private Consumer<Direction> onPhaseInfoChange; // GUI'ye hangi yönün ve fazın aktif olduğunu bildirmek için

    public void setTimerDisplay(TimerDisplay timerDisplay)
    {
        this.timerDisplay = timerDisplay;
    }
    public TrafficController getTrafficController() {
        return trafficController;
    }

    public SimulationManager() {
        // Başlangıçta tüm ışıkları kırmızı yap
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, LightPhase.RED);
        }
    }

    public void setOnTick(Consumer<Integer> tickCallback) {
        this.onTick = tickCallback;
    }

    // onPhaseChange -> onPhaseInfoChange olarak yeniden adlandırıldı ve amacı netleştirildi
    public void setOnPhaseInfoChange(Consumer<Direction> phaseInfoCallback) {
        this.onPhaseInfoChange = phaseInfoCallback;
    }

    public void startSimulation() {

        isRunning = true;
        isPaused = false;
        cycleManager.startCycle();

        this.currentlyActiveDirection = cycleManager.getCurrentDirection();
        int greenDuration = cycleManager.getCurrentDuration();

        System.out.println("Simülasyon başlatılıyor: " + this.currentlyActiveDirection.getTurkishName() + " Yeşil Fazı ile.");
        // Diğer tüm ışıkları kırmızı yap, aktif olanı yeşil yap
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, (dir == this.currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED);
        }
        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(this.currentlyActiveDirection); // GUI'yi bilgilendir
        runPhaseTimer(greenDuration, LightPhase.GREEN);
    }

    private void runPhaseTimer(int durationSeconds, LightPhase phaseForActiveDirection) {
        if (!isRunning) return;

        currentLightPhases.put(this.currentlyActiveDirection, phaseForActiveDirection);
        // Diğer yönler kırmızı kalmaya devam eder (eğer bu yeşil veya sarı ise)
        if (phaseForActiveDirection == LightPhase.GREEN || phaseForActiveDirection == LightPhase.YELLOW) {
            for(Direction d : Direction.values()){
                if(d != this.currentlyActiveDirection) {
                    currentLightPhases.put(d, LightPhase.RED);
                }
            }
        }

        if (phaseTimeline != null) {
            phaseTimeline.stop();
        }

        final int[] timeLeft = {durationSeconds};
        if (onTick != null) {
            onTick.accept(timeLeft[0]); // İlk süreyi gönder
        }

        phaseTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (!isRunning || isPaused) { // Duraklatma kontrolü eklendi
                        return;
                    }
                    timeLeft[0]--;
                    if (onTick != null && timeLeft[0] >= 0) {
                        onTick.accept(timeLeft[0]);
                    }
                    if (timeLeft[0] < 0) {
                        handlePhaseTimerEnd(phaseForActiveDirection);
                    }
                })
        );
        phaseTimeline.setCycleCount(durationSeconds + 1);
        phaseTimeline.play();
    }

    private void handlePhaseTimerEnd(LightPhase finishedPhase) {
        if (!isRunning) return;

        if (finishedPhase == LightPhase.GREEN) {
            // Yeşil bitti, aynı yön için Sarı'yı başlat
            System.out.println(this.currentlyActiveDirection.getTurkishName() + " için Sarı Faz başlıyor.");
            runPhaseTimer((int) LightPhase.getDefaultPhaseDuration(LightPhase.YELLOW).getSeconds(), LightPhase.YELLOW);
        } else if (finishedPhase == LightPhase.YELLOW) {
            // Sarı bitti, bu yönü Kırmızı yap
            System.out.println(this.currentlyActiveDirection.getTurkishName() + " için Kırmızı Faz başlıyor.");
            currentLightPhases.put(this.currentlyActiveDirection, LightPhase.RED);
            // Şimdi bir sonraki yönün Yeşil fazına geçmek için döngü mantığını çalıştır
            processNextInCycle();
        }
    }

    private void processNextInCycle() {
        if (!isRunning) return;

        // Manuel modda tam döngü kontrolü
        if (!isAutoMode && cycleManager.isEndOfCycle()) { // isEndOfCycle, mevcut Kırmızıya dönen yönün döngünün sonu olup olmadığını kontrol eder
            System.out.println("Manuel mod: Tam döngü tamamlandı. Simülasyon durduruluyor.");
            stopSimulation();
            if (onPhaseInfoChange != null) onPhaseInfoChange.accept(null); // Aktif yön olmadığını bildir
            if (onTick != null) onTick.accept(0); // Zamanlayıcıyı sıfırla
            return;
        }

        cycleManager.switchToNextDirection(); // Bir sonraki yeşil yakacak yönü belirle
        this.currentlyActiveDirection = cycleManager.getCurrentDirection();
        int nextGreenDuration = cycleManager.getCurrentDuration();

        if (this.currentlyActiveDirection == null) {
            System.err.println("SimulationManager Hata: Bir sonraki yön null. Simülasyon durduruluyor.");
            stopSimulation();
            return;
        }

        System.out.println("Sıradaki Yeşil Faz: " + this.currentlyActiveDirection.getTurkishName() + ", Süre: " + nextGreenDuration + "sn");
        // Diğer tüm ışıkları kırmızı yap, yeni aktif olanı yeşil yap
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, (dir == this.currentlyActiveDirection) ? LightPhase.GREEN : LightPhase.RED);
        }
        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(this.currentlyActiveDirection); // GUI'yi bilgilendir
        runPhaseTimer(nextGreenDuration, LightPhase.GREEN);
    }

    public void stopSimulation() {
        if (!isRunning && !isPaused) return;
        isRunning = false;
        isPaused = false;
        if (phaseTimeline != null) {
            phaseTimeline.stop();
            phaseTimeline = null;
        }
        // Tüm ışıkları kırmızı yap veya varsayılan bir duruma getir
        for (Direction dir : Direction.values()) {
            currentLightPhases.put(dir, LightPhase.RED);
        }
        currentlyActiveDirection = null;
        System.out.println("Simülasyon durduruldu.");
        // GUI'yi bilgilendir ki labelları sıfırlasın
        if (onPhaseInfoChange != null) onPhaseInfoChange.accept(null);
        if (onTick != null) onTick.accept(0);
    }

    public void pauseSimulation() {
        if (isRunning && !isPaused) {
            isPaused = true;
            if (phaseTimeline != null) {
                phaseTimeline.pause();
            }
            System.out.println("Simülasyon duraklatıldı.");
        }
    }

    public void resumeSimulation() {
        if (isRunning && isPaused) {
            isPaused = false;
            if (phaseTimeline != null) {
                phaseTimeline.play();
            }
            System.out.println("Simülasyon devam ettiriliyor.");
            // GUI'ye mevcut durumu tekrar gönder (kalan süre vs.)
            if (onTick != null && phaseTimeline != null) {
                // Timeline'ın kalan süresini doğrudan almak zor, bu yüzden bir sonraki tick'i bekleyebilir
                // veya onTick.accept(mevcut_kalan_süre_hesaplanmali)
            }
        }
    }

    public boolean isRunning() { return isRunning; }
    public boolean isPaused() { return isPaused; }

    // Bu metod VehicleAnimation tarafından kullanılacak
    public LightPhase getLightPhaseForDirection(Direction direction) {
        return currentLightPhases.getOrDefault(direction, LightPhase.RED);
    }

    public void startAutoMode() {
        isAutoMode = true;

        Map<Direction, Integer> randomCounts = new HashMap<>();
        Random rand = new Random();
        for (Direction dir : Direction.values()) {
            randomCounts.put(dir, rand.nextInt(30));
        }

        trafficController.setVehicleCounts(randomCounts);
        trafficController.updateDurations();

        this.timerDisplay.labelDisplayBaslangic();




        startSimulation();
    }

    public void startManualMode(Map<Direction, Integer> manualCounts) {
        isAutoMode = false;
        trafficController.setVehicleCounts(manualCounts);
        trafficController.updateDurations();
        startSimulation();
    }
}