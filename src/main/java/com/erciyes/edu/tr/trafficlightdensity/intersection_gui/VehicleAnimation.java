package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.*;

/* ───────────────────────────── Constants ───────────────────────────── */
/*  Bu bölümde kavşağın geometrisi ve animasyon için sabit kullanılan
    değerler tanımlanır.  */
public final class VehicleAnimation {

    /* Araçlar arası boşluk: 1 araç boyu + 2 araç genişliği (tampon mesafe). */
    private static final double GAP_BETWEEN  =
            Vehicle.DEFAULT_LENGTH + Vehicle.DEFAULT_WIDTH * 2.0;

    /* ─────────────────────────── Fields ──────────────────────────────── */
    /* Her yön (N,E,S,W) için bağlı araç listesi tutulur. */
    private final Map<Direction, List<Vehicle>> laneVehicles =
            new EnumMap<>(Direction.class);

    private final SimulationManager simManager;    // Işık fazları buradan alınır
    private final AnimationTimer    timer;         // JavaFX animasyon döngüsü

    private Pane canvas;           // Araçların çizildiği ana panel
    private boolean active = false;

    /* Dört görünmez sensör (kavşak giriş çizgileri). */
    private Circle sensorNorth;
    private Circle sensorSouth;
    private Circle sensorEast;
    private Circle sensorWest;

    /* ───────────────────────── Constructor ──────────────────────────── */
    public VehicleAnimation(SimulationManager simManager) {
        this.simManager = simManager;
        for (Direction d : Direction.values()) laneVehicles.put(d, new ArrayList<>());

        /* Her karede handle() çağrılır; simülasyon durmadıysa
           araçların konumu güncellenir. */
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (active && simManager.isRunning() && !simManager.isPaused()) {
                    processFrame();
                }
            }
        };
    }

    /* ───────────────────────── Public API ───────────────────────────── */
    /** Kuyrukları oluştur ve sensörleri yerleştir. */
    public void initializeVehicles(TrafficController controller, Pane canvas) {
        clearAllVehicles();        // Önce eski nesneleri temizle
        this.canvas = canvas;
        if (canvas == null) {
            System.err.println("Canvas NULL – çizim yapılamadı!");
            return;
        }
        addSensors();              // Görünmez sensörleri ekle
        controller.getVehicleCounts().forEach(this::createQueueForLane);
    }

    /** Animasyonu başlat. */
    public void startAnimation()  { if (!active) { active = true;  timer.start(); } }
    /** Animasyonu durdur. */
    public void stopAnimation()   { if (active)  { active = false; timer.stop();  } }

    /** Tüm araç ve sensörleri sahneden/bellekten sil. */
    public void clearAllVehicles() {
        stopAnimation();
        if (canvas != null) {
            laneVehicles.values().stream()
                    .flatMap(List::stream)
                    .map(Vehicle::getView)
                    .forEach(canvas.getChildren()::remove);

            canvas.getChildren().removeAll(
                    sensorNorth, sensorSouth, sensorEast, sensorWest
            );
        }
        laneVehicles.values().forEach(List::clear);
        sensorNorth = sensorSouth = sensorEast = sensorWest = null;
    }

    /* ───────────────────── Sensor Helpers ───────────────────────────── */
    /** Dört görünmez sensör dikdörtgenini kavşak merkezine yerleştirir. */
    private void addSensors() {
        if (sensorNorth != null) return;   // Zaten eklendi


        sensorNorth = createSensor(637,313,7);

        sensorSouth = createSensor(550,100,7);

        sensorEast  = createSensor(459,255,7);

        sensorWest  = createSensor(700,180,7);

        canvas.getChildren().addAll(sensorNorth, sensorSouth, sensorEast, sensorWest);
    }

    /** Şeffaf sensör circle oluşturur. */
    private Circle createSensor(double x, double y, double r) {
        Circle circle = new Circle(r);
        circle.setFill(Color.TRANSPARENT);
        circle.setLayoutX(x);
        circle.setLayoutY(y);
        circle.setMouseTransparent(true);   // Fare olaylarını bloklamasın
        return circle;
    }

    /* ───────────────────── Queue Builders ───────────────────────────── */
    /** Verilen yöne (lane) istenen adet araç ekler. */
    private void createQueueForLane(Direction dir, int count) {
        List<Vehicle> list = laneVehicles.get(dir);
        double baseX = 0, baseY = 0;

        /* İlk aracın koordinatı – şerit/geçiş yönüne göre. */
        switch (dir) {
            case NORTH -> {
                baseX = 637;
                baseY = 559;
            }
            case SOUTH -> {
                baseX = 550;
                baseY = -100;
            }
            case EAST -> {
                baseX = 29;
                baseY = 255;
            }
            case WEST -> {
                baseX = 1174;
                baseY = 180;
            }
        }

        /* Kuyruk oluştur: her sonraki araç sabit aralıkla ofsetlenir. */
        for (int i = 0; i < count; i++) {
            double x = baseX, y = baseY;
            switch (dir) {
                case NORTH -> y += i * GAP_BETWEEN;
                case SOUTH -> y -= i * GAP_BETWEEN;
                case EAST  -> x -= i * GAP_BETWEEN;
                case WEST  -> x += i * GAP_BETWEEN;
            }
            Vehicle v = new Vehicle(dir, x, y);
            list.add(v);
            canvas.getChildren().add(v.getView());
        }
    }

    /* ───────────────────── Main Animation Loop ─────────────────────── */
    /** Her kare çağrılır: araçlar ilerletilir, sensör/kaldırma kontrolleri yapılır. */
    private void processFrame() {
        /* 1. Işık rengine göre araçları hareket ettir. */
        laneVehicles.forEach((dir, list) ->
                list.forEach(v -> v.move(simManager.getLightPhaseForDirection(dir))));

        /* 2. Araç sensöre girdiyse kavşakta olduğunu işaretle. */
        laneVehicles.values().forEach(list -> list.forEach(this::checkSensorIntersection));

        /* 3. Ekran dışına çıkanları temizle. */
        laneVehicles.values().forEach(list -> list.removeIf(this::outOfCanvas));
    }

    /* ───────────────────── Helper Methods ─────────────────────────── */
    /** Araç sensöre temas ediyorsa kavşağa girdi olarak işaretle. */
    private void checkSensorIntersection(Vehicle v) {
        if (v.isInIntersection()) return; // Zaten işaretli
        Circle s = switch (v.getDirection()) {
            case NORTH -> sensorNorth;
            case SOUTH -> sensorSouth;
            case EAST  -> sensorEast;
            case WEST  -> sensorWest;
        };
        if (v.getView().getBoundsInParent().intersects(s.getBoundsInParent())) {
            v.markInsideIntersection();
        }
    }

    /** Araç sahne sınırını aştıysa node’u panelden sil ve true döndür. */
    private boolean outOfCanvas(Vehicle v) {
        if (canvas == null) return false;
        double w = canvas.getWidth(), h = canvas.getHeight();
        double x = v.getView().getLayoutX(), y = v.getView().getLayoutY();
        boolean outside = x < -100 || x > w + 100 || y < -100 || y > h + 100;
        if (outside) canvas.getChildren().remove(v.getView());
        return outside;
    }
}
