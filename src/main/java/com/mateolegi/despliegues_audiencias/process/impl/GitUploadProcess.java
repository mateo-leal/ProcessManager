package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues_audiencias.util.ProcessManager;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.despliegues_audiencias.process.RunnableProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class GitUploadProcess implements RunnableProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUploadProcess.class);

    private final Configuration configuration = new Configuration();
    private final File outputDirectory = new File(configuration.getOutputDirectory());

    @Override
    public boolean prepare() {
        LOGGER.debug("Valida que el zip se haya generado.");
        ProcessManager.setValue("Valida que el zip se haya generado.");
        var zip = new File(outputDirectory, DeployNumbers.getDeploymentVersion() + ".zip");
        return zip.exists();
    }

    @Override
    public CompletableFuture<Integer> start() {
        return gitCheckoutMaster()
                .thenComposeAsync(this::gitNewBranch)
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
        LOGGER.debug("Creamos la rama para la versión de despliegue");
        ProcessManager.setValue("Creando la rama...");
        assert resp == 0;
        return getProcess("git checkout -b " + DeployNumbers.getDeploymentVersion());
    }

    private CompletableFuture<Integer> gitAdd(int resp) {
        LOGGER.debug("Adicionamos el zip generado");
        ProcessManager.setValue("Adicionando los cambios...");
        assert resp == 0;
        return getProcess("git add " + DeployNumbers.getDeploymentVersion() + ".zip");
    }

    private CompletableFuture<Integer> gitCommit(int resp) {
        LOGGER.debug("Damos commit a los cambios");
        ProcessManager.setValue("Confirmando los cambios...");
        assert resp == 0;
        var command = "git commit -m 'Backoffice " +
                DeployNumbers.getBackofficeVersion() + ", Audiencias " +
                DeployNumbers.getAudienciasVersion() + " despliegue " +
                DeployNumbers.getDeploymentNumber() + "'";
        return getProcess(command);
    }

    private CompletableFuture<Integer> gitPush(int resp) {
        LOGGER.debug("Subimos al repositorio");
        ProcessManager.setValue("Subiendo al repositorio de despliegues...");
        assert resp == 0;
        return getProcess("git push origin " + DeployNumbers.getDeploymentVersion());
    }

    private CompletableFuture<Integer> getProcess(String command) {
        LOGGER.debug(command);
        return CompletableFuture.supplyAsync(()
                -> new ProcessManager(ProcessManager.SH, "-c", command)
                .withDirectory(outputDirectory).startAndWait())
                .exceptionally(this::handleError);
    }

    @SuppressWarnings("SameReturnValue")
    private int handleError(Throwable error) {
        LOGGER.error("Ocurrió un error subiendo a Git.", error);
        return ProcessCode.GIT_ERROR;
    }
}
