package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.Event;
import com.mateolegi.despliegues_audiencias.constant.Constants;
import com.mateolegi.despliegues_audiencias.process.ProcessSet;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.ConfirmBox;
import com.mateolegi.despliegues_audiencias.util.VersionGetter;
import com.mateolegi.git.GitManager;
import com.mateolegi.util.EmitterOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mateolegi.despliegues_audiencias.App.getStage;
import static com.mateolegi.despliegues_audiencias.constant.Constants.NUMBER_OF_PROCESS;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.STRING_PROPERTY;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
    private static final Configuration CONFIGURATION = new Configuration();

    @FXML private TextField deploymentVersionField;
    @FXML private TextField audienciasVersionField;
    @FXML private TextField deploymentNumberField;
    @FXML private TextField backOfficeVersionField;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressIndicatorLabel;
    @FXML private Button generateButton;
    @FXML private TextArea txtAreaOutput;

    @FXML
    public void initialize() {
        progressIndicatorLabel.textProperty().bind(STRING_PROPERTY);
        deploymentVersionField.setDisable(true);
        generateButton.setDisable(true);
        initializeTextArea();
        getNextDeployVersion();
        getAppVersion();
    }

    @FXML
    public void generateDeployment() {
        setData();
        disableFields(true);
        Platform.runLater(() -> getStage().getScene().setCursor(Cursor.WAIT));
        Root.get()
                .on(Root.PROCESS_FINISHED, this::incrementProgressBar)
                .on(Root.SUCCESS, this::limpiarVentana)
                .on(Root.ERROR, this::onError)
                .on(Constants.Event.GIT_CONFIRM, this::onGitConfirmation)
                .on(Constants.Event.DEPLOY_CONFIRM, this::onDeployConfirmation)
                .withManager(ProcessSet.getManager())
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
    }

    private void incrementProgressBar(Event e) {
        Platform.runLater(() -> progressBar.setProgress(progressBar.getProgress() + (1d / NUMBER_OF_PROCESS)));
    }

    private void limpiarVentana(Event e) {
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

    private void onError(Event e) {
        Platform.runLater(() -> {
            new Alert(ERROR, "Ocurrió un error generando la versión de despliegue.\n" +
                    "Por favor revise el log para obtener detalles.", ButtonType.OK).show();
            progressBar.setProgress(0);
            disableFields(false);
            setValue("");
            getStage().getScene().setCursor(Cursor.DEFAULT);
        });
    }

    private boolean onGitConfirmation(Event e) {
        if (CONFIGURATION.shouldUploadGit()) {
            return true;
        }
        var response = new AtomicBoolean();
        Platform.runLater(() -> response.set(new ConfirmBox("¿Desea subir la versión al servidor de Git?")
                .showAndWait()
                .filter(ButtonType.YES::equals).isPresent()));
        return response.get();
    }

    private boolean onDeployConfirmation(Event e) {
        if (CONFIGURATION.shouldDeploy()) {
            return true;
        }
        var response = new AtomicBoolean();
        Platform.runLater(() -> response.set(new ConfirmBox("¿Desea desplegar la versión en el servidor de pruebas?")
                .showAndWait()
                .filter(ButtonType.YES::equals).isPresent()));
        return response.get();
    }

    private void getNextDeployVersion() {
        new Thread(() -> {
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.WAIT));
            var nextVersion = new GitManager(new File(CONFIGURATION.getOutputDirectory()), CONFIGURATION.getGitUser(),
                    CONFIGURATION.getGitPassword()).nextVersion(CONFIGURATION.getGitRemote());
            Platform.runLater(() -> {
                deploymentVersionField.setText(nextVersion);
                deploymentVersionField.setDisable(false);
                generateButton.setDisable(false);
            });
            Platform.runLater(() -> getStage().getScene().setCursor(Cursor.DEFAULT));
        }).start();
    }

    private void getAppVersion() {
        new Thread(() -> {
            var versionGetter = new VersionGetter();
            Platform.runLater(() -> audienciasVersionField.setText(versionGetter.getAudienciasVersion()));
            Platform.runLater(() -> deploymentNumberField.setText(versionGetter.getAudienciasDeploy()));
        }).start();
    }

    private void initializeTextArea() {
        EmitterOutputStream.on(s -> Platform.runLater(() -> txtAreaOutput.appendText(s)));
    }
}
