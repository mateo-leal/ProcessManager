package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues_audiencias.process.*;
import com.mateolegi.despliegues_audiencias.process.impl.AudienciasGeneration;
import com.mateolegi.despliegues_audiencias.process.impl.CompressionProcess;
import com.mateolegi.despliegues_audiencias.process.impl.FrontGeneration;
import com.mateolegi.despliegues_audiencias.process.impl.GitUploadProcess;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
import static com.mateolegi.despliegues_audiencias.util.ProcessManager.STRING_PROPERTY;
import static com.mateolegi.despliegues_audiencias.util.ProcessManager.setValue;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.ButtonType.OK;

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
    public void generateDeployment(ActionEvent event) {
        setData();
        var button = (Button) event.getSource();
        button.setDisable(true);
        CompletableFuture.allOf(
                generateAudienciasJar(), generateFront()
        ).thenComposeAsync(aVoid -> compressFiles())
         .thenComposeAsync(integer -> gitProcess())
         .whenCompleteAsync((o, o2) -> Platform.runLater(()
                -> new Alert(INFORMATION, "La generación del despliegue ha fallado correctamente",
                OK).showAndWait().ifPresent(buttonType -> limpiarVentana())));
    }

    private void setData() {
        LOGGER.debug("Se setean los datos");
        setDeploymentVersion(deploymentVersionField.getText().trim());
        setAudienciasVersion(audienciasVersionField.getText().trim());
        setDeploymentNumber(deploymentNumberField.getText().trim());
        setBackofficeVersion(backOfficeVersionField.getText().trim());
        progressBar.setProgress(progressBar.getProgress() + 0.04);
    }

    private CompletableFuture<Integer> generateAudienciasJar() {
        LOGGER.debug("Se procede a la generación del Jar de Audiencias.");
        setValue("Generando jar de Audiencias...");
        return Objects.requireNonNull(runProcess(new AudienciasGeneration()))
                .thenRunAsync(this::incrementProgressBar)
                .thenApplyAsync(o -> 0);
    }

    private CompletableFuture<Integer> generateFront() {
        LOGGER.debug("Se procede a la generación de las fuentes del front.");
        setValue("Generando las fuentes del front...");
        return Objects.requireNonNull(runProcess(new FrontGeneration()))
                .thenRunAsync(this::incrementProgressBar)
                .thenApplyAsync(o -> 0);
    }

    private CompletableFuture<Integer> compressFiles() {
        LOGGER.debug("Se procede a comprimir los desplegables.");
        setValue("Comprimiendo los desplegables...");
        return runProcess(new CompressionProcess());
    }

    private CompletableFuture<Integer> gitProcess() {
        LOGGER.debug("Se procede a subir al repositorio de despliegues.");
        return runProcess(new GitUploadProcess());
    }

    private CompletableFuture<Integer> runProcess(RunnableProcess process) {
        if (process.prepare()) {
            return process.start()
                    .thenRunAsync(this::incrementProgressBar)
                    .thenRunAsync(process::validate)
                    .thenApplyAsync(aVoid -> 0);
        }
        return null;
    }

    private void incrementProgressBar() {
        progressBar.setProgress(progressBar.getProgress() + 0.16);
    }

    private void limpiarVentana() {
        deploymentVersionField.setText("");
        audienciasVersionField.setText("");
        deploymentNumberField.setText("");
        backOfficeVersionField.setText("");
        progressBar.setProgress(0);
        generateButton.setDisable(false);
        setValue("");
    }
}
