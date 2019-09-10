package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues_audiencias.exception.ProcessException;
import com.mateolegi.despliegues_audiencias.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.process.impl.*;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.ConfirmBox;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

class MainProcess implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainProcess.class);
    private static final Configuration CONFIGURATION = new Configuration();
    private Runnable postProcess;
    private Runnable onSuccess;
    private Runnable onError;

    @Override
    public void run() {
        cloneBase().whenComplete((isGitRepositoryDownloaded, __)
                -> generateAudienciasJar().thenAcceptBothAsync(generateFront(), (jar, front) -> {
                    if (jar && front) {
                        if (isGitRepositoryDownloaded) {
                            if (CONFIGURATION.shouldUploadGit()) {
                                postGeneration();
                            } else {
                                Platform.runLater(() -> new ConfirmBox("¿Desea subir la versión al servidor de Git?")
                                        .showAndWait()
                                        .filter(ButtonType.YES::equals)
                                        .ifPresentOrElse(___ -> postGeneration(), onSuccess));
                            }
                        } else {
                            onSuccess.run();
                        }
                    } else {
                        onError.run();
                        throw new ProcessException("Error generando desplegables.");
                    }
                }).exceptionally(throwable -> {
                    LOGGER.error(throwable.getMessage(), throwable);
                    onError.run();
                    return null;
                }));
    }

    MainProcess onSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    MainProcess onError(Runnable onError) {
        this.onError = onError;
        return this;
    }

    MainProcess onProcessFinished(Runnable postProcess) {
        this.postProcess = postProcess;
        return this;
    }

    private void postGeneration() {
        compressFiles().thenCompose(comprimioCorrectamente -> {
            if (comprimioCorrectamente) {
                return gitProcess();
            }
            throw new ProcessException("Error comprimiendo los archivos generados");
        }).thenRun(() -> {
            if (CONFIGURATION.shouldDeploy()) {
                postGit();
            } else {
                Platform.runLater(()
                        -> new ConfirmBox("¿Desea desplegar la versión en el servidor de pruebas?")
                        .showAndWait()
                        .filter(ButtonType.YES::equals)
                        .ifPresentOrElse(__ -> postGit(), onSuccess));
            }
        })
        .exceptionally(throwable -> {
            LOGGER.error(throwable.getMessage(), throwable);
            onError.run();
            return null;
        });
    }

    private void postGit() {
        deploymentProcess().whenComplete((desplegoCorrectamente, throwable) -> {
            if (desplegoCorrectamente) onSuccess.run();
            else throw new ProcessException("Error desplegando la versión");
            if (throwable != null) throw new ProcessException("Error desplegando la versión", throwable);
        });
    }

    private CompletableFuture<Boolean> cloneBase() {
        LOGGER.debug("Se procede a descarga del repositorio base para los despliegues.");
        setValue("Clonando repositorio de despliegues...");
        return runProcess(new ClonningBaseProcess());
    }

    private CompletableFuture<Boolean> generateAudienciasJar() {
        LOGGER.debug("Se procede a la generación del Jar de Audiencias.");
        setValue("Generando jar de Audiencias...");
        return runProcess(new AudienciasGeneration());
    }

    private CompletableFuture<Boolean> generateFront() {
        LOGGER.debug("Se procede a la generación de las fuentes del front.");
        setValue("Generando las fuentes del front...");
        return runProcess(new FrontGeneration());
    }

    private CompletableFuture<Boolean> compressFiles() {
        LOGGER.debug("Se procede a comprimir los desplegables.");
        setValue("Comprimiendo los desplegables...");
        return runProcess(new CompressionProcess());
    }

    private CompletableFuture<Boolean> gitProcess() {
        LOGGER.debug("Se procede a subir al repositorio de despliegues.");
        return runProcess(new GitUploadProcessCommand());
    }

    private CompletableFuture<Boolean> deploymentProcess() {
        LOGGER.debug("Desplegando en ambiente de pruebas.");
        return runProcess(new SSHDeploy());
    }

    private CompletableFuture<Boolean> runProcess(AsyncProcess process) {
        if (process.prepare()) {
            return process.start()
                    .thenRun(postProcess)
                    .thenApply(__ -> process.validate());
        }
        return CompletableFuture.completedFuture(false);
    }
}
