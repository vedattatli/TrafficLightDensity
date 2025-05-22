module com.erciyes.edu.tr.trafficlightdensity {
    requires javafx.controls;
    requires javafx.fxml;

    //  ➡️  Bu satırı EKLE
    opens com.erciyes.edu.tr.trafficlightdensity.intersection_gui to javafx.fxml;

    // İstiyorsan ana paketi dış dünyaya açmaya devam edebilirsin
    exports com.erciyes.edu.tr.trafficlightdensity;
}
