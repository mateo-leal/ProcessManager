package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.git.GitManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.GIT_ERROR;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class CloneBaseProcess implements AsyncProcess {

    private static final Configuration CONFIGURATION = new Configuration();
    private static final Logger LOGGER = LoggerFactory.getLogger(CloneBaseProcess.class);

    private final File outputDirectory = new File(CONFIGURATION.getOutputDirectory());
    private final GitManager gitManager = new GitManager(new File(CONFIGURATION.getOutputDirectory()),
            CONFIGURATION.getGitUser(), CONFIGURATION.getGitPassword());

    /**
     * Prepara los archivos y realiza las respectivas validaciones
     * antes de realizar un proceso.
     *
     * @return resultado de la preparación, si es falso no se podría
     * ejecutar el proceso.
     */
    @Override
    public boolean prepare() {
        var gitFolder = new File(outputDirectory, ".git");
        setValue("Validando existencia del repositorio local...");
        if (gitFolder.exists()) {
            return true;
        }
        return outputDirectory.delete() && outputDirectory.getParentFile().mkdirs();
    }

    /**
     * Encapsula en un futuro un proceso y lo retorna.
     * Este futuro debe retornar un entero como respuesta,
     * siendo 0 cuando el proceso es exitoso, en otro caso debe
     * retornar un error que sea especificado en la
     * clase {@link ProcessCode}
     *
     * @return futuro
     */
    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Se procede a descarga del repositorio base para los despliegues.");
        return CompletableFuture.supplyAsync(() -> {
            var gitFolder = new File(outputDirectory, ".git");
            if (!gitFolder.exists()) {
                setValue("Clonando repositorio de despliegues...");
                try {
                    gitManager.cloneRepo(CONFIGURATION.getGitRemote());
                } catch (GitAPIException e) {
                    LOGGER.error(e.getMessage(), e);
                    return GIT_ERROR;
                }
            }
            return 0;
        });
    }

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     *
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    @Override
    public boolean validate() {
        var gitFolder = new File(outputDirectory, ".git");
        return outputDirectory.isDirectory() && gitFolder.exists();
    }
}
