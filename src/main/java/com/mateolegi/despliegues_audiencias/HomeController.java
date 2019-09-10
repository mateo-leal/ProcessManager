package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues_audiencias.util.GitManager;
import com.mateolegi.despliegues_audiencias.util.VersionGetter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mateolegi.despliegues_audiencias.App.getStage;
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
        deploymentVersionField.setDisable(true);
        generateButton.setDisable(true);
        new Thread(() -> {
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.WAIT));
            var nextVersion = new GitManager().nextVersion();
            Platform.runLater(() -> {
                deploymentVersionField.setText(nextVersion);
                deploymentVersionField.setDisable(false);
                generateButton.setDisable(false);
            });
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.DEFAULT));
        }).start();
        new Thread(() -> {
            var versionGetter = new VersionGetter();
            Platform.runLater(() -> audienciasVersionField.setText(versionGetter.getAudienciasVersion()));
            Platform.runLater(() -> deploymentNumberField.setText(versionGetter.getAudienciasDeploy()));
        }).start();
    }

    @FXML
    public void generateDeployment() {
        setData();
        disableFields(true);
        Platform.runLater(() -> getStage().getScene().setCursor(Cursor.WAIT));
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
        Platform.runLater(() -> progressBar.setProgress(progressBar.getProgress() + 0.16));
    }

    private void limpiarVentana() {
        Platform.runLater(() -> {
            new Alert(CONFIRMATION, "La versión de despliegue se generó correctamente.", ButtonType.OK).show();
            deploymentVersionField.setText("");
            audienciasVersionField.setText("");
            deploymentNumberField.setText("");
            backOfficeVersionField.setText("");
            progressBar.setProgress(0);
            disableFields(false);
            setValue("");
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.DEFAULT));
        });
    }

    private void onError() {
        Platform.runLater(() -> {
            new Alert(ERROR, "Ocurrió un error generando la versión de despliegue.\n" +
                    "Por favor revise el log para obtener detalles.", ButtonType.OK).show();
            progressBar.setProgress(0);
            disableFields(false);
            setValue("");
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.DEFAULT));
        });
    }
}
