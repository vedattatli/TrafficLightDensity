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
    public static final double DEFAULT_LENGTH = 30.0; // Araç uzunluğu (piksel) - Biraz küçülttüm
    public static final double DEFAULT_WIDTH = 15.0;  // Araç genişliği (piksel) - Biraz küçülttüm
    public static final double DEFAULT_INITIAL_SPEED = 1.0; // Araç hızı (animasyon tick'i başına piksel)
    private static final double SAFE_DISTANCE_BUFFER = 5.0; // Araçlar arası minimum boşluk

    // --- Kavşak Geometrisi ve Durma Çizgileri (ÖRNEK DEĞERLER - KENDİ TASARIMINIZA GÖRE AYARLAYIN!) ---
    // Bu değerler kavşağınızın merkezinin (0,0) olduğunu ve yolların bu merkeze göre konumlandığını varsayabilir
    // VEYA ekranın sol üst köşesinin (0,0) olduğunu varsayabilir.
    // Aşağıdaki değerler, ekranın sol üstünün (0,0) olduğu ve kavşağın ortada bir yerde olduğu varsayımına daha yakın.
    // Bu değerleri VehicleAnimation veya UserInterfaceController'dan alıp Vehicle'a iletmek daha esnek olur.
    // Şimdilik basitlik için buraya sabitler ekliyorum ama bu ideal değil.
    // KAVŞAK MERKEZİ (Yaklaşık, FXML'e göre ayarlanmalı)
    public static final double INTERSECTION_CENTER_X = 300; // Örnek
    public static final double INTERSECTION_CENTER_Y = 250; // Örnek
    public static final double INTERSECTION_ROAD_WIDTH = 50; // Tek bir yolun genişliği (örn: 2 şerit + ayırıcı)

    // DURMA ÇİZGİLERİ (Kavşağa girmeden önceki çizgi, aracın ÖNÜNÜN bu çizgiyi geçmemesi lazım)
    // Bu değerler, aracın ön ucunun duracağı Y veya X koordinatlarıdır.
    public static final double STOP_LINE_NORTH_Y = INTERSECTION_CENTER_Y - INTERSECTION_ROAD_WIDTH / 2; // Kuzeyden gelen araç için Y
    public static final double STOP_LINE_SOUTH_Y = INTERSECTION_CENTER_Y + INTERSECTION_ROAD_WIDTH / 2; // Güneyden gelen araç için Y
    public static final double STOP_LINE_WEST_X = INTERSECTION_CENTER_X - INTERSECTION_ROAD_WIDTH / 2;  // Batıdan gelen araç için X
    public static final double STOP_LINE_EAST_X = INTERSECTION_CENTER_X + INTERSECTION_ROAD_WIDTH / 2;  // Doğudan gelen araç için X

    // KAVŞAK ÇIKIŞ SINIRLARI (Aracın TAMAMI bu çizgiyi geçtiğinde kavşaktan çıkmış sayılır)
    // Bu değerler, aracın ARKA ucunun geçmesi gereken Y veya X koordinatlarıdır.
    public static final double EXIT_LINE_NORTH_Y = INTERSECTION_CENTER_Y - INTERSECTION_ROAD_WIDTH / 2 - DEFAULT_LENGTH; // Kuzeye giden araç için çıkış Y'si (kavşağın üstü)
    public static final double EXIT_LINE_SOUTH_Y = INTERSECTION_CENTER_Y + INTERSECTION_ROAD_WIDTH / 2 + DEFAULT_LENGTH; // Güneye giden araç için çıkış Y'si (kavşağın altı)
    public static final double EXIT_LINE_WEST_X = INTERSECTION_CENTER_X - INTERSECTION_ROAD_WIDTH / 2 - DEFAULT_LENGTH;  // Batıya giden araç için çıkış X'i (kavşağın solu)
    public static final double EXIT_LINE_EAST_X = INTERSECTION_CENTER_X + INTERSECTION_ROAD_WIDTH / 2 + DEFAULT_LENGTH;  // Doğuya giden araç için çıkış X'i (kavşağın sağı)


    // --- Araç Başına Değişen Özellikler ---
    protected String id;
    protected double currentSpeed;
    protected Direction direction;
    protected Node view;
    protected double currentX;
    protected double currentY;

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
        this.currentSpeed = 0; // Başlangıçta hızı 0 yapalım, yeşil yanınca hareketlensin.
        this.state = VehicleState.WAITING; // Başlangıçta bekliyor olsun (ilk ışık durumuna göre değişir)

        Rectangle rectView = new Rectangle(DEFAULT_WIDTH, DEFAULT_LENGTH);
        switch (direction) {
            case NORTH: rectView.setFill(Color.LIGHTCORAL.deriveColor(0, 1.0, 0.8, 1.0)); break;
            case SOUTH: rectView.setFill(Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 0.8, 1.0)); break;
            case EAST:  rectView.setFill(Color.LIGHTGREEN.deriveColor(0, 1.0, 0.7, 1.0)); break;
            case WEST:  rectView.setFill(Color.KHAKI.deriveColor(0, 1.0, 0.9, 1.0)); break;
            default:    rectView.setFill(Color.LIGHTGRAY); break;
        }
        rectView.setStroke(Color.BLACK);
        rectView.setStrokeWidth(0.5);
        this.view = rectView;
        updateViewPosition();
    }

    public void move(TrafficLight trafficLight, Vehicle leadingVehicle) {
        if (state == VehicleState.PASSED) {
            return;
        }

        // 1. Trafik Işığı ve Durma Çizgisi Kontrolü
        boolean canProceedBasedOnLight = false;
        if (trafficLight.isGreen()) {
            canProceedBasedOnLight = true;
            if (state == VehicleState.WAITING) { // Eğer bekliyorsa ve yeşil yandıysa harekete geç
                this.state = VehicleState.MOVING;
                this.currentSpeed = DEFAULT_INITIAL_SPEED;
            }
        } else if (trafficLight.isYellow()) {
            // Sarı ışıkta: Kavşağa çok yakınsa geç, değilse durmaya çalış.
            // Basit senaryo: Sarı ışıkta da geçmeye devam etsin (kavşak içindeyse) veya dursun.
            if (isInIntersection() || !isBeforeStoppingLine()) { // Zaten kavşaktaysa veya durma çizgisini geçtiyse
                canProceedBasedOnLight = true;
                if (state == VehicleState.WAITING) { // Sarı yandığında hala bekliyorsa ve geçebilecekse
                    this.state = VehicleState.MOVING;
                    this.currentSpeed = DEFAULT_INITIAL_SPEED;
                }
            } else { // Durma çizgisinden önce ve sarı yandıysa
                this.state = VehicleState.WAITING;
                this.currentSpeed = 0;
                canProceedBasedOnLight = false;
            }
        } else { // Kırmızı ışık
            if (isBeforeStoppingLine() || isInIntersection()) {
                // Kırmızıda ve hala durma çizgisine gelmediyse veya kavşaktaysa (kavşağı boşaltmalı)
                // Bu durum karmaşık. Şimdilik kırmızıda durma çizgisine gelince dursun.
            }
            if (isBeforeStoppingLine() && state != VehicleState.WAITING) {
                // Tam durma çizgisine gelmeden önce hızı azaltıp WAITING'e geçebilir.
                // Şimdilik, tam çizgideyse WAITING olacak.
            }


            this.state = VehicleState.WAITING;
            this.currentSpeed = 0;
            canProceedBasedOnLight = false;
        }

        // Durma çizgisi kontrolü (SADECE KIRMIZI VEYA DURMASI GEREKEN SARI İÇİN)
        if ((trafficLight.isRed() || (trafficLight.isYellow() && isBeforeStoppingLine() && !isInIntersection() )) && isAtStoppingLine()) {
            this.state = VehicleState.WAITING;
            this.currentSpeed = 0;
        }


        // 2. Öndeki Araçla Çarpışma Önleme
        if (leadingVehicle != null && state != VehicleState.WAITING) {
            double distanceToLeading = calculateDistanceTo(leadingVehicle);
            if (distanceToLeading < SAFE_DISTANCE_BUFFER) {
                this.currentSpeed = Math.min(this.currentSpeed, leadingVehicle.getCurrentSpeed()); // Öndekinin hızına düş veya daha yavaşla
                if (leadingVehicle.getState() == VehicleState.WAITING && distanceToLeading < (SAFE_DISTANCE_BUFFER / 2) ) { // Çok yakınsa ve öndeki duruyorsa dur
                    this.currentSpeed = 0;
                    this.state = VehicleState.WAITING; // Öndeki araçtan dolayı bekle
                }
            } else if (this.currentSpeed < DEFAULT_INITIAL_SPEED && state != VehicleState.WAITING) {
                // Önde boşluk varsa ve beklemiyorsak tekrar hızlanabiliriz (eğer ışık da izin veriyorsa)
                if(canProceedBasedOnLight) this.currentSpeed = DEFAULT_INITIAL_SPEED;
            }
        } else if (state != VehicleState.WAITING && canProceedBasedOnLight) {
            // Önde araç yoksa ve ışık izin veriyorsa normal hızda git
            this.currentSpeed = DEFAULT_INITIAL_SPEED;
        }


        // 3. Hareketi Uygula (Eğer hızı sıfırdan büyükse ve geçiş izni varsa)
        if (this.currentSpeed > 0 && (state == VehicleState.MOVING || state == VehicleState.IN_INTERSECTION)) {
            // Eğer MOVING durumundaysa ve kavşağa girdiyse durumunu IN_INTERSECTION yap
            if (state == VehicleState.MOVING && isInIntersection()) {
                state = VehicleState.IN_INTERSECTION;
            }

            double distanceToMove = this.currentSpeed;
            switch (this.direction) {
                case NORTH: this.currentY -= distanceToMove; break;
                case SOUTH: this.currentY += distanceToMove; break;
                case EAST:  this.currentX += distanceToMove; break;
                case WEST:  this.currentX -= distanceToMove; break;
            }
            updateViewPosition();
        }

        // 4. Kavşaktan Geçiş Kontrolü
        if (hasPassedIntersection()) {
            this.state = VehicleState.PASSED;
            this.currentSpeed = 0; // Artık hareket etmiyor
            // Bu araç simülasyondan ve görselden VehicleAnimation sınıfı tarafından kaldırılmalı
        }
    }

    private void updateViewPosition() {
        if (view != null) {
            view.setLayoutX(this.currentX);
            view.setLayoutY(this.currentY);
        }
    }

    private boolean isBeforeStoppingLine() {
        switch (this.direction) {
            case NORTH: return this.currentY > STOP_LINE_NORTH_Y; // Y konumu durma çizgisinin Y'sinden büyük mü (aşağıda mı?)
            case SOUTH: return this.currentY < STOP_LINE_SOUTH_Y; // Y konumu durma çizgisinin Y'sinden küçük mü (yukarıda mı?)
            case WEST:  return this.currentX > STOP_LINE_WEST_X;  // X konumu durma çizgisinin X'inden büyük mü (sağda mı?)
            case EAST:  return this.currentX < STOP_LINE_EAST_X;  // X konumu durma çizgisinin X'inden küçük mü (solda mı?)
        }
        return false;
    }

    private boolean isAtStoppingLine() {
        // Aracın ön ucunun durma çizgisine çok yakın veya üzerinde olup olmadığını kontrol et.
        // Küçük bir tolerans (epsilon) eklenebilir.
        double epsilon = this.currentSpeed > 0 ? this.currentSpeed : DEFAULT_INITIAL_SPEED; // Hız kadar tolerans
        switch (this.direction) {
            case NORTH: return this.currentY <= STOP_LINE_NORTH_Y && this.currentY > STOP_LINE_NORTH_Y - DEFAULT_LENGTH;
            case SOUTH: return this.currentY >= STOP_LINE_SOUTH_Y && this.currentY < STOP_LINE_SOUTH_Y + DEFAULT_LENGTH;
            case WEST:  return this.currentX <= STOP_LINE_WEST_X && this.currentX > STOP_LINE_WEST_X - DEFAULT_LENGTH;
            case EAST:  return this.currentX >= STOP_LINE_EAST_X && this.currentX < STOP_LINE_EAST_X + DEFAULT_LENGTH;
        }
        return false;
    }

    private boolean isInIntersection() {
        // Aracın herhangi bir bölümünün kavşak alanı içinde olup olmadığını kontrol et.
        // Bu, aracın 4 köşe noktasının kavşak sınırları ile karşılaştırılmasıyla yapılabilir.
        // Basitleştirilmiş kontrol: Aracın merkezi kavşak bölgesinde mi?
        // Ya da daha iyisi: Durma çizgisini geçti mi ve çıkış çizgisine henüz gelmedi mi?
        switch (direction) {
            case NORTH: return currentY < STOP_LINE_NORTH_Y && currentY > EXIT_LINE_NORTH_Y + DEFAULT_LENGTH; // Basit bir aralık
            case SOUTH: return currentY > STOP_LINE_SOUTH_Y && currentY < EXIT_LINE_SOUTH_Y - DEFAULT_LENGTH;
            case EAST:  return currentX > STOP_LINE_EAST_X && currentX < EXIT_LINE_EAST_X - DEFAULT_LENGTH;
            case WEST:  return currentX < STOP_LINE_WEST_X && currentX > EXIT_LINE_WEST_X + DEFAULT_LENGTH;
        }
        return false;
    }

    private boolean hasPassedIntersection() {
        // Aracın TAMAMININ kavşağı terk edip etmediğini kontrol et.
        switch (this.direction) {
            // Kuzeye giden araç için Y ekseni yukarı doğru azalır. Aracın ALT KENARI çıkış çizgisinin ÜSTÜNDE mi?
            case NORTH: return (this.currentY + DEFAULT_LENGTH) < EXIT_LINE_NORTH_Y + DEFAULT_LENGTH; // Bu tanım biraz hatalı olabilir, Y ekseninin yönüne göre değişir.
            // Eğer Y yukarı doğru azalıyorsa, aracın önü EXIT_LINE_NORTH_Y'den küçük olmalı
            // Şu anki EXIT_LINE tanımı, aracın arkasının geçeceği yer.
            // Kuzey için: currentY (ön uç) < EXIT_LINE_NORTH_Y (kavşağın görsel olarak en üstü)
            return this.currentY < EXIT_LINE_NORTH_Y;


            // Güneye giden araç için Y ekseni aşağı doğru artar. Aracın ÜST KENARI çıkış çizgisinin ALTINDA mı?
            case SOUTH: // currentY (ön uç) > EXIT_LINE_SOUTH_Y (kavşağın görsel olarak en altı)
                return this.currentY > EXIT_LINE_SOUTH_Y;

            // Doğuya giden araç için X ekseni sağa doğru artar. Aracın SOL KENARI çıkış çizgisinin SAĞINDA mı?
            case EAST:  // currentX (ön uç) > EXIT_LINE_EAST_X (kavşağın görsel olarak en sağı)
                return this.currentX > EXIT_LINE_EAST_X;

            // Batıya giden araç için X ekseni sola doğru azalır. Aracın SAĞ KENARI çıkış çizgisinin SOLUNDA mı?
            case WEST:  // currentX (ön uç) < EXIT_LINE_WEST_X (kavşağın görsel olarak en solu)
                return this.currentX < EXIT_LINE_WEST_X;
        }
        return false;
    }

    private double calculateDistanceTo(Vehicle otherVehicle) {
        if (this.direction != otherVehicle.getDirection() || otherVehicle.getState() == VehicleState.PASSED ) {
            return Double.MAX_VALUE; // Farklı yönlerdelerse veya öndeki zaten geçmişse mesafe sonsuz
        }
        // Bu metod, öndeki aracın ARKA ucu ile bu aracın ÖN ucu arasındaki mesafeyi hesaplamalı.
        switch (this.direction) {
            case NORTH: // this.currentY (benim önüm) > otherVehicle.currentY + DEFAULT_LENGTH (onun arkası)
                return this.currentY - (otherVehicle.currentY + DEFAULT_LENGTH);
            case SOUTH: // otherVehicle.currentY (onun önü) > this.currentY + DEFAULT_LENGTH (benim arkam) -> Yanlış
                // otherVehicle.currentY (onun önü) - this.currentY (benim önüm) -> Bu araçlar arası merkezden merkeze gibi olur.
                // Doğrusu: öndeki aracın (otherVehicle) BAŞLANGIÇ Y'si - kendi aracımızın BİTİŞ Y'si
                return otherVehicle.currentY - (this.currentY + DEFAULT_LENGTH);
            case EAST:  // otherVehicle.currentX (onun solu) - (this.currentX + DEFAULT_WIDTH) (benim sağım)
                return otherVehicle.currentX - (this.currentX + DEFAULT_WIDTH);
            case WEST:  // this.currentX (benim solum) - (otherVehicle.currentX + DEFAULT_WIDTH) (onun sağı)
                return this.currentX - (otherVehicle.currentX + DEFAULT_WIDTH);
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