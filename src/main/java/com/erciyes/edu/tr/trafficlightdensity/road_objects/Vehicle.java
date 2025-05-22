package com.erciyes.edu.tr.trafficlightdensity.road_objects;

//	Tüm araçların ortak özelliklerini tanımlar (soyut).
public abstract class Vehicle {
    protected String id;
    protected double length;
    protected double width;
    protected double speed;
    protected Direction direction;

    public Vehicle(String id, double length, double width, double speed, Direction direction) {
        this.id = id;
        this.length = length;
        this.width = width;
        this.speed = speed;
        this.direction = direction;
    }

    // Soyut metot - her araç tipi kendi hareket davranışını tanımlar
    public abstract void move();

    // Getter metotları
    public String getId() {
        return id;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getSpeed() {
        return speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}