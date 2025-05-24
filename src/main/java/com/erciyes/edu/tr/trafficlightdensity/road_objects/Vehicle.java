package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Vehicle represents a single, simple car in the simulation.
 *
 * Behaviour rules:
 * <ul>
 *     <li>If the traffic light in its lane is <b>GREEN</b>, the vehicle moves forward at {@code DEFAULT_SPEED}.</li>
 *     <li>Otherwise (<b>RED</b> or <b>YELLOW</b>), it stops exactly where it is.</li>
 * </ul>
 * No intersection geometry, collision‑avoidance, or queue logic is included.
 * Each vehicle is given a random pastel colour when created so it is easy to
 * distinguish on screen.
 */
public class Vehicle {

    // ─────────────────────────────────────────────────────────────────────────────
    //  CONSTANTS
    // ─────────────────────────────────────────────────────────────────────────────
    public static final double DEFAULT_LENGTH = 30.0;  // px – along the direction of travel
    public static final double DEFAULT_WIDTH  = 15.0;  // px – perpendicular to travel
    public static final double DEFAULT_SPEED  = 2.0;   // px per animation frame

    // ─────────────────────────────────────────────────────────────────────────────
    //  STATE
    // ─────────────────────────────────────────────────────────────────────────────
    private final String id;
    private final Direction direction;

    private double x;          // top‑left X (px)
    private double y;          // top‑left Y (px)
    private double speed;      // current speed (px/frame)

    private final Node view;   // JavaFX node rendered on the canvas

    // ─────────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Builds a new vehicle instance.
     *
     * @param id        unique identifier (useful for debugging)
     * @param direction direction of travel (NORTH, SOUTH, EAST, WEST)
     * @param startX    initial X position
     * @param startY    initial Y position
     */
    public Vehicle(String id, Direction direction, double startX, double startY) {
        this.id        = id;
        this.direction = direction;
        this.x         = startX;
        this.y         = startY;
        this.speed     = 0; // start stationary

        // Rectangle used to draw the vehicle
        Rectangle rect = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);

        // Rotate for east/west so the long side faces the correct axis
        if (direction == Direction.EAST || direction == Direction.WEST) {
            rect.setWidth(DEFAULT_LENGTH);
            rect.setHeight(DEFAULT_WIDTH);
        }

        rect.setFill(randomPastel());   // random colour per vehicle
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(0.5);

        this.view = rect;
        updateViewPosition();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Advances the vehicle by one animation tick.
     *
     * @param currentPhase traffic‑light phase for this vehicle's lane.
     *                     <ul>
     *                         <li>GREEN  → move at {@link #DEFAULT_SPEED}</li>
     *                         <li>RED/YELLOW → stop</li>
     *                     </ul>
     */
    public void move(LightPhase currentPhase) {
        // 1 – determine speed from light phase
        speed = (currentPhase == LightPhase.GREEN) ? DEFAULT_SPEED : 0;

        // 2 – update logical position
        switch (direction) {
            case NORTH -> y -= speed;
            case SOUTH -> y += speed;
            case EAST  -> x += speed;
            case WEST  -> x -= speed;
        }

        // 3 – push to JavaFX node
        updateViewPosition();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    /** Synchronises the JavaFX node with the logical coordinates. */
    private void updateViewPosition() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }

    /**
     * Generates a random pastel colour (low saturation, high brightness) so that
     * vehicles look distinct yet unobtrusive.
     */
    private static Color randomPastel() {
        double hue = ThreadLocalRandom.current().nextDouble(0, 360);
        double saturation = 0.4;   // pastel feel
        double brightness  = 0.85; // keep it light
        return Color.hsb(hue, saturation, brightness);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  GETTERS
    // ─────────────────────────────────────────────────────────────────────────────

    public Node getView()           { return view; }
    public String getId()           { return id; }
    public Direction getDirection() { return direction; }
}
