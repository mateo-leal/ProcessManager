package com.mateolegi.despliegues_audiencias.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ConfirmBox {

    private final Alert alert;

    public ConfirmBox(String text) {
        alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO);
    }

    public Optional<ButtonType> showAndWait() {
        return alert.showAndWait();
    }
}
