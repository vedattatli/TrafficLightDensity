package com.erciyes.edu.tr.trafficlightdensity.intersection_gui;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.event.ActionEvent;

public class UserInterfaceController {
    // Arayüz buton vs. burda çizilecek

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
    private Button rerunButton;

    @FXML
    private Label nortTimerLabel,southTimerLabel,eastTimerLabel,westTimerLabel;

    @FXML
    private VBox topVBox;

    @FXML
    private void onRandomSelect(ActionEvent e)
    {
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);


    }

    @FXML
    private void onUserInputSelect(ActionEvent e)
    {
        topVBox.setVisible(false);
        mainPane.setVisible(true);
        startButton.setVisible(true);
        pauseButton.setVisible(true);
        rerunButton.setVisible(true);
    }

    @FXML
    private void onStartSimulation(ActionEvent e)
    {
        System.out.println("Simulasyon başlatıldı!");
        //ileride dolcak timer ve araç animasyonları vb.
    }

    @FXML
    private void onPauseSimulation(ActionEvent e )
    {

    }

    @FXML
    private void onRerunSimulation(ActionEvent e)
    {

    }

    public void initialize()
    {
        //Seçim butonlarını ve başlık etiketlerini göster
        topVBox.setVisible(true);
        // Kavşak alanını ve kontrol butonlarını gizle
        mainPane.setVisible(false);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
        rerunButton.setVisible(false);
    }


}