package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * <h2>Araç (Vehicle) Sınıfı</h2>
 * Basit, tek tip bir aracı temsil eder.
 * <ul>
 *     <li>Şeridindeki trafik ışığı <b>YEŞİL</b> ise {@link #DEFAULT_SPEED} ile ilerler.</li>
 *     <li>Işık <b>KIRMIZI</b> veya <b>SARI</b> ise durur.</li>
 * </ul>
 * Kavşak geometrisi veya çarpışma kontrolü içermez. Oluşturulan her araca rastgele pastel bir renk atanır.
 */
public class Vehicle {

    // ─────────────────────────── Sabitler ───────────────────────────
    public static final double DEFAULT_LENGTH = 30.0; // px – boy
    public static final double DEFAULT_WIDTH  = 15.0; // px – en
    public static final double DEFAULT_SPEED  = 2.0;  // px/kare – sabit hız

    // ───────────────────────── Alan Değişkenleri ────────────────────
    private final Direction direction; // Gidiş yönü
    private double x;                  // Sol‑üst X koordinatı
    private double y;                  // Sol‑üst Y koordinatı
    private double speed;              // Anlık hız (px/kare)

    private final Node view;           // JavaFX’te çizilen şekil

    // ─────────────────────────── Yapıcılar ──────────────────────────

    /**
     * Yeni araç oluşturur.
     *
     * @param direction Gidiş yönü (NORTH, SOUTH, EAST, WEST)
     * @param startX    Başlangıç X koordinatı
     * @param startY    Başlangıç Y koordinatı
     */
    public Vehicle(Direction direction, double startX, double startY) {
        this.direction = direction;
        this.x = startX;
        this.y = startY;
        this.speed = 0; // Başlangıçta durur

        // Araç görünümü: dikdörtgen
        Rectangle rect = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);
        if (direction == Direction.EAST || direction == Direction.WEST) {
            // Doğu/Batı için uzun kenarı yatay yap
            rect.setWidth(DEFAULT_LENGTH);
            rect.setHeight(DEFAULT_WIDTH);
        }
        rect.setFill(randomPastel());
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(0.5);

        this.view = rect;
        updateViewPosition();
    }

    /**
     * <p>Geriye dönük uyumluluk amacıyla, eski API’de bulunan yapıcıyı
     * destekler. <code>id</code> parametresi <em>kullanılmıyor</em>.</p>
     */
    public Vehicle(String id, Direction direction, double startX, double startY) {
        this(direction, startX, startY); // id yok sayılır
    }

    // ───────────────────────── Genel API ────────────────────────────

    /**
     * Bir animasyon karesi boyunca aracı günceller.
     * @param phase Bu aracın şeridine ait trafik ışığı fazı
     */
    public void move(LightPhase phase) {
        // Işığa göre hız belirle
        speed = (phase == LightPhase.GREEN) ? DEFAULT_SPEED : 0;

        // Konumu güncelle
        switch (direction) {
            case NORTH -> y -= speed;
            case SOUTH -> y += speed;
            case EAST  -> x += speed;
            case WEST  -> x -= speed;
        }
        updateViewPosition();
    }

    // ─────────────────────── Yardımcı Metotlar ──────────────────────
    private void updateViewPosition() {
        view.setLayoutX(x);
        view.setLayoutY(y);
    }

    private static Color randomPastel() {
        double hue = ThreadLocalRandom.current().nextDouble(0, 360);
        return Color.hsb(hue, 0.4, 0.85); // Düşük satürasyon, yüksek parlaklık
    }

    // ───────────────────────── Getter’lar ───────────────────────────
    public Node getView() { return view; }
}
