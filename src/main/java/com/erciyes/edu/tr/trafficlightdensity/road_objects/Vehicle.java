package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Basit araç sınıfı.
 * <ul>
 *   <li>Sensöre (stop çizgisi) kadar ışığa bakmadan sabit hızla ilerler.</li>
 *   <li>Sensördeyken ışığa göre durur / yavaşlar / geçer.</li>
 *   <li>Kavşağı tamamen geçince tekrar hızlanır.</li>
 * </ul>
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

    private boolean inIntersection     = false; // Sensörü geçti mi?
    private boolean passedIntersection = false; // Kavşaktan tam çıktı mı?

    private final Node view;           // JavaFX görseli

    // ───────── Yapıcılar ─────────
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
    // Eski API ile uyumluluk
    public Vehicle(String id, Direction d, double x, double y) {
        this(d, x, y);
    }

    // ───────── Genel API ─────────
    public void move(LightPhase phase) {
        final double AFTER_JUNCTION_FACTOR = 1.3;           // Kavşak sonrası +%30
        final double YELLOW_FACTOR         = 1.0 / 1.5;     // Sarı’da %33 yavaşla
        final double DECELERATION_STEP     = DEFAULT_SPEED * 0.1; // Kademeli fren

        /* 1) Kavşağı geçtikten sonra hızlan */
        if (passedIntersection) {
            speed = DEFAULT_SPEED * AFTER_JUNCTION_FACTOR;

            /* 2) Sensöre kadar – ışığa bakma */
        } else if (!inIntersection) {
            speed = DEFAULT_SPEED;

            /* 3) Sensördeyken – ışığa göre hareket */
        } else {
            switch (phase) {
                case GREEN  -> speed = DEFAULT_SPEED;
                case YELLOW -> speed = DEFAULT_SPEED * YELLOW_FACTOR;
                case RED    -> speed = Math.max(0, speed - DECELERATION_STEP);
            }
        }

        /* Konum güncelle */
        switch (direction) {
            case NORTH -> y -= speed;
            case SOUTH -> y += speed;
            case EAST  -> x += speed;
            case WEST  -> x -= speed;
        }
        updateView();
    }

    // ───────── Kavşak Bayrakları ─────────
    /** Sensör çizgisine ilk temasında çağrılır. */
    public void markInsideIntersection() {
        inIntersection = true;
    }
    public boolean isInIntersection() {
        return inIntersection;
    }

    /** Kavşağın çıkışını belirleyeceğin yerde çağır. */
    public void markPassedIntersection() {
        passedIntersection = true;
    }
    public boolean hasPassedIntersection() {
        return passedIntersection;
    }

    // ───────── Yardımcılar ─────────
    private void updateView() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }
    private static Color randomPastel() {
        double h = ThreadLocalRandom.current().nextDouble(0, 360);
        return Color.hsb(h, 0.4, 0.85);
    }

    // ───────── Getter ─────────
    public Node getView() {
        return view;
    }

    public double getX() { return x; }   // opsiyonel: konum kontrolü için
    public double getY() { return y; }

    public Direction getDirection() {
        return direction;
    }
}
