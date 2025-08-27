# Traffic Light Density Simulation 🚦

<p align="center">
  <img src="https://img.shields.io/badge/Java-JavaFX-orange" alt="JavaFX">
  <img src="https://img.shields.io/badge/Build-Maven-blue" alt="Maven">
  <img src="https://img.shields.io/badge/Status-Active-brightgreen" alt="Status">
</p>

---

## 📖 Proje Açıklaması

Traffic Light Density Simulation, trafik yoğunluğunu baz alarak trafik ışığı sürelerini **dinamik** şekilde ayarlayan bir kavşak simülasyonudur. Kullanıcı, araç sayılarını manuel girebilir veya rastgele üretebilir. Simülasyon, **JavaFX** tabanlı görsel bir arayüzle çalışır.

---

## ✨ Özellikler

* **Dinamik Işık Süreleri:** Araç yoğunluğuna göre ışık süreleri anlık ayarlanır.
* **İki Mod:**

  * **Manuel Mod:** Araç sayısını kullanıcı belirler.
  * **Otomatik Mod:** Araç sayısı rastgele atanır.
* **Görsel Simülasyon:** Araçların geçişi ve ışık renkleri canlı olarak izlenir.
* **Detaylı Bilgi Paneli:** Anlık araç sayısı, yeşil-kırmızı süreleri gibi veriler gösterilir.
* **Simülasyon Kontrolleri:** Başlat, duraklat, devam et, yeniden başlat.

---

## 🛠 Teknolojiler

* **Java**: Ana programlama dili.
* **JavaFX**: Kullanıcı arayüzü için.
* **Maven**: Proje ve bağımlılık yönetimi.

---

## 🚀 Nasıl Çalıştırılır?

```bash
# Projeyi klonla
git clone https://github.com/vedattatli/trafficlightdensity.git

# IDE ile aç (IntelliJ IDEA, Eclipse vb.)

# Maven bağımlılıklarını yükle
# pom.xml üzerinden otomatik veya manuel yükleme

# Uygulamayı çalıştır
TrafficSimApp.java dosyasını çalıştır
```

---

## 📂 Proje Yapısı

```plaintext
trafficlightdensity/
├── brain/
│   ├── SimulationManager.java      # Simülasyon akışını yönetir
│   ├── TrafficController.java      # Işık sürelerini hesaplar
│   └── CycleManager.java           # Döngüleri yönetir
│
├── intersection_gui/
│   ├── UserInterfaceController.java # FXML arayüz yönetimi
│   ├── VehicleAnimation.java        # Araç animasyonları
│   ├── TimerDisplay.java            # Zamanlayıcı gösterimi
│   └── TrafficLightColorUpdater.java# Işık renk güncelleme
│
├── road_objects/                    # Araç, yön, ışık fazı tanımları
├── resources/                       # FXML ve diğer kaynaklar
└── TrafficSimApp.java                # Giriş noktası
```
---

## 📜 Lisans

Bu proje MIT Lisansı ile lisanslanmıştır.

---

### 🧑‍💻 Geliştirici

**Vedat Tatlı**
GitHub: [vedattatli](https://github.com/vedattatli)

---
