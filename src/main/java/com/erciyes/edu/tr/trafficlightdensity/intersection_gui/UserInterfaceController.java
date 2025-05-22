package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import com.erciyes.edu.tr.trafficlightdensity.brain.SimulationManager;
import com.erciyes.edu.tr.trafficlightdensity.brain.TrafficController;
import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
// Kullanılmayan importlar kaldırıldı
import java.util.List;
import java.util.Optional;

public class UserInterfaceController {
    // Arayüz buton vs. burda çizilecek
    // private Timeline countdownTimeline; // UserInterfaceController'da Timeline'a gerek yok
    TrafficController trafficController = new TrafficController();
    SimulationManager simulationManager = new SimulationManager();
    boolean isRandom; // Hangi modun seçildiğini tutar (true ise rastgele, false ise manuel)
    boolean simulationIsCurrentlyPaused = false; // Simülasyonun duraklatılma durumunu tutar

    @FXML
    private Pane mainPane;

    @FXML
    private Button random_select_button;

    @FXML
    private Button user_input_button;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button rerunButton; // Reset butonu

    @FXML
    private Label northTimerLabel, southTimerLabel, eastTimerLabel, westTimerLabel;

    @FXML
    private VBox topVBox; // Giriş seçme butonlarını içeren VBox

    private Direction currentDirectionForLabelUpdate; // Sadece label güncellemesi için aktif yön

    public void initialize() {
        // Başlangıç UI durumu ve bayrakların sıfırlanması
        topVBox.setVisible(true);
        mainPane.setVisible(false);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
        rerunButton.setVisible(false);
        resetTimerLabels(); // Labelları başlangıç durumuna getir

        isRandom = false; // Başlangıçta bir mod seçilmemiş
        simulationIsCurrentlyPaused = false; // Simülasyon başlangıçta duraklatılmış değil
        pauseButton.setText("Pause"); // Pause butonunun metnini "Pause" yap

        // SimulationManager'dan gelen tick (süre) bilgilerini dinle
        simulationManager.setOnTick(kalanSure -> {
            if (this.currentDirectionForLabelUpdate != null) {
                labeliGuncelle(this.currentDirectionForLabelUpdate, kalanSure);
            } else {
                // Aktif yön yoksa (örn. simülasyon durmuş veya henüz başlamamışsa) labelları temizle
                resetTimerLabels();
            }
        });

        // SimulationManager'dan gelen faz (yön) değişikliği bilgilerini dinle
        simulationManager.setOnPhaseChange(direction -> {
            this.currentDirectionForLabelUpdate = direction; // Aktif yönü sakla
            // Faz değiştiğinde, ilk tick gelene kadar label'da eski süre kalmasın diye
            // eğer yeni yön null değilse ve süre biliniyorsa label'ı güncelleyebiliriz.
            // Ancak onTick zaten ilk değeri basacağı için genellikle gerek kalmaz.
            // if (direction != null) {
            // labeliGuncelle(direction, trafficController.getGreenDuration(direction));
            // }
        });
    }

    @FXML
    private void onRandomSelect(ActionEvent e) {
        isRandom = true;
        // UI'ı simülasyon ekranına geçir
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
        startButton.setDisable(false); // Başlat butonu tıklanabilir olsun
        System.out.println("Rastgele araç sayısı modu seçildi.");
        // Araç sayıları ve süreler "Start" butonuna basılınca SimulationManager.startAutoMode içinde hesaplanacak.
        // O yüzden burada labelSureleriniGuncelle() çağırmak yerine resetTimerLabels() daha doğru olabilir
        // ya da Start'a basılana kadar bir şey göstermeyebilir. Şimdilik böyle kalsın, Start'ta güncellenir.
        resetTimerLabels(); // Ya da bir "Hazır" mesajı gösterilebilir
    }

