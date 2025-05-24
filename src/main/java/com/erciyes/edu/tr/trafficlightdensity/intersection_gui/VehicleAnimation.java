package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;


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

    private static final double VEHICLE_QUEUE_START_OFFSET = 200;
    private static final double LANE_OFFSET_FROM_CENTER = Vehicle.DEFAULT_WIDTH * 0.75;

    public VehicleAnimation(SimulationManager simManager) {
        this.vehiclesByDirection = new HashMap<>();
        for (Direction dir : Direction.values()) {
            vehiclesByDirection.put(dir, new ArrayList<>());
        }
        this.simulationManagerRef = simManager;

        animationTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {

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
                double vehicleSpacing = Vehicle.DEFAULT_LENGTH + Vehicle.SAFE_DISTANCE_BUFFER * 5; // Araçlar arası daha fazla boşluk

                switch (dir) {
                    case NORTH: // Kuzeye gidecekler, ekranın altından (güneyden) gelir – sağ şerit doğru
                        startX = Vehicle.INTERSECTION_CENTER_X + LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        startY = Vehicle.INTERSECTION_CENTER_Y + VEHICLE_QUEUE_START_OFFSET + (i * vehicleSpacing);
                        break;

                    case SOUTH: // Güneye gidecekler, ekranın üstünden (kuzeyden) gelir – sağ şeride hizalandı
                        startX = 550.0;
                        startY = Vehicle.INTERSECTION_CENTER_Y - VEHICLE_QUEUE_START_OFFSET - Vehicle.DEFAULT_LENGTH - (i * vehicleSpacing);
                        break;

                    case EAST:  // Doğuya gidecekler, ekranın solundan (batıdan) gelir – sağ şeride hizalandı
                        startX = Vehicle.INTERSECTION_CENTER_X - VEHICLE_QUEUE_START_OFFSET - Vehicle.DEFAULT_LENGTH - (i * vehicleSpacing);
                        startY = Vehicle.INTERSECTION_CENTER_Y + LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
                        break;

                    case WEST:  // Batıya gidecekler, ekranın sağından (doğudan) gelir – sağ şerit doğru
                        startX = Vehicle.INTERSECTION_CENTER_X + VEHICLE_QUEUE_START_OFFSET + (i * vehicleSpacing);
                        startY = Vehicle.INTERSECTION_CENTER_Y - LANE_OFFSET_FROM_CENTER - Vehicle.DEFAULT_WIDTH / 2;
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
//        if (simulationManagerRef == null) return;

        for (Direction dir : Direction.values()) {
            List<Vehicle> laneVehicles = vehiclesByDirection.get(dir);

//            for (int i = 0; i < laneVehicles.size(); i++) {
//                Vehicle currentVehicle = laneVehicles.get(i);
//                Vehicle leadingVehicle = null;
//            }

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