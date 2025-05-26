package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.concurrent.ThreadLocalRandom;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.LightPhase;

/**
 * Basit araç sınıfı.
 * <ul>
 *   <li>Sensöre (stop çizgisi) kadar ışığa bakmadan sabit hızla ilerler.</li>
 *   <li>Sensördeyken ışığa göre durur / yavaşlar / geçer.</li>
 *   <li>Kavşağı tamamen geçince tekrar hızlanır.</li>
 * </ul>
 */
public class Vehicle {

    // Sabitler
    public static final double DEFAULT_LENGTH = 30.0; // px
    public static final double DEFAULT_WIDTH  = 15.0; // px
    public static final double DEFAULT_SPEED  = 2.0;  // px/kare

    // Alanlar
    private final Direction direction;
    private double x, y, speed;
    private boolean inIntersection     = false;
    private boolean passedIntersection = false;
    private final Node view;

    // Yapıcı
    public Vehicle(Direction direction, double startX, double startY) {
        this.direction = direction;
        this.x         = startX;
        this.y         = startY;
        this.speed     = 0;

        Rectangle r = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);
        if (direction == Direction.EAST || direction == Direction.WEST) {
            r.setWidth (DEFAULT_LENGTH);
            r.setHeight(DEFAULT_WIDTH);
        }
        r.setFill(randomPastel());
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(0.5);
        this.view = r;
        updateView();
    }
    public Vehicle(String id, Direction d, double x, double y) { this(d, x, y); }

    // Hareket
    public void move(LightPhase phase) {
        final double AFTER_JUNCTION_FACTOR = 1.3;
        final double YELLOW_FACTOR         = 1.0 / 1.5;
        final double DECELERATION_STEP     = DEFAULT_SPEED * 0.1;

        if (passedIntersection) {
            speed = DEFAULT_SPEED * AFTER_JUNCTION_FACTOR;
        } else if (!inIntersection) {
            speed = DEFAULT_SPEED / 3;
        } else {
            switch (phase) {
                case GREEN  -> speed = DEFAULT_SPEED;
                case YELLOW -> speed = DEFAULT_SPEED * YELLOW_FACTOR;
                case RED    -> speed = Math.max(0, speed - DECELERATION_STEP);
            }
        }

        switch (direction) {
            case NORTH -> y -= speed;
            case SOUTH -> y += speed;
            case EAST  -> x += speed;
            case WEST  -> x -= speed;
        }
        updateView();
    }

    // Kavşak bayrakları
    public void markInsideIntersection() { inIntersection = true; }
    public boolean isInIntersection()      { return inIntersection; }

    public void markPassedIntersection()   { passedIntersection = true; }
    public boolean hasPassedIntersection() { return passedIntersection; }

    // Yardımcılar
    private void updateView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
    private static Color randomPastel() {
        double h = ThreadLocalRandom.current().nextDouble(0, 360);
        return Color.hsb(h, 0.4, 0.85);
    }

    // Getter
    public Node getView()       { return view; }
    public Direction getDirection() { return direction; }
}