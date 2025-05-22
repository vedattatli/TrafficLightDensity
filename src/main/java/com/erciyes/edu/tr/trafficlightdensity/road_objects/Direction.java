package com.erciyes.edu.tr.trafficlightdensity.road_objects;
//Dört yönü (NORTH, SOUTH, EAST, WEST) temsil eden enum.
import javafx.geometry.Point2D;

/**
 * Yön bilgisini temsil eden enum.
 * Trafik simülasyonunda araçların ve ışıkların yönünü belirtir.
 */
public enum Direction {
    NORTH("Kuzey", new Point2D(0, -1), 0),
    SOUTH("Güney", new Point2D(0, 1), 180),
    EAST("Doğu", new Point2D(1, 0), 90),
    WEST("Batı", new Point2D(-1, 0), 270);

    private final String turkishName;
    private final Point2D vector;
    private final double rotationAngle;

    /**
     * Direction constructor
     * @param turkishName Yönün Türkçe adı
     * @param vector Hareket vektörü (normalize edilmiş)
     * @param rotationAngle JavaFX'te döndürme açısı (derece)
     */
    Direction(String turkishName, Point2D vector, double rotationAngle) {
        this.turkishName = turkishName;
        this.vector = vector;
        this.rotationAngle = rotationAngle;
    }

    /**
     * Bu yönün zıt yönünü döndürür
     * @return Zıt yön
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * Yönün Türkçe adını döndürür
     * @return Türkçe yön adı
     */
    public String getTurkishName() {
        return turkishName;
    }

    /**
     * Hareket vektörünü döndürür
     * @return Point2D nesnesi olarak vektör
     */
    public Point2D getVector() {
        return vector;
    }

    /**
     * JavaFX'te kullanılacak döndürme açısını döndürür
     * @return Döndürme açısı (derece)
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Verilen koordinata bu yönde hareket edildiğinde ulaşılacak yeni koordinatı hesaplar
     * @param currentX Mevcut x koordinatı
     * @param currentY Mevcut y koordinatı
     * @param distance Hareket mesafesi
     * @return Yeni koordinat (Point2D)
     */
    public Point2D calculateMove(double currentX, double currentY, double distance) {
        return new Point2D(
                currentX + (vector.getX() * distance),
                currentY + (vector.getY() * distance)
        );
    }

    /**
     * Rastgele bir yön seçer
     * @return Rastgele Direction sabiti
     */
    public static Direction random() {
        Direction[] directions = values();
        return directions[(int) (Math.random() * directions.length)];
    }
}