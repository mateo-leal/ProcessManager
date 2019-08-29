package com.mateolegi.despliegues_audiencias;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.STRING_PROPERTY;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @FXML private TextField deploymentVersionField;
    @FXML private TextField audienciasVersionField;
    @FXML private TextField deploymentNumberField;
    @FXML private TextField backOfficeVersionField;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressIndicatorLabel;
    @FXML private Button generateButton;

    @FXML
    public void initialize() {
        progressIndicatorLabel.textProperty().bind(STRING_PROPERTY);
    }

    @FXML
    public void generateDeployment() {
        setData();
        disableFields(true);
        new MainProcess()
                .onProcessFinished(this::incrementProgressBar)
                .onSuccess(this::limpiarVentana)
                .onError(this::onError)
                .run();
    }

    private void disableFields(boolean disable) {
        generateButton.setDisable(disable);
        deploymentVersionField.setDisable(disable);
        deploymentNumberField.setDisable(disable);
        audienciasVersionField.setDisable(disable);
        backOfficeVersionField.setDisable(disable);
    }

    private void setData() {
        LOGGER.debug("Se setean los datos");
        setDeploymentVersion(deploymentVersionField.getText().trim());
        LOGGER.debug("Deployment version: " + getDeploymentVersion());
        setAudienciasVersion(audienciasVersionField.getText().trim());
        LOGGER.debug("Audiencias version: " + getAudienciasVersion());
        setDeploymentNumber(deploymentNumberField.getText().trim());
        LOGGER.debug("Deployment number: " + getDeploymentNumber());
        setBackofficeVersion(backOfficeVersionField.getText().trim());
        LOGGER.debug("Backoffice version: " + getBackofficeVersion());
        progressBar.setProgress(progressBar.getProgress() + 0.04);
    }

    private void incrementProgressBar() {
        progressBar.setProgress(progressBar.getProgress() + 0.16);
    }

    private void limpiarVentana() {
        new Alert(CONFIRMATION, "La versión de despliegue se generó correctamente.", ButtonType.OK).show();
        deploymentVersionField.setText("");
        audienciasVersionField.setText("");
        deploymentNumberField.setText("");
        backOfficeVersionField.setText("");
        progressBar.setProgress(0);
        disableFields(false);
        setValue("");
    }

    private void onError() {
        new Alert(ERROR, "Ocurrió un error generando la versión de despliegue.\n" +
                "Por favor revise el log para obtener detalles.", ButtonType.OK).show();
        progressBar.setProgress(0);
        disableFields(false);
        setValue("");
    }
}
