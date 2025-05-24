package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Basit araç sınıfı – YEŞİL'de gider, KIRMIZI/SARI'da durur.
 * Kavşağa girdikten sonra ışığa bakmaz.
 */
public class Vehicle {

    // ─────────── Sabitler ───────────
    public static final double DEFAULT_LENGTH = 30.0; // px – boy
    public static final double DEFAULT_WIDTH  = 15.0; // px – en
    public static final double DEFAULT_SPEED  = 2.0;  // px/kare – sabit hız

    // ───────── Alanlar ─────────
    private final Direction direction;   // Gidiş yönü
    private double x;                    // Sol-üst X
    private double y;                    // Sol-üst Y
    private double speed;                // Anlık hız

    private boolean inIntersection = false; // Sensörü geçti mi?

    private final Node view;           // JavaFX görseli

    // ───────── Yapıcılar ─────────

    public Vehicle(Direction direction, double startX, double startY) {
        this.direction = direction;
        this.x = startX;
        this.y = startY;
        this.speed = 0;

        Rectangle r = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);
        if (direction == Direction.EAST || direction == Direction.WEST) {
            r.setWidth(DEFAULT_LENGTH);
            r.setHeight(DEFAULT_WIDTH);
        }
        r.setFill(randomPastel());
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(0.5);
        this.view = r;
        updateView();
    }
    // Eski API ile uyum

    public Vehicle(String id, Direction d, double x, double y) { this(d, x, y); }
    // ───────── Genel API ─────────

    public void move(LightPhase phase) {
        // Eğer kavşaktaysak ışığa bakma – daima git
        if (inIntersection) {
            speed = DEFAULT_SPEED;
        } else {
            speed = (phase == LightPhase.GREEN) ? DEFAULT_SPEED : 0;
        }

        switch (direction) {
            case NORTH -> y -= speed;
            case SOUTH -> y += speed;
            case EAST  -> x += speed;
            case WEST  -> x -= speed;
        }
        updateView();
    }
    // Kavşağa girdiğini işaretle

    public void markInsideIntersection() { inIntersection = true; }
    public boolean isInIntersection() { return inIntersection; }
    // ───────── Yardımcılar ─────────

    private void updateView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
    private static Color randomPastel() {
        double h = ThreadLocalRandom.current().nextDouble(0, 360);
        return Color.hsb(h, 0.4, 0.85);
    }

    // Getter

    public Node getView() { return view; }
    public Direction getDirection() {
        return direction;
    }
}
