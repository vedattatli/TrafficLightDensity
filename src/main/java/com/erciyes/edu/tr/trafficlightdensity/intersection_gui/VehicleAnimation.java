package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;

import java.util.*;

/**
 *  * Extremely simplified animation loop that matches the streamlined {@link Vehicle}
 *  class ("green → move", "red/yellow → stop").
 *
 *  * <p>All intersection-geometry constants live <em>here</em> now so the {@code Vehicle}
 *  class can stay 100 % generic.</p>
 *
 *  * <p><b>Compatibility Note:</b> Original controller classes referenced the old
 *  API names (<code>initializeVehicles</code>, <code>startAnimation</code>, etc.).
 *  Thin wrapper methods have been added so you can compile without touching the
 *  UI code. New preferred names are <code>initialiseVehicles</code>,
 *  <code>start</code>, <code>stop</code>, and <code>clear</code>.</p>
 */
public final class VehicleAnimation {

    // ─────────────────────────────────────────────────────────────────────────────
    //  CONFIG – tune these to match your FXML canvas size
    // ─────────────────────────────────────────────────────────────────────────────

    private static final double INTERSECTION_CENTER_X  = 545.5;      // px
    private static final double INTERSECTION_CENTER_Y  = 250.0;      // px
    private static final double VEHICLE_QUEUE_OFFSET   = 200.0;      // px – how far back the first queued car starts
    private static final double LANE_OFFSET            = Vehicle.DEFAULT_WIDTH * 0.75; // px – half-lane from centre
    private static final double EXTRA_SPACING          = Vehicle.DEFAULT_LENGTH + (Vehicle.DEFAULT_WIDTH * 2.0);

    // ─────────────────────────────────────────────────────────────────────────────
    //  INSTANCE FIELDS
    // ─────────────────────────────────────────────────────────────────────────────

    private final Map<Direction, List<Vehicle>> lanes = new EnumMap<>(Direction.class);
    private final SimulationManager simManager;
    private final AnimationTimer timer;

    private Pane canvas;
    private boolean running = false;

    // ─────────────────────────────────────────────────────────────────────────────
    //  CTOR
    // ─────────────────────────────────────────────────────────────────────────────

    public VehicleAnimation(SimulationManager simManager) {
        this.simManager = simManager;
        for (Direction d : Direction.values()) lanes.put(d, new ArrayList<>());

        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (running && simManager != null && simManager.isRunning() && !simManager.isPaused()) {
                    tick();
                }
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  PUBLIC API (new preferred names)
    // ─────────────────────────────────────────────────────────────────────────────

    /** Builds queues for every direction & adds nodes to <code>mainPane</code>. */
    public void initialiseVehicles(TrafficController controller, Pane mainPane) {
        clear();
        this.canvas = mainPane;
        if (canvas == null) {
            System.err.println("VehicleAnimation ▶ mainPane is null; cannot render vehicles.");
            return;
        }

        controller.getVehicleCounts().forEach(this::createQueue);
        System.out.println("Vehicles initialised & added to canvas.");
    }

    /** Starts the per‑frame animation loop. */
    public void start()  { if (!running) { running = true;  timer.start(); } }
    /** Stops (pauses) the animation loop. */
    public void stop()   { if (running)  { running = false; timer.stop();  } }
    /** Removes all vehicles from both canvas and internal lists. */
    public void clear() {
        stop();
        if (canvas != null) {
            lanes.values().stream().flatMap(List::stream).map(Vehicle::getView).forEach(canvas.getChildren()::remove);
        }
        lanes.values().forEach(List::clear);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  LEGACY WRAPPERS (for existing UI code)
    // ─────────────────────────────────────────────────────────────────────────────

    public void initializeVehicles(TrafficController c, Pane p) { initialiseVehicles(c, p); }
    public void startAnimation()                                { start();                 }
    public void stopAnimation()                                 { stop();                  }
    public void clearAllVehicles()                              { clear();                 }

    // ─────────────────────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    private void createQueue(Direction dir, int count) {
        List<Vehicle> list = lanes.get(dir);
        double baseX = 0, baseY = 0; // position for first in the queue

        switch (dir) {
            case NORTH -> {
                baseX = INTERSECTION_CENTER_X + LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2;
                baseY = INTERSECTION_CENTER_Y + VEHICLE_QUEUE_OFFSET;
            }
            case SOUTH -> {
                baseX = INTERSECTION_CENTER_X - LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2 + Vehicle.DEFAULT_WIDTH; // visual tweak
                baseY = INTERSECTION_CENTER_Y - VEHICLE_QUEUE_OFFSET - Vehicle.DEFAULT_LENGTH;
            }
            case EAST -> {
                baseX = INTERSECTION_CENTER_X - VEHICLE_QUEUE_OFFSET - Vehicle.DEFAULT_LENGTH;
                baseY = INTERSECTION_CENTER_Y + LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2;
            }
            case WEST -> {
                baseX = INTERSECTION_CENTER_X + VEHICLE_QUEUE_OFFSET;
                baseY = INTERSECTION_CENTER_Y - LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2;
            }
        }

        for (int i = 0; i < count; i++) {
            double x = baseX;
            double y = baseY;
            switch (dir) {
                case NORTH -> y += i * EXTRA_SPACING;
                case SOUTH -> y -= i * EXTRA_SPACING;
                case EAST  -> x -= i * EXTRA_SPACING;
                case WEST  -> x += i * EXTRA_SPACING;
            }
            Vehicle v = new Vehicle(dir.name().charAt(0) + "_Car" + (i + 1), dir, x, y);
            list.add(v);
            canvas.getChildren().add(v.getView());
        }
    }

    /** Single animation step for <em>all</em> vehicles. */
    private void tick() {
        for (Map.Entry<Direction, List<Vehicle>> entry : lanes.entrySet()) {
            LightPhase phase = simManager.getLightPhaseForDirection(entry.getKey());
            for (Vehicle v : entry.getValue()) v.move(phase);
        }
        lanes.values().forEach(list -> list.removeIf(this::pruneIfOffScreen));
    }

    /** Returns true <em>and</em> removes the node if the car has driven out of view. */
    private boolean pruneIfOffScreen(Vehicle v) {
        if (canvas == null) return false;
        double w = canvas.getWidth(), h = canvas.getHeight();
        double x = v.getView().getLayoutX(), y = v.getView().getLayoutY();
        boolean off = x < -100 || x > w + 100 || y < -100 || y > h + 100;
        if (off) canvas.getChildren().remove(v.getView());
        return off;
    }
}
