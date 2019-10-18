package com.mateolegi.despliegues_audiencias;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AdminServicesController {

    @FXML private Button btnIniciar;
    @FXML private Button btnReiniciar;
    @FXML private Button btnDetener;


    public void iniciarServicios(ActionEvent actionEvent) {
        disableButtons(true);
        //Root.newRoot()
                //.on(Root.PROCESS_FINISHED, event -> Platform.runLater(new ()))
    }

    public void reiniciarServicios(ActionEvent actionEvent) {
    }

    public void detenerServicios(ActionEvent actionEvent) {

    }

    private void disableButtons(boolean disable) {
        btnIniciar.setDisable(disable);
        btnReiniciar.setDisable(disable);
        btnDetener.setDisable(disable);
    }
}
