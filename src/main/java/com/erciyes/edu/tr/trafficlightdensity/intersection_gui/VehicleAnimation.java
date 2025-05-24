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
 * <p><strong>Araç animasyonu</strong>ndan sorumlu sınıf.</p>
 * <ul>
 *     <li>Her yöndeki kuyrukları oluşturur.</li>
 *     <li>Trafik ışığı YEŞİL ise aracı sabit hızla ileri taşır.</li>
 *     <li>KIRMIZI veya SARI ise aracı durdurur.</li>
 * </ul>
 * <p>Kod, <em>basit ve anlaşılır</em> olması için tamamen sade tutulmuştur.</p>
 * <p>Eski UI koduyla uyumlu kalmak amacıyla; <code>initializeVehicles</code>,
 * <code>startAnimation</code>, <code>stopAnimation</code> ve
 * <code>clearAllVehicles</code> isimli sarmalayıcı (wrapper) metotlar hâlâ
 * mevcuttur. Yeni tercih edilen isimler sırasıyla
 * <code>initialiseVehicles</code>, <code>start</code>, <code>stop</code> ve
 * <code>clear</code>.</p>
 */
public final class VehicleAnimation {

    // ───────────────────────────────── Sabitler ─────────────────────────────────
    // Değerleri FXML sahnenize göre güncelleyin.
    private static final double INTERSECTION_CENTER_X = 545.5;  // Kavşak merkez X
    private static final double INTERSECTION_CENTER_Y = 250.0;  // Kavşak merkez Y
    private static final double VEHICLE_QUEUE_OFFSET  = 200.0;  // İlk aracın başlangıç uzaklığı
    private static final double LANE_OFFSET           = Vehicle.DEFAULT_WIDTH * 0.75;
    private static final double EXTRA_SPACING         = Vehicle.DEFAULT_LENGTH + (Vehicle.DEFAULT_WIDTH * 2.0);

    // ───────────────────────────  Alan Değişkenleri ────────────────────────────
    private final Map<Direction, List<Vehicle>> lanes = new EnumMap<>(Direction.class); // Her yön için araç listesi
    private final SimulationManager simManager;   // Trafik ışığı fazlarını sorgulamak için
    private final AnimationTimer timer;           // JavaFX animasyon zamanlayıcısı

    private Pane canvas;      // Araç düğümlerinin çizileceği Panel
    private boolean running = false; // Animasyon çalışıyor mu?

    // ─────────────────────────────── Kurucu ────────────────────────────────────
    public VehicleAnimation(SimulationManager simManager) {
        this.simManager = simManager;
        for (Direction d : Direction.values()) lanes.put(d, new ArrayList<>());

        // Her karede çağrılan animasyon geri çağrısı
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (running && simManager.isRunning() && !simManager.isPaused()) {
                    tick(); // Bir kare çiz
                }
            }
        };
    }

    // ────────────────────────────── Genel API ──────────────────────────────────

    /**
     * Kuyrukları oluşturur ve araçları <code>mainPane</code>e ekler.
     */
    public void initialiseVehicles(TrafficController controller, Pane mainPane) {
        clear();
        this.canvas = mainPane;
        if (canvas == null) {
            System.err.println("VehicleAnimation ▶ mainPane NULL, araçlar çizilemedi!");
            return;
        }
        controller.getVehicleCounts().forEach(this::createQueue);
        System.out.println("Araçlar oluşturuldu ve sahneye eklendi.");
    }

    /** Animasyonu başlatır. */
    public void start() { if (!running) { running = true; timer.start(); } }
    /** Animasyonu duraklatır. */
    public void stop()  { if (running)  { running = false; timer.stop();  } }

    /**
     * Tüm araç düğümlerini sahneden kaldırır ve listeleri temizler.
     */
    public void clear() {
        stop();
        if (canvas != null) {
            lanes.values().stream().flatMap(List::stream).map(Vehicle::getView)
                    .forEach(canvas.getChildren()::remove);
        }
        lanes.values().forEach(List::clear);
    }

    // ─────────── Eski isimlerle uyumlu sarmalayıcı metotlar ───────────────
    public void initializeVehicles(TrafficController c, Pane p) { initialiseVehicles(c, p); }
    public void startAnimation()                                { start(); }
    public void stopAnimation()                                 { stop();  }
    public void clearAllVehicles()                              { clear(); }

    // ───────────────────────────── Yardımcı Metotlar ───────────────────────────

    /**
     * Belirli bir yönde <code>count</code> kadar aracı sıraya dizer.
     */
    private void createQueue(Direction dir, int count) {
        List<Vehicle> list = lanes.get(dir);
        double baseX = 0, baseY = 0; // İlk aracın konumu

        switch (dir) {
            case NORTH -> {
                baseX = INTERSECTION_CENTER_X + LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2;
                baseY = INTERSECTION_CENTER_Y + VEHICLE_QUEUE_OFFSET;
            }
            case SOUTH -> {
                baseX = INTERSECTION_CENTER_X - LANE_OFFSET - Vehicle.DEFAULT_WIDTH / 2 + Vehicle.DEFAULT_WIDTH;
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
            // Benzersiz ve anlaşılır bir kimlik veriyoruz
            String id = dir.name().charAt(0) + "_Car" + (i + 1);
            Vehicle v = new Vehicle(dir, x, y);
            list.add(v);
            canvas.getChildren().add(v.getView());
        }
    }

    /**
     * Bir karelik animasyon güncellemesi: ışık fazını al, aracı ilerlet.
     */
    private void tick() {
        for (Map.Entry<Direction, List<Vehicle>> entry : lanes.entrySet()) {
            LightPhase phase = simManager.getLightPhaseForDirection(entry.getKey());
            for (Vehicle v : entry.getValue()) v.move(phase);
        }
        // Ekran dışına çıkan araçları silerek performans koru
        lanes.values().forEach(list -> list.removeIf(this::pruneIfOffScreen));
    }

    /**
     * Araç ekran dışına çıktıysa düğümü sahneden kaldırır ve <code>true</code>
     * döner; aksi halde <code>false</code>.
     */
    private boolean pruneIfOffScreen(Vehicle v) {
        if (canvas == null) return false;
        double w = canvas.getWidth(), h = canvas.getHeight();
        double x = v.getView().getLayoutX(), y = v.getView().getLayoutY();
        boolean disarda = x < -100 || x > w + 100 || y < -100 || y > h + 100;
        if (disarda) canvas.getChildren().remove(v.getView());
        return disarda;
    }
}
