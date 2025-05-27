package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.*;

public final class VehicleAnimation {

    // Artırılmış kuyruk boşluğu: çarpışmayı önlemek için
    public static final double GAP_BETWEEN = 40.0;

    private static final Map<Direction, List<Vehicle>> laneVehicles = new EnumMap<>(Direction.class);
    private final SimulationManager simManager;
    private final AnimationTimer    timer;

    private Pane canvas;
    private boolean active = false;

    private Circle sensorNorth, sensorSouth, sensorEast, sensorWest;

    public VehicleAnimation(SimulationManager simManager) {
        this.simManager = simManager;
        for (Direction d : Direction.values()) laneVehicles.put(d, new ArrayList<>());
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (active && simManager.isRunning() && !simManager.isPaused()) {
                    processFrame();
                }
            }
        };
    }

    public void initializeVehicles(TrafficController controller, Pane canvas) {
        clearAllVehicles();
        this.canvas = canvas;
        if (canvas == null) return;
        addSensors();
        controller.getVehicleCounts().forEach(this::createQueueForLane);
    }

    public void startAnimation() { if (!active) { active = true; timer.start(); } }
    public void stopAnimation()  { if (active)  { active = false; timer.stop(); } }
    public void clearAllVehicles() {
        stopAnimation();
        if (canvas != null) {
            laneVehicles.values().stream()
                    .flatMap(List::stream)
                    .map(Vehicle::getView)
                    .forEach(canvas.getChildren()::remove);
            canvas.getChildren().removeAll(sensorNorth, sensorSouth, sensorEast, sensorWest);
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
        switch (dir) {
            case NORTH -> { baseX = 637; baseY = 559; }
            case SOUTH -> { baseX = 550; baseY = -100; }
            case EAST  -> { baseX = 29;  baseY = 255; }
            case WEST  -> { baseX = 1174; baseY = 180; }
        }
        for (int i = 0; i < count; i++) {
            double x = baseX, y = baseY;
            switch (dir) {
                case NORTH ->
                        {
                            y += i * GAP_BETWEEN;

                        }
                case SOUTH ->
                        {
                            y -= i * GAP_BETWEEN;

                        }
                case EAST  ->
                        {
                            x -= i * GAP_BETWEEN;
                       }
                case WEST  ->
                        {
                            x += i * GAP_BETWEEN;

                        }
            }
            Vehicle v = new Vehicle(dir, x, y);
            list.add(v);
            canvas.getChildren().add(v.getView());
        }
    }

    private void processFrame() {
        laneVehicles.forEach((dir, list) ->
                list.forEach(v -> v.move(simManager.getLightPhaseForDirection(dir))));
        laneVehicles.values().forEach(list -> list.forEach(this::checkSensorIntersection));
        laneVehicles.values().forEach(list -> list.forEach(this::checkSensorExit));
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

    /** Kavşak içindeki araç çıkış çizgisini geçtiğinde işaretle. */
    private void checkSensorExit(Vehicle v) {
        if (!v.isInIntersection() || v.hasPassedIntersection()) return;
        switch (v.getDirection()) {
            case NORTH -> {
                if (v.getView().getLayoutY() < sensorNorth.getLayoutY() - sensorNorth.getRadius()) {
                    v.markPassedIntersection();
                }
            }
            case SOUTH -> {
                if (v.getView().getLayoutY() > sensorSouth.getLayoutY() + sensorSouth.getRadius()) {
                    v.markPassedIntersection();
                }
            }
            case EAST -> {
                if (v.getView().getLayoutX() > sensorEast.getLayoutX() + sensorEast.getRadius()) {
                    v.markPassedIntersection();
                }
            }
            case WEST -> {
                if (v.getView().getLayoutX() < sensorWest.getLayoutX() - sensorWest.getRadius()) {
                    v.markPassedIntersection();
                }
            }
        }
    }

    /** Araç sahne sınırını aştıysa node’u panelden sil ve true döndür. */
    private boolean outOfCanvas(Vehicle v) {
        if (canvas == null) return false;
        double w = canvas.getWidth(), h = canvas.getHeight();
        double x = v.getView().getLayoutX(), y = v.getView().getLayoutY();
        boolean outside = x < -120 || x > w + 120 || y < -120 || y > h + 120;
        if (outside) canvas.getChildren().remove(v.getView());
        return outside;
    }

    public static Vehicle getFrontVehicle(Vehicle me) {
        List<Vehicle> lane = laneVehicles.get(me.getDirection());
        int idx = lane.indexOf(me);
        return (idx > 0 ? lane.get(idx - 1) : null);
    }
}
