package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import java.util.List;
import java.util.Map;

public class CycleManager {
    private int currentIndex;
    private final List<Direction> directionOrder; // Döngü sırası, constructor'da başlatılacak
    private Direction activeDirection;
    private final TrafficController trafficController;

    public CycleManager(TrafficController trafficController) {
        this.trafficController = trafficController;
        // Standart trafik ışığı döngü sırası
        this.directionOrder = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    /**
     * Döngüyü başlatır veya sıfırlar.
     * Aktif yönü döngünün ilk yönüne ayarlar.
     */
    public void startCycle() {
        currentIndex = 0;
        if (!directionOrder.isEmpty()) {
            activeDirection = directionOrder.get(currentIndex);
        } else {
            activeDirection = null; // Hata durumu veya boş döngü
            System.err.println("CycleManager Error: Direction order is empty!");
        }
        // Konsola başlangıç bilgisini yazdıralım (Türkçe karakterler için getTurkishName() kullanılabilir)
        System.out.println("Döngü başlatıldı. İlk yön: " +
                (activeDirection != null ? activeDirection: "YOK") +
                ", Süre: " + getCurrentDuration() + "sn");
    }

    /**
     * Bir sonraki yöne geçer. Döngü sırasının sonuna gelindiğinde başa döner.
     */
    public void switchToNextDirection() {
        if (directionOrder.isEmpty()) {
            System.err.println("CycleManager Error: Cannot switch direction, order is empty!");
            return;
        }
        currentIndex = (currentIndex + 1) % directionOrder.size();
        activeDirection = directionOrder.get(currentIndex);
    }

    /**
     * Mevcut aktif yönü döndürür.
     * @return Aktif {@link Direction} nesnesi.
     */
    public Direction getCurrentDirection() {
        return activeDirection;
    }

    /**
     * Mevcut aktif yön için hesaplanmış yeşil ışık süresini döndürür.
     * @return Saniye cinsinden süre.
     */
    public int getCurrentDuration() {
        if (activeDirection == null) {
            return 0; // Aktif yön yoksa süre 0
        }
        return trafficController.getGreenDuration(activeDirection);
    }

    /**
     * Mevcut aktif yönde hala araç olup olmadığını kontrol eder.
     * (Bu metodun kullanımı mevcut döngü mantığında değişmiş olabilir, ancak genel bir yardımcı olarak kalabilir.)
     * @return Araç varsa true, yoksa false.
     */
    public boolean hasRemainingVehicle() {
        if (trafficController == null || trafficController.getVehicleCounts() == null || activeDirection == null) {
            return false;
        }
        Map<Direction, Integer> vehicleCounts = trafficController.getVehicleCounts();
        Integer count = vehicleCounts.get(activeDirection);
        return count != null && count > 0;
    }

    /**
     * Mevcut aktif yönün, tanımlanmış döngü sırasının son yönü olup olmadığını kontrol eder.
     * Bu, SimulationManager tarafından tam bir döngünün tamamlanıp tamamlanmadığını belirlemek için kullanılır.
     * @return Mevcut yön döngünün son yönü ise true, aksi halde false.
     */
    public boolean isEndOfCycle() {
        if (activeDirection == null || directionOrder == null || directionOrder.isEmpty()) {
            return false; // Başlatma hatası veya beklenmedik durum
        }
        // Aktif yön, directionOrder listesindeki son eleman ise, bu döngünün sonudur.
        return activeDirection == directionOrder.get(directionOrder.size() - 1);
    }
}