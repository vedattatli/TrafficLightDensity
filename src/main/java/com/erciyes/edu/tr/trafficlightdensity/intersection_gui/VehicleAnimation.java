package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
// import javafx.scene.Node; // Gerekirse

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class VehicleAnimation {
    private Map<Direction, List<Vehicle>> vehiclesByDirection;
    private Pane mainPane; // Araçların çizileceği ana panel
    private AnimationTimer animationTimer;
    private SimulationManager simulationManagerRef; // Işık durumlarını almak için
    private boolean isAnimationRunning = false;

    // --- BAŞLANGIÇ POZİSYONLARI VE ŞERİT KOORDİNATLARI (MUTLAKA FXML'İNİZE GÖRE AYARLAYIN!) ---
    // Bu değerler, araçların kavşağın DIŞINDA, kendi şeritlerinde sıralanacağı yerlerdir.
    // Örnek: Kuzey'e giden araçlar ekranın altından başlar, Batı'ya gidenler sağından başlar.
    // Aracın (sol-üst) köşe noktası için varsayımsal değerler.
    private static final double VEHICLE_QUEUE_START_OFFSET = 200; // Kavşak merkezinden ne kadar uzakta sıraya girecekler
    private static final double LANE_OFFSET_FROM_CENTER = Vehicle.DEFAULT_WIDTH * 0.75; // Şeridin merkezden ne kadar kayık olacağı

    public VehicleAnimation(SimulationManager simManager) {
        this.vehiclesByDirection = new HashMap<>();
        for (Direction dir : Direction.values()) {
            vehiclesByDirection.put(dir, new ArrayList<>());
        }
        this.simulationManagerRef = simManager;

        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0; // Akıcı animasyon için zaman kontrolü (isteğe bağlı)

            @Override
            public void handle(long now) {
                // İsteğe bağlı: Delta time ile daha düzgün animasyon
                // if (now - lastUpdate < 16_000_000) { return; } // ~60 FPS
                // lastUpdate = now;
                if (isAnimationRunning && simulationManagerRef != null && simulationManagerRef.isRunning() && !simulationManagerRef.isPaused()) {
                    updateVehiclePositions();
                }
            }
        };
    }

    public void initializeVehicles(TrafficController trafficController, Pane mainPane) {
        clearAllVehicles(); // Önceki simülasyondan kalan araçları temizle
        this.mainPane = mainPane;
        if (this.mainPane == null) {
            System.err.println("VehicleAnimation Error: MainPane null, araçlar çizilemiyor.");
            return;
        }
        Map<Direction, Integer> vehicleCounts = trafficController.getVehicleCounts();

        for (Direction dir : Direction.values()) {
            int count = vehicleCounts.getOrDefault(dir, 0);
            List<Vehicle> laneVehicles = vehiclesByDirection.get(dir); // Bu yöne ait araç listesini al
            laneVehicles.clear(); // Listeyi temizle (initialize her çağrıldığında sıfırdan başlasın)

            double startX = 0, startY = 0;
            // Araçları kendi yönlerine göre kavşağın dışında, geriye doğru sırala
            for (int i = 0; i < count; i++) {
                String vehicleId = dir.name().charAt(0) + "_Car" + (i + 1);
                // Başlangıç pozisyonlarını yönlerine ve sıralarına göre ayarla
                // Araçlar birbirinin içine girmemeli, aralarında boşluk olmalı
                double vehicleSpacing = Vehicle.DEFAULT_LENGTH + Vehicle.SAFE_DISTANCE_BUFFER * 5; // Araçlar arası daha fazla boşluk

                switch (dir) {
                    case NORTH: // Kuzeye gidecekler, ekranın altından (güneyden) gelir
                        startX = Vehicle.INTERSECTION_CENTER_X - LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        startY = Vehicle.INTERSECTION_CENTER_Y + VEHICLE_QUEUE_START_OFFSET + (i * vehicleSpacing);
                        break;
                    case SOUTH: // Güneye gidecekler, ekranın üstünden (kuzeyden) gelir
                        startX = Vehicle.INTERSECTION_CENTER_X + LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        startY = Vehicle.INTERSECTION_CENTER_Y - VEHICLE_QUEUE_START_OFFSET - Vehicle.DEFAULT_LENGTH - (i * vehicleSpacing);
                        break;
                    case EAST:  // Doğuya gidecekler, ekranın solundan (batıdan) gelir
                        startX = Vehicle.INTERSECTION_CENTER_X - VEHICLE_QUEUE_START_OFFSET - Vehicle.DEFAULT_LENGTH - (i * vehicleSpacing);
                        startY = Vehicle.INTERSECTION_CENTER_Y - LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        break;
                    case WEST:  // Batıya gidecekler, ekranın sağından (doğudan) gelir
                        startX = Vehicle.INTERSECTION_CENTER_X + VEHICLE_QUEUE_START_OFFSET + (i * vehicleSpacing);
                        startY = Vehicle.INTERSECTION_CENTER_Y + LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        break;
                }
                Vehicle vehicle = new Vehicle(vehicleId, dir, startX, startY);
                laneVehicles.add(vehicle);
                this.mainPane.getChildren().add(vehicle.getView());
            }
        }
        System.out.println("Araçlar başlatıldı ve pane'e eklendi.");
    }

    private void updateVehiclePositions() {
        if (simulationManagerRef == null) return;

        for (Direction dir : Direction.values()) {
            List<Vehicle> laneVehicles = vehiclesByDirection.get(dir);
            // Araçları kavşağa yakınlıklarına göre sıralamak gerekebilir, şimdilik listedeki sıraya göre işleyelim.
            // Öndeki aracı belirlemek için, listenin başından (kavşağa en uzak) sonuna (kavşağa en yakın) doğru gidelim.
            // Ya da tam tersi: kavşağa en yakın olanı önce hareket ettir, sonra arkasındakini.
            // Çarpışma önleme için, her aracın bir önündeki aracı bilmesi lazım.
            // Listeyi, araçların gerçek mekansal sıralamasına göre (kavşağa en yakın olan en sonda) tuttuğumuzu varsayalım.
            // VEYA her araç için listedeki bir önceki aracı "leadingVehicle" olarak alalım.
            // initializeVehicles'daki sıralama (i artarken) kavşaktan uzaklaşan bir sıra oluşturuyor.
            // Bu yüzden öndeki aracı bulmak için i-1 indeksi doğru olabilir (eğer i=0 en öndeki ise).
            // Şimdiki sıralama (i=0 en uzaktaki):

            for (int i = 0; i < laneVehicles.size(); i++) {
                Vehicle currentVehicle = laneVehicles.get(i);
                Vehicle leadingVehicle = null; // Bu şeritteki bir sonraki araç (kavşağa daha yakın olan)

                // Öndeki aracı bulma mantığı (mevcut sıralamaya göre i > 0 ise laneVehicles.get(i-1) öndeki olur)
                // Ancak bu, araçların listedeki sırasının mekansal sıralarını koruduğunu varsayar.
                // Basitlik için: Her araç kendinden bir önceki aracı (eğer varsa) leading olarak kabul etsin.
                // Bu, initializeVehicles'taki sıralamaya göre: i=0 en uzaktaki, i=size-1 en yakındaki.
                // Bu durumda, i'inci aracın önündeki araç (kavşağa daha yakın olan) i+1'inci araç olurdu (yanlış).
                // Doğrusu: Eğer liste kavşağa en yakın olan en başta olacak şekilde sıralıysa, i-1 öndeki olur.
                // Ya da her aracın önündekini dinamik olarak bulmak lazım.
                // Şimdilik, listenin i'inci elemanı için i-1'inci elemanı "öndeki" (ona yetişmeye çalıştığı) kabul edelim.
                // Bu, araçların listede kavşağa göre sıralı olduğunu (en öndeki ilk eleman) varsayar.
                // Initialize'daki sıralama tersi olduğu için bu mantığı düzeltmek lazım.

                // Geçici olarak öndeki araç mantığını basitleştiriyorum:
                if (i > 0) { // Eğer ilk araç değilse (kavşaktan en uzak olan)
                    // Mevcut sıralamada (i=0 en uzak) i-1. araç daha da uzaktadır.
                    // Bizim i'nci aracımızın önündeki araç, listede daha sonraki bir indekste olmalı
                    // VEYA liste ters sıralı olmalı.
                    // Şimdilik, en basit haliyle, öndeki araç kontrolünü daha sonra detaylandıracağımızı varsayalım.
                    // Öndeki aracı doğru bulmak için araçları her tick'te sıralamak veya dikkatli yönetmek gerekir.
                    // Şimdilik leadingVehicle = null bırakıyorum, sonraki adımda eklenebilir.
                    // Veya listenin sonundan başına doğru iterate edelim:
                }

                // Doğru öndeki araç mantığı için:
                // Listenin sonundan (kavşağa en yakın) başına doğru işleyelim.
                // V for (int i = laneVehicles.size() - 1; i >= 0; i--) {
                // V    Vehicle currentVehicle = laneVehicles.get(i);
                // V    Vehicle leadingVehicle = (i < laneVehicles.size() - 1) ? laneVehicles.get(i + 1) : null;

                // Şimdiki implementasyonla (i=0 en uzak):
                // leadingVehicle = (i > 0) ? laneVehicles.get(i - 1) : null; // Bu, i-1'in currentVehicle'ın ARKASINDA olduğu anlamına gelir.
                // Doğrusu, leadingVehicle currentVehicle'ın ÖNÜNDE (kavşağa daha yakın veya kavşak içinde) olmalı.
                // Bu yüzden her bir araç için listedeki diğer tüm araçları kontrol edip en yakın öndekini bulmak gerekebilir.
                // Ya da araçları her zaman pozisyonlarına göre sıralı tutmak gerekir.

                // En basit çarpışma önleme: Listenin başından sonuna doğru işlerken, bir araç kendinden
                // bir önceki (indeks olarak) aracı leading kabul eder. Bu, araçların
                // listede kavşağa en yakın olandan en uzak olana doğru sıralı olması GEREKTİRİR.
                // initializeVehicles'da sıralama tersi (en uzak olan başta).
                // Bu yüzden iterate ederken öndeki aracı doğru belirlemek için dikkatli olmalıyız.
                // Şimdilik, i.indeksteki aracın önündeki, i+1. indeksteki araçmış gibi (eğer varsa) basit bir varsayım yapalım.
                // Bu durumda en öndeki araç (i=size-1) için leading null olur.

                // Kavşağa en yakın olan araçtan başlayarak işleyelim (listenin sonundan)
                // Bu durumda bir önceki işlenen araç "öndeki araç" olur.
            }
            // Geçici çözüm: Öndeki aracı null geçelim, sadece ışıklara göre hareket etsinler.
            // Daha sonra çarpışma önleme eklenebilir.
            for (Vehicle currentVehicle : laneVehicles) {
                LightPhase lightPhase = simulationManagerRef.getLightPhaseForDirection(currentVehicle.getDirection());
                currentVehicle.move(lightPhase, null); // Şimdilik leadingVehicle null
            }


        }

        // Kavşağı geçen araçları temizle
        for (List<Vehicle> laneVehicles : vehiclesByDirection.values()) {
            Iterator<Vehicle> iterator = laneVehicles.iterator();
            while (iterator.hasNext()) {
                Vehicle vehicle = iterator.next();
                if (vehicle.getState() == Vehicle.VehicleState.PASSED) {
                    if (mainPane != null) {
                        mainPane.getChildren().remove(vehicle.getView());
                    }
                    iterator.remove();
                    System.out.println(vehicle.getId() + " kavşağı geçti ve kaldırıldı.");
                }
            }
        }
    }

    public void startAnimation() {
        if (!isAnimationRunning) {
            isAnimationRunning = true;
            animationTimer.start();
            System.out.println("Araç animasyonu başlatıldı.");
        }
    }

    public void stopAnimation() {
        if (isAnimationRunning) {
            isAnimationRunning = false;
            animationTimer.stop();
            System.out.println("Araç animasyonu durduruldu.");
        }
    }

    public void clearAllVehicles() {
        stopAnimation(); // Animasyonu durdur
        if (mainPane != null) {
            for (List<Vehicle> laneVehicles : vehiclesByDirection.values()) {
                for (Vehicle vehicle : laneVehicles) {
                    mainPane.getChildren().remove(vehicle.getView());
                }
            }
        }
        for (List<Vehicle> laneVehicles : vehiclesByDirection.values()) {
            laneVehicles.clear();
        }
        System.out.println("Tüm araçlar temizlendi.");
    }
}