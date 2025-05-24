package com.erciyes.edu.tr.trafficlightdensity.road_objects;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Simülasyondaki tek tip aracı temsil eder.
 * Boyut ve varsayılan hız gibi özellikleri sabittir.
 * ID, yön ve başlangıç konumu gibi bilgiler nesne oluşturulurken atanır.
 */
public class Vehicle {

    // --- Araçların Sabit Özellikleri ---
    public static final double DEFAULT_LENGTH = 30.0;
    public static final double DEFAULT_WIDTH = 15.0;
    public static final double DEFAULT_INITIAL_SPEED = 1.0; // Animasyon tick'i başına piksel
    public static final double SAFE_DISTANCE_BUFFER = 10.0; // Araçlar arası minimum boşluk (biraz artırıldı)

    // --- KAVŞAK GEOMETRİSİ SABİTLERİ (MUTLAKA KENDİ FXML'İNİZE GÖRE AYARLAYIN!) ---
    // Bu değerler, aracın "ön ucunun" koordinatlarıdır.
    // Kavşağın merkezi ve yolların genişliği.
    public static final double INTERSECTION_CENTER_X = 545.5; // Örnek: MainPane genişliği 600 ise merkezi
    public static final double INTERSECTION_CENTER_Y =250.0; // Örnek: MainPane yüksekliği 500 ise merkezi
    public static final double ROAD_HALF_WIDTH = 590.0;      // Tek bir yolun yarım genişliği (örn. toplam yol 50 ise)

    // DURMA ÇİZGİLERİ (Aracın ön ucunun bu çizgide veya biraz gerisinde durması hedeflenir)
    // Bu değerler, kavşağa girmeden hemen önceki çizgilerdir.
    public static final double STOP_LINE_NORTH_Y = INTERSECTION_CENTER_Y - ROAD_HALF_WIDTH;
    public static final double STOP_LINE_SOUTH_Y = INTERSECTION_CENTER_Y + ROAD_HALF_WIDTH;
    public static final double STOP_LINE_WEST_X = INTERSECTION_CENTER_X - ROAD_HALF_WIDTH;
    public static final double STOP_LINE_EAST_X = INTERSECTION_CENTER_X + ROAD_HALF_WIDTH;

    // KAVŞAK ÇIKIŞ SINIRLARI (Aracın ARKA ucu bu çizgiyi geçtiğinde kavşaktan çıkmış sayılır)
    // Bu değerler, aracın kavşağı tamamen terk ettiği sınırlardır.
    public static final double EXIT_LINE_NORTH_Y = INTERSECTION_CENTER_Y - ROAD_HALF_WIDTH - DEFAULT_LENGTH;
    public static final double EXIT_LINE_SOUTH_Y = INTERSECTION_CENTER_Y + ROAD_HALF_WIDTH + DEFAULT_LENGTH;
    public static final double EXIT_LINE_WEST_X = INTERSECTION_CENTER_X - ROAD_HALF_WIDTH - DEFAULT_LENGTH;
    public static final double EXIT_LINE_EAST_X = INTERSECTION_CENTER_X + ROAD_HALF_WIDTH + DEFAULT_LENGTH;
    // --- END OF GEOMETRY CONSTANTS ---

    protected String id;
    protected double currentSpeed;
    protected Direction direction;
    protected Node view;
    protected double currentX; // Aracın sol üst köşesinin X'i
    protected double currentY; // Aracın sol üst köşesinin Y'si

    public enum VehicleState {
        MOVING,
        WAITING,
        IN_INTERSECTION,
        PASSED
    }
    protected VehicleState state;

