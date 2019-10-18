package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues.process.Event;
import com.mateolegi.despliegues_audiencias.constant.Constants;
import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.despliegues_audiencias.util.ProcessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.GIT_ERROR;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class GitUploadProcessCommand implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUploadProcessCommand.class);

    private final Configuration configuration = new Configuration();
    private final File outputDirectory = new File(configuration.getOutputDirectory());

    @Override
    public boolean prepare() {
        if (Event.Confirmation.APPROVED != Root.get().emitConfirmation(Constants.Event.GIT_CONFIRM)) {
            return false;
        }
        LOGGER.debug("Valida que el zip se haya generado.");
        setValue("Valida que el zip se haya generado.");
        var zip = new File(outputDirectory, DeployNumbers.getDeploymentVersion() + ".zip");
        return zip.exists();
    }

    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Se procede a subir al repositorio de despliegues.");
        setValue("Subiendo despliegables al repositorio...");
        return gitCheckoutMaster()
                .thenCompose(this::gitNewBranch)
                .thenComposeAsync(this::gitAdd)
                .thenComposeAsync(this::gitCommit)
                .thenComposeAsync(this::gitPush);
    }

    @Override
    public boolean validate() {
        return true;
    }

    private CompletableFuture<Integer> gitCheckoutMaster() {
        LOGGER.debug("Nos movemos a la rama master");
        return getProcess("git checkout master");
    }

    private CompletableFuture<Integer> gitNewBranch(int resp) {
        if (resp != 0) {
            return CompletableFuture.supplyAsync(() -> GIT_ERROR);
        }
        LOGGER.debug("Creamos la rama para la versión de despliegue");
        setValue("Creando la rama...");
        return getProcess("git checkout -b " + DeployNumbers.getDeploymentVersion());
    }

    private CompletableFuture<Integer> gitAdd(int resp) {
        if (resp != 0) {
            return CompletableFuture.supplyAsync(() -> GIT_ERROR);
        }
        LOGGER.debug("Adicionamos el zip generado");
        setValue("Adicionando los cambios...");
        return getProcess("git add " + DeployNumbers.getDeploymentVersion() + ".zip");
    }

    private CompletableFuture<Integer> gitCommit(int resp) {
        if (resp != 0) {
            return CompletableFuture.supplyAsync(() -> GIT_ERROR);
        }
        LOGGER.debug("Damos commit a los cambios");
        setValue("Confirmando los cambios...");
        var command = "git commit -m 'Backoffice " +
                DeployNumbers.getBackofficeVersion() + ", Audiencias " +
                DeployNumbers.getAudienciasVersion() + " despliegue " +
                DeployNumbers.getDeploymentNumber() + "'";
        return getProcess(command);
    }

    private CompletableFuture<Integer> gitPush(int resp) {
        if (resp != 0) {
            return CompletableFuture.supplyAsync(() -> GIT_ERROR);
        }
        LOGGER.debug("Subimos al repositorio");
        setValue("Subiendo al repositorio de despliegues...");
        return getProcess("git push origin " + DeployNumbers.getDeploymentVersion());
    }

    private CompletableFuture<Integer> getProcess(String command) {
        LOGGER.debug(command);
        return CompletableFuture.supplyAsync(()
                -> new ProcessFactory(ProcessFactory.SH, "-c", command)
                .withDirectory(outputDirectory).startAndWait())
                .exceptionally(this::handleError);
    }

    @SuppressWarnings("SameReturnValue")
    private int handleError(Throwable error) {
        LOGGER.error("Ocurrió un error subiendo a Git.", error);
        return ProcessCode.GIT_ERROR;
    }
}
