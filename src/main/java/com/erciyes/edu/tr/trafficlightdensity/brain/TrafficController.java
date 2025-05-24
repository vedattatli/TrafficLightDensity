package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.HashMap;
import java.util.Map;

// Sensörlerden gelen yoğunluk verisiyle her yöne yeşil süresi hesaplar.
public class TrafficController {

    public final static int TOTAL_CYCLE_TIME = 120;
    public final static int YELLOW_DURATION = 3;

    Map<Direction, Integer> vehicleCount;
    HashMap<Direction, Integer> greenDurations;

    private int totalVehicleCount;

    /**
     * TrafficController constructor.
     * vehicleCount ve greenDurations map'lerini başlatır.
     */
    public TrafficController() {
        this.vehicleCount = new HashMap<>(); // vehicleCount map'ini burada başlatın
        this.greenDurations = new HashMap<>(); // greenDurations map'ini de burada başlatın
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
        totalVehicleCount = 0; // yeniden hesaplamaya başlarken sıfırla
        if (vehicleCount != null) { // vehicleCount null değilse döngüye gir
            for (int count : vehicleCount.values()) {
                totalVehicleCount += count;
            }
        }
        return totalVehicleCount;
    }

    private void calculateGreenDurations() {
        greenDurations = new HashMap<>(); // Her hesaplamada sıfırdan oluşturmak daha güvenli olabilir.

        if (totalVehicleCount == 0 || vehicleCount == null || vehicleCount.isEmpty()) { // vehicleCount null veya boş ise kontrol et
            // Eğer vehicleCount başlangıçta boşsa (örneğin rastgele mod için henüz set edilmediyse)
            // ve kullanıcı girişiyle de doldurulmadıysa, tüm yönlere 0 süresi ata.
            // Bu durum, Direction.values() kullanılarak tüm olası yönler için yapılabilir.
            for (Direction direction : Direction.values()) { // Enum'daki tüm yönler için
                greenDurations.put(direction, 0);
            }
            return;
        }

        for (Direction direction : vehicleCount.keySet()) {
            int count = vehicleCount.getOrDefault(direction, 0); // NullPointer'dan kaçınmak için getOrDefault
            int duration = (int) ((count / (double) totalVehicleCount) * (TOTAL_CYCLE_TIME - 4 * YELLOW_DURATION));
            greenDurations.put(direction, duration);
        }
    }

    public void updateDurations() {
        calculateTotalVehicleCount();
        calculateGreenDurations();
    }
}