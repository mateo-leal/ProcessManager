package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.git.GitManager;
import com.mateolegi.despliegues_audiencias.util.ProcessFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.GIT_ERROR;

public class GitUploadProcessNative implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUploadProcessNative.class);

    private static final Configuration CONFIGURATION = new Configuration();
    private final File outputDirectory = new File(CONFIGURATION.getOutputDirectory());
    private final GitManager gitManager = new GitManager(new File(CONFIGURATION.getOutputDirectory()),
            CONFIGURATION.getGitUser(), CONFIGURATION.getGitPassword());

    @Override
    public boolean prepare() {
        LOGGER.debug("Valida que el zip se haya generado.");
        ProcessFactory.setValue("Valida que el zip se haya generado.");
        var zip = new File(outputDirectory, DeployNumbers.getDeploymentVersion() + ".zip");
        return zip.exists();
    }

    @Override
    public CompletableFuture<Integer> start() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                gitInitRepo();
                gitCheckoutMaster();
                gitNewBranch();
                gitAdd();
                gitCommit();
                gitPush();
                return 0;
            } catch (GitAPIException | IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return GIT_ERROR;
        });
    }

    @Override
    public boolean validate() {
        return true;
    }

    private void gitInitRepo() throws IOException {
        var localPath = new File(CONFIGURATION.getOutputDirectory());
        var localRepo = new File(localPath, ".git");
        if (localRepo.exists()) {
            gitManager.openRepo();
        }
    }

    private void gitCheckoutMaster() throws GitAPIException {
        LOGGER.debug("Nos movemos a la rama master");
        gitManager.checkoutMaster();
    }

    private void gitNewBranch() throws GitAPIException {
        LOGGER.debug("Creamos la rama para la versión de despliegue");
        ProcessFactory.setValue("Creando la rama...");
        gitManager.createBranch();
    }

    private void gitAdd() throws GitAPIException {
        LOGGER.debug("Adicionamos el zip generado");
        ProcessFactory.setValue("Adicionando los cambios...");
        gitManager.addToRepo();
    }

    private void gitCommit() throws GitAPIException {
        LOGGER.debug("Damos commit a los cambios");
        ProcessFactory.setValue("Confirmando los cambios...");
        gitManager.commitToRepo();
    }

    private void gitPush() throws GitAPIException {
        LOGGER.debug("Subimos al repositorio");
        ProcessFactory.setValue("Subiendo al repositorio de despliegues...");
        gitManager.pushToRepo();
    }
}
