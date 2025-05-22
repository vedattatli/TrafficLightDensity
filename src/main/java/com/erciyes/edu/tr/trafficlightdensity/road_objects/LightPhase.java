package com.erciyes.edu.tr.trafficlightdensity.road_objects;
import javafx.scene.paint.Color;

import java.time.Duration;

//Işığın durumlarını (RED, YELLOW, GREEN) tutan enum.
public enum LightPhase {
    /**
     * Trafik ışığının farklı durumlarını temsil eder.
     * JavaFX renk bilgilerini içerir ve durum geçişlerini yönetir.
     */

    GREEN("Yeşil", Color.LIMEGREEN, "İlerleyebilir"),
    YELLOW("Sarı", Color.GOLD, "Hazırlan"),
    RED("Kırmızı", Color.TOMATO, "Dur");

    private final String turkishName;
    private final Color javafxColor;
    private final String actionText;

    /**
     * LightPhase constructor
     * @param turkishName Türkçe durum adı
     * @param javafxColor JavaFX Color nesnesi
     * @param actionText Yapılacak aksiyon metni
     */
    LightPhase(String turkishName, Color javafxColor, String actionText) {
        this.turkishName = turkishName;
        this.javafxColor = javafxColor;
        this.actionText = actionText;
    }

    /**
     * Bir sonraki trafik ışığı durumunu döndürür
     * @return Sonraki LightPhase durumu
     */
    public LightPhase next() {
        return switch (this) {
            case GREEN -> YELLOW;
            case YELLOW -> RED;
            case RED -> GREEN;
        };
    }

    /**
     * JavaFX uyumlu renk nesnesi döndürür
     * @return JavaFX Color nesnesi
     */
    public Color getJavafxColor() {
        return javafxColor;
    }

    /**
     * Durumun Türkçe adını döndürür
     * @return Türkçe durum adı
     */
    public String getTurkishName() {
        return turkishName;
    }

    /**
     * Yapılması gereken aksiyon metnini döndürür
     * @return Aksiyon metni
     */
    public String getActionText() {
        return actionText;
    }

    /**
     * Durumun yeşil olup olmadığını kontrol eder
     * @return Yeşil ise true, değilse false
     */
    public boolean isGreen() {
        return this == GREEN;
    }

    /**
     * Durumun kırmızı olup olmadığını kontrol eder
     * @return Kırmızı ise true, değilse false
     */
    public boolean isRed() {
        return this == RED;
    }

    /**
     * Durumun sarı olup olmadığını kontrol eder
     * @return Sarı ise true, değilse false
     */
    public boolean isYellow() {
        return this == YELLOW;
    }

    public Duration getDuration() {
        return getDuration();
    }

    public static Duration getDefaultPhaseDuration(LightPhase phase) {
        return switch (phase) {
            case YELLOW -> Duration.ofSeconds(3); // sabit
            case RED, GREEN -> Duration.ofSeconds(0); // dışarıdan atanacak
        };
    }
}