    public Vehicle(String id, Direction direction, double startX, double startY) {
        this.id = id;
        this.direction = direction;
        this.currentX = startX;
        this.currentY = startY;
        this.currentSpeed = 0; // Başlangıçta duruyor
        this.state = VehicleState.WAITING; // İlk ışık durumuna göre hareket edecek

        Rectangle rectView = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);
        switch (direction) {
            case NORTH: rectView.setFill(Color.LIGHTCORAL.deriveColor(0, 1.0, 0.8, 1.0)); break;
            case SOUTH: rectView.setFill(Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 0.8, 1.0)); break;
            case EAST:  rectView.setWidth(DEFAULT_LENGTH); rectView.setHeight(DEFAULT_WIDTH); rectView.setFill(Color.LIGHTGREEN.deriveColor(0, 1.0, 0.7, 1.0)); break;
            case WEST:  rectView.setWidth(DEFAULT_LENGTH); rectView.setHeight(DEFAULT_WIDTH); rectView.setFill(Color.KHAKI.deriveColor(0, 1.0, 0.9, 1.0)); break;
            default:    rectView.setFill(Color.LIGHTGRAY); break;
        }
        rectView.setStroke(Color.BLACK);
        rectView.setStrokeWidth(0.5);
        this.view = rectView;
        updateViewPosition();
    }

    /**
     * Aracın hareket mantığını uygular.
     * @param currentLightPhase Bu aracın yönüne ait trafik ışığının güncel fazı.
     * @param leadingVehicle Eğer varsa, bu aracın hemen önündeki araç.
     */
    public void move(LightPhase currentLightPhase, Vehicle leadingVehicle) {
        if (state == VehicleState.PASSED) {
            return; // Kavşağı geçmişse bir şey yapma
        }

        boolean proceedSignal = false; // Işığa göre geçiş izni

        // 1. Trafik Işığına Göre Karar Verme
        if (currentLightPhase == LightPhase.GREEN) {
            proceedSignal = true;
            if (state == VehicleState.WAITING) { // Eğer bekliyorsa ve yeşil yandıysa
                state = VehicleState.MOVING;
                currentSpeed = DEFAULT_INITIAL_SPEED;
            }
        } else if (currentLightPhase == LightPhase.YELLOW) {
            // Sarı ışıkta: Kavşağa çok yakınsa (örn. durma çizgisini geçmişse veya kavşak içindeyse) geç,
            // değilse (durma çizgisinden önceyse) dur.
            if (isInIntersection() || !isBeforeStoppingLineAndNotAtIt()) {
                proceedSignal = true;
                if (state == VehicleState.WAITING) { // Bekliyordu ve sarıda geçebilecek durumda
                    state = VehicleState.MOVING; // Ya da IN_INTERSECTION
                    currentSpeed = DEFAULT_INITIAL_SPEED;
                }
            } else { // Sarı ve durma çizgisinden önce
                state = VehicleState.WAITING;
                currentSpeed = 0;
                proceedSignal = false;
            }
        } else { // Kırmızı ışık (LightPhase.RED)
            state = VehicleState.WAITING;
            currentSpeed = 0;
            proceedSignal = false;
        }

        // Durma çizgisinde bekleme (Eğer ışık kırmızıysa veya durulması gereken sarıysa)
        if (state == VehicleState.WAITING && isAtStoppingLine()) {
            currentSpeed = 0; // Hızı kesin olarak sıfırla
        }

        // 2. Öndeki Araçla Çarpışma Önleme
        if (leadingVehicle != null && state != VehicleState.WAITING && state != VehicleState.PASSED) {
            double distanceToLeading = calculateDistanceTo(leadingVehicle);
            // Güvenli mesafeden daha yakınsa ve öndeki araç yavaşsa/duruyorsa
            if (distanceToLeading < SAFE_DISTANCE_BUFFER) {
                // Eğer öndeki araç bizden yavaşsa veya duruyorsa, onun hızına adapte ol veya dur.
                if (leadingVehicle.getCurrentSpeed() < this.currentSpeed || leadingVehicle.getState() == VehicleState.WAITING) {
                    this.currentSpeed = leadingVehicle.getCurrentSpeed();
                    if (this.currentSpeed == 0 && distanceToLeading < SAFE_DISTANCE_BUFFER / 2) { // Çok yakın ve öndeki duruyorsa
                        this.state = VehicleState.WAITING; // Öndeki araçtan dolayı bekle
                    }
                }
            } else if (proceedSignal && this.currentSpeed < DEFAULT_INITIAL_SPEED) {
                // Önde boşluk varsa ve ışık izin veriyorsa tekrar hızlanabiliriz.
                this.currentSpeed = DEFAULT_INITIAL_SPEED;
                if(state == VehicleState.WAITING) state = VehicleState.MOVING; // Beklemeden harekete geç
            }
        } else if (proceedSignal && state != VehicleState.WAITING && state != VehicleState.PASSED) {
            // Önde araç yoksa ve ışık izin veriyorsa normal hızda git (ve beklemiyorsa)
            this.currentSpeed = DEFAULT_INITIAL_SPEED;
            if(state == VehicleState.WAITING) state = VehicleState.MOVING;
        }


        // 3. Hareketi Uygula
        if (currentSpeed > 0 && (state == VehicleState.MOVING || state == VehicleState.IN_INTERSECTION)) {
            // Kavşağa giriş durumunu güncelle
            if (state == VehicleState.MOVING && isInIntersection()) {
                state = VehicleState.IN_INTERSECTION;
            }

            double actualMoveDistance = currentSpeed;

            // Durma çizgisine gelince tam durmasını sağlamak için ince ayar
            if (state != VehicleState.IN_INTERSECTION && !proceedSignal && isApproachingStoppingLine()) {
                actualMoveDistance = Math.min(currentSpeed, getDistanceToStoppingLine());
                if (actualMoveDistance <= 0.1) { // Çok küçük bir hareket kalmışsa durmuş say
                    currentSpeed = 0;
                    state = VehicleState.WAITING;
                    actualMoveDistance = 0;
                    // Pozisyonu tam durma çizgisine sabitle (isteğe bağlı)
                    snapToStoppingLine();
                }
            }


            if(actualMoveDistance > 0) {
                switch (direction) {
                    case NORTH: currentY -= actualMoveDistance; break;
                    case SOUTH: currentY += actualMoveDistance; break;
                    case EAST:  currentX += actualMoveDistance; break;
                    case WEST:  currentX -= actualMoveDistance; break;
                }
                updateViewPosition();
            }
        }

        // 4. Kavşaktan Geçiş Kontrolü
        if (state != VehicleState.PASSED && hasPassedIntersection()) {
            state = VehicleState.PASSED;
            currentSpeed = 0;
        }
    }

    private void updateViewPosition() {
        if (view != null) {
            view.setLayoutX(currentX);
            view.setLayoutY(currentY);
        }
    }

    /** Aracın durma çizgisine ulaşıp ulaşmadığını veya geçip geçmediğini kontrol eder. */
    private boolean isBeforeStoppingLineAndNotAtIt() {
        switch (direction) {
            case NORTH: return currentY > STOP_LINE_NORTH_Y + currentSpeed; // Biraz pay bırak
            case SOUTH: return currentY < STOP_LINE_SOUTH_Y - currentSpeed;
            case WEST:  return currentX > STOP_LINE_WEST_X + currentSpeed;
            case EAST:  return currentX < STOP_LINE_EAST_X - currentSpeed;
        }
        return false;
    }

    /** Aracın tam durma çizgisinde olup olmadığını kontrol eder (ön ucu). */
    private boolean isAtStoppingLine() {
        double tolerance = DEFAULT_INITIAL_SPEED / 2; // Yarım hız kadar tolerans
        switch (direction) {
            case NORTH: return Math.abs(currentY - STOP_LINE_NORTH_Y) < tolerance;
            case SOUTH: return Math.abs(currentY - STOP_LINE_SOUTH_Y) < tolerance;
            case WEST:  return Math.abs(currentX - STOP_LINE_WEST_X) < tolerance;
            case EAST:  return Math.abs(currentX - STOP_LINE_EAST_X) < tolerance;
        }
        return false;
    }

    /** Aracın durma çizgisine yaklaşıp yaklaşmadığını kontrol eder. */
    private boolean isApproachingStoppingLine() {
        return getDistanceToStoppingLine() < DEFAULT_LENGTH; // Bir araç boyu mesafeden daha yakınsa
    }

    /** Aracı tam durma çizgisine sabitler (görsel hizalama için). */
    private void snapToStoppingLine() {
        switch (direction) {
            case NORTH: currentY = STOP_LINE_NORTH_Y; break;
            case SOUTH: currentY = STOP_LINE_SOUTH_Y; break;
            case WEST:  currentX = STOP_LINE_WEST_X; break;
            case EAST:  currentX = STOP_LINE_EAST_X; break;
        }
        updateViewPosition();
    }


    /** Aracın ön ucunun durma çizgisine olan mesafesini döndürür. */
    private double getDistanceToStoppingLine() {
        switch (direction) {
            case NORTH: return currentY - STOP_LINE_NORTH_Y;
            case SOUTH: return STOP_LINE_SOUTH_Y - currentY;
            case WEST:  return currentX - STOP_LINE_WEST_X;
            case EAST:  return STOP_LINE_EAST_X - currentX;
        }
        return Double.MAX_VALUE;
    }


    /** Aracın herhangi bir bölümünün kavşak alanı içinde olup olmadığını kontrol eder. */
    private boolean isInIntersection() {
        // Durma çizgisini geçmiş ve çıkış çizgisine gelmemiş olmalı.
        // Not: Yönlere göre aracın kapladığı alanın tamamı dikkate alınmalı.
        // Basitleştirilmiş: Aracın ön ucu durma çizgisini geçtiyse ve çıkışa gelmediyse.
        switch (direction) {
            case NORTH: return currentY < STOP_LINE_NORTH_Y && (currentY + DEFAULT_LENGTH) > EXIT_LINE_NORTH_Y;
            case SOUTH: return currentY > STOP_LINE_SOUTH_Y && currentY < EXIT_LINE_SOUTH_Y;
            case WEST:  return currentX < STOP_LINE_WEST_X && (currentX + DEFAULT_WIDTH) > EXIT_LINE_WEST_X;
            case EAST:  return currentX > STOP_LINE_EAST_X && currentX < EXIT_LINE_EAST_X;
        }
        return false;
    }

    /** Aracın TAMAMININ (arka ucunun) kavşağı terk edip etmediğini kontrol eder. */
    private boolean hasPassedIntersection() {
        switch (direction) {
            case NORTH: return currentY < EXIT_LINE_NORTH_Y; // Aracın önü, kuzey çıkış çizgisinden (daha küçük Y) geçtiyse
            case SOUTH: return (currentY - DEFAULT_LENGTH) > EXIT_LINE_SOUTH_Y; // Aracın arkası (Y - Uzunluk), güney çıkış çizgisinden (daha büyük Y) geçtiyse -> HATA: Y koordinatı sol üst köşe ise, currentY > EXIT_LINE_SOUTH_Y olmalı
            case WEST:  return currentX < EXIT_LINE_WEST_X; // Aracın önü, batı çıkış çizgisinden (daha küçük X) geçtiyse
            case EAST:  return (currentX - DEFAULT_WIDTH) > EXIT_LINE_EAST_X; // Aracın arkası (X - Genişlik), doğu çıkış çizgisinden (daha büyük X) geçtiyse -> HATA

        }
        return false;
    }

    /** Bu araç ile öndeki araç arasındaki net mesafeyi hesaplar. */
    private double calculateDistanceTo(Vehicle leadingVehicle) {
        if (leadingVehicle == null || this.direction != leadingVehicle.getDirection() || leadingVehicle.getState() == VehicleState.PASSED) {
            return Double.MAX_VALUE;
        }
        // Öndeki aracın ARKA ucu ile bu aracın ÖN ucu arasındaki mesafe.
        switch (direction) {
            case NORTH: // Öndeki (Y'si daha küçük) - Benim (Y'si daha büyük)
                return (this.currentY - DEFAULT_LENGTH) - leadingVehicle.currentY; // Benim önüm - Öndekinin arkası
            case SOUTH: // Benim (Y'si daha küçük) - Öndeki (Y'si daha büyük)
                return leadingVehicle.currentY - (this.currentY + DEFAULT_LENGTH); // Öndekinin önü - Benim arkam
            case WEST:  // Benim (X'i daha büyük) - Öndeki (X'i daha küçük)
                return (this.currentX - DEFAULT_WIDTH) - leadingVehicle.currentX; // Benim önüm - Öndekinin arkası (Batıya giderken X azalır)
            case EAST:  // Öndeki (X'i daha büyük) - Benim (X'i daha küçük)
                return leadingVehicle.currentX - (this.currentX + DEFAULT_WIDTH); // Öndekinin önü - Benim arkam (Doğuya giderken X artar)
        }
        return Double.MAX_VALUE;
    }

    // --- Getter ve Setter Metotları ---
    public String getId() { return id; }
    public double getLength() { return DEFAULT_LENGTH; }
    public double getWidth() { return DEFAULT_WIDTH; }
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double currentSpeed) { this.currentSpeed = currentSpeed; }
    public Direction getDirection() { return direction; }
    public Node getView() { return view; }
    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public VehicleState getState() { return state; }
    public void setState(VehicleState state) { this.state = state; }
}