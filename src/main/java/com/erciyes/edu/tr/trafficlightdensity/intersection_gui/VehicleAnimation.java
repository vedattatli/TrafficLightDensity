package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

/**
 * <h2>KavşakAnimasyonu</h2>
 * <p>
 * Bu sınıf; araç kuyruklarını oluşturur, trafik ışığına göre hareket ettirir,
 * kavşağa giren aracı sensör yardımıyla belirler ve ekran dışına çıkanları
 * temizler. Kodun her satırı Türkçe yorumlarla açıklanmıştır; değişken ve metot
 * adları da olabildiğince Türkçeleştirilmiştir.
 * </p>
 */
public final class VehicleAnimation {

    /* ───────────────────────────── Sabitler ──────────────────────────────── */
    private static final double MERKEZ_X   = 545.5;                                   // Kavşak merkezi (X)
    private static final double MERKEZ_Y   = 250.0;                                   // Kavşak merkezi (Y)
    private static final double KUYRUK_OF  = 200.0;                                   // İlk araç ile merkez arası mesafe
    private static final double ŞERİT_OF   = Vehicle.DEFAULT_WIDTH * 0.75;            // Şerit merkeze ofseti
    private static final double ARA_BOŞLUK = Vehicle.DEFAULT_LENGTH + Vehicle.DEFAULT_WIDTH * 2.0; // Araçlar arası boşluk

    private static final double SENSOR_THİCKNESS = 2.0; // px – sensör dikdörtgenlerinin kalınlığı

    /* ───────────────────────── Alan Değişkenleri ────────────────────────── */
    private final Map<Direction, List<Vehicle>> şeritAraçları = new EnumMap<>(Direction.class); // Her yön için araç listesi
    private final SimulationManager simYönetici;        // Trafik ışığı fazlarını almak için
    private final AnimationTimer zamanlayıcı;            // JavaFX animasyon döngüsü

    private Pane tuval;          // Araçların çizildiği ana panel
    private boolean çalışıyor = false; // Animasyon aktif mi?

    // Dört görünmez sensör
    private Rectangle sensörKuzey, sensörGüney, sensörDoğu, sensörBatı;

    /* ─────────────────────────── Yapıcı ─────────────────────────────────── */
    public VehicleAnimation(SimulationManager simYönetici) {
        this.simYönetici = simYönetici;
        for (Direction d : Direction.values()) şeritAraçları.put(d, new ArrayList<>());

        // Her karede tick() çağırılır
        zamanlayıcı = new AnimationTimer() {
            @Override public void handle(long now) {
                if (çalışıyor && simYönetici.isRunning() && !simYönetici.isPaused()) kareİşle();
            }
        };
    }

    /* ───────────────────────── Genel API ────────────────────────────────── */

    /** Simülasyon başlarken kuyrukları oluştur ve sensörleri ekle. */
    public void kuyruklarıOluştur(TrafficController trafikKontrol, Pane anaPanel) {
        temizle();                 // Önce eski her şeyi temizle
        this.tuval = anaPanel;
        if (tuval == null) { System.err.println("Pane NULL – çizim yapılamadı!"); return; }
        sensörleriEkle();          // Görünmez sensörler
        // trafficController içindeki her yön için araç sayısını al ve kuyruk hazırla
        trafikKontrol.getVehicleCounts().forEach(this::şeritKuyrukEkle);
    }

    /** Animasyonu başlat. */
    public void başlat() { if (!çalışıyor) { çalışıyor = true; zamanlayıcı.start(); } }
    /** Animasyonu durdur (pause). */
    public void durdur() { if (çalışıyor)  { çalışıyor = false; zamanlayıcı.stop();  } }

    /** Tüm araç ve sensörleri panelden ve bellekten temizler. */
    public void temizle() {
        durdur();
        if (tuval != null) {
            şeritAraçları.values().stream().flatMap(List::stream).map(Vehicle::getView)
                    .forEach(tuval.getChildren()::remove);
            tuval.getChildren().removeAll(sensörKuzey, sensörGüney, sensörDoğu, sensörBatı);
        }
        şeritAraçları.values().forEach(List::clear);
        sensörKuzey = sensörGüney = sensörDoğu = sensörBatı = null;
    }

    /* Eski isimlerle uyumluluk (UI kodu bozmasın diye) */
    public void initialiseVehicles(TrafficController c, Pane p){kuyruklarıOluştur(c,p);}
    public void initializeVehicles(TrafficController c, Pane p){kuyruklarıOluştur(c,p);}
    public void startAnimation(){başlat();}
    public void stopAnimation(){durdur();}
    public void clearAllVehicles(){temizle();}

    /* ───────────────────── Sensör Ekleme ───────────────────── */
    private void sensörleriEkle() {
        if (sensörKuzey != null) return; // Zaten eklenmiş

        double yolGenişlik = Vehicle.DEFAULT_WIDTH * 3;

        sensörKuzey = sensörOluştur(MERKEZ_X - yolGenişlik/2, MERKEZ_Y - SENSOR_THİCKNESS /2, yolGenişlik, SENSOR_THİCKNESS);
        sensörGüney = sensörOluştur(MERKEZ_X - yolGenişlik/2, MERKEZ_Y + SENSOR_THİCKNESS/2, yolGenişlik, SENSOR_THİCKNESS);

        sensörDoğu  = sensörOluştur(MERKEZ_X - SENSOR_THİCKNESS/2, MERKEZ_Y - yolGenişlik/2, SENSOR_THİCKNESS, yolGenişlik);
        sensörBatı  = sensörOluştur(MERKEZ_X + SENSOR_THİCKNESS/2, MERKEZ_Y - yolGenişlik/2, SENSOR_THİCKNESS, yolGenişlik);

        tuval.getChildren().addAll(sensörKuzey, sensörGüney, sensörDoğu, sensörBatı);
    }

    /** Şeffaf sensör diktörtgeni üretir. */
    private Rectangle sensörOluştur(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setFill(Color.TRANSPARENT);
        r.setLayoutX(x);
        r.setLayoutY(y);
        r.setMouseTransparent(true); // Fare etkileşimi almasın
        return r;
    }

    /* ───────────────────── Kuyruk Hazırlama ───────────────────── */
    private void şeritKuyrukEkle(Direction yön, int adet) {
        List<Vehicle> liste = şeritAraçları.get(yön);
        double başlangıçX = 0, başlangıçY = 0;

        // İlk aracın konumunu belirle
        switch (yön) {
            case NORTH -> { başlangıçX = MERKEZ_X + ŞERİT_OF - Vehicle.DEFAULT_WIDTH/2; başlangıçY = MERKEZ_Y + KUYRUK_OF; }
            case SOUTH -> { başlangıçX = MERKEZ_X - ŞERİT_OF - Vehicle.DEFAULT_WIDTH/2 + Vehicle.DEFAULT_WIDTH; başlangıçY = MERKEZ_Y - KUYRUK_OF - Vehicle.DEFAULT_LENGTH; }
            case EAST  -> { başlangıçX = MERKEZ_X - KUYRUK_OF - Vehicle.DEFAULT_LENGTH; başlangıçY = MERKEZ_Y + ŞERİT_OF - Vehicle.DEFAULT_WIDTH/2; }
            case WEST  -> { başlangıçX = MERKEZ_X + KUYRUK_OF; başlangıçY = MERKEZ_Y - ŞERİT_OF - Vehicle.DEFAULT_WIDTH/2; }
        }

        // Her araç için konumu ofsetle, oluştur, listeye ve panele ekle
        for (int i = 0; i < adet; i++) {
            double x = başlangıçX, y = başlangıçY;
            switch (yön) {
                case NORTH -> y += i * ARA_BOŞLUK;
                case SOUTH -> y -= i * ARA_BOŞLUK;
                case EAST  -> x -= i * ARA_BOŞLUK;
                case WEST  -> x += i * ARA_BOŞLUK;
            }
            Vehicle araç = new Vehicle(yön, x, y);
            liste.add(araç);
            tuval.getChildren().add(araç.getView());
        }
    }

    /* ───────────────────── Ana Animasyon Döngüsü ───────────────────── */
    private void kareİşle(){
        // 1) Araçları ışığa göre hareket ettir
        şeritAraçları.forEach((yön, liste) -> liste.forEach(ar -> ar.move(simYönetici.getLightPhaseForDirection(yön))));

        // 2) Sensör kesişimini test et → kavşağa girdi mi?
        şeritAraçları.values().forEach(liste -> liste.forEach(this::sensörKontrol));

        // 3) Ekran dışına çıkanları panelden ve listeden kaldır
        şeritAraçları.values().forEach(liste -> liste.removeIf(this::ekranDışıMı));
    }

    /** Aracın sensörle kesişip kesişmediğini kontrol eder. */
    private void sensörKontrol(Vehicle araç){
        if(araç.isInIntersection()) return; // Zaten işaretli
        Rectangle sensör = switch(araç.getDirection()){
            case NORTH -> sensörKuzey;
            case SOUTH -> sensörGüney;
            case EAST  -> sensörDoğu;
            case WEST  -> sensörBatı;
        };
        // Bounds kesişiyorsa kavşağa girmiş say
        if(araç.getView().getBoundsInParent().intersects(sensör.getBoundsInParent())){
            araç.markInsideIntersection();
        }
    }

    /** Araç ekrandan çıktıysa true döndür, ayrıca node’u panelden sil. */
    private boolean ekranDışıMı(Vehicle araç){
        if(tuval==null) return false;
        double w=tuval.getWidth(), h=tuval.getHeight();
        double x=araç.getView().getLayoutX(), y=araç.getView().getLayoutY();
        boolean dışarıda=x<-100||x>w+100||y<-100||y>h+100;
        if(dışarıda) tuval.getChildren().remove(araç.getView());
        return dışarıda;
    }
}