    @FXML
    private void onUserInputSelect(ActionEvent e) {
        isRandom = false;
        trafficController.getVehicleCounts().clear(); // Önceki girişleri temizle

        for (Direction yon : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            TextInputDialog dialog = new TextInputDialog("0"); // Varsayılan değer 0 olsun
            dialog.setTitle("Araç Girişi");
            dialog.setHeaderText(yon.getTurkishName() + " yönü için araç sayısı:");
            dialog.setContentText("Sayı:");
            Optional<String> sonuc = dialog.showAndWait();
            if (sonuc.isPresent() && !sonuc.get().trim().isEmpty()) {
                try {
                    int sayi = Integer.parseInt(sonuc.get().trim());
                    trafficController.getVehicleCounts().put(yon, Math.max(0, sayi)); // Negatifse 0 ata
                } catch (NumberFormatException ex) {
                    System.err.println("Geçersiz sayı formatı: " + sonuc.get() + ". " + yon.getTurkishName() + " için 0 araç atandı.");
                    trafficController.getVehicleCounts().put(yon, 0);
                }
            } else {
                System.out.println(yon.getTurkishName() + " yönü için giriş yapılmadı veya iptal edildi. 0 araç atandı.");
                trafficController.getVehicleCounts().put(yon, 0); // Boş veya iptal durumunda 0 ata
            }
        }
        trafficController.updateDurations(); // Girilen sayılara göre süreleri hesapla
        labelSureleriniGuncelle(); // Hesaplanan süreleri GUI'de göster

        // UI'ı simülasyon ekranına geçir
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
        startButton.setDisable(false); // Başlat butonu tıklanabilir olsun
        System.out.println("Manuel araç sayısı modu seçildi ve veriler girildi.");
    }

    @FXML
    private void onStartSimulation(ActionEvent e) {
        System.out.println("Simülasyon başlatılıyor...");
        simulationIsCurrentlyPaused = false; // Simülasyon başlarken duraklatılmış değil
        pauseButton.setText("Pause");   // Pause buton metnini ayarla
        startButton.setDisable(true);   // Başlat butonunu pasif yap (tekrar basılmasın)

        if (isRandom) {
            simulationManager.startAutoMode();
        } else { // Manuel mod (!isRandom)
            simulationManager.startManualMode(trafficController.getVehicleCounts());
        }
    }

    @FXML
    private void onPauseSimulation(ActionEvent e) {
        if (!simulationManager.isRunning()) { // Simülasyon hiç başlamamışsa veya bitmişse
            System.out.println("Pause: Simülasyon çalışmıyor.");
            return;
        }

        if (simulationIsCurrentlyPaused) {
            simulationManager.resumeSimulation();
            pauseButton.setText("Pause");
            simulationIsCurrentlyPaused = false;
        } else {
            simulationManager.pauseSimulation();
            pauseButton.setText("Resume");
            simulationIsCurrentlyPaused = true;
        }
    }

    @FXML
    private void onRerunSimulation(ActionEvent e) { // Reset butonu
        System.out.println("Simülasyon sıfırlanıyor (Reset)...");
        simulationManager.stopSimulation(); // Çalışan simülasyonu ve iç durumlarını durdur/sıfırla

        // TODO: Araç animasyonları varsa buradan temizlenmeli.
        // Örnek: if (vehicleAnimator != null) vehicleAnimator.clearAllVisuals(mainPane);
        // Ya da mainPane'e doğrudan eklenen araç node'ları varsa:
        // mainPane.getChildren().removeIf(node -> node instanceof VehicleView);

        trafficController.getVehicleCounts().clear(); // Araç sayılarını sıfırla
        trafficController.updateDurations();          // Süreleri (0 olarak) güncelle

        // UI'ı ve bayrakları başlangıç durumuna getir
        initialize();
        // startButton initialize içinde gizlenip, mod seçilince görünür ve aktif hale gelecek.
    }

    private void labelSureleriniGuncelle() {
        if (trafficController == null) return;
        northTimerLabel.setText(trafficController.getGreenDuration(Direction.NORTH) + " sn");
        eastTimerLabel.setText(trafficController.getGreenDuration(Direction.EAST) + " sn");
        southTimerLabel.setText(trafficController.getGreenDuration(Direction.SOUTH) + " sn");
        westTimerLabel.setText(trafficController.getGreenDuration(Direction.WEST) + " sn");
    }

    private void labeliGuncelle(Direction aktifYon, int sure) {
        resetTimerLabels();
        String sureText = (sure >= 0 ? sure : "0") + " sn";
        switch (aktifYon) {
            case NORTH -> northTimerLabel.setText(sureText);
            case SOUTH -> southTimerLabel.setText(sureText);
            case EAST  -> eastTimerLabel.setText(sureText);
            case WEST  -> westTimerLabel.setText(sureText);
        }
    }

    private void resetTimerLabels() {
        northTimerLabel.setText("—");
        southTimerLabel.setText("—");
        eastTimerLabel.setText("—");
        westTimerLabel.setText("—");
    }
}