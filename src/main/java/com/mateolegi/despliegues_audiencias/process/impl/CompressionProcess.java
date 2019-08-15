package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues_audiencias.util.ProcessManager;
import com.mateolegi.despliegues_audiencias.process.AsyncProcess;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getDeploymentVersion;
import static com.mateolegi.despliegues_audiencias.util.ProcessManager.*;

/**
 * Realiza el proceso de compresión de los archivos ubicados en
 * los directorios del jar de audiencias, el front y si existe
 * el jar de backoffice.
 */
public class CompressionProcess implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionProcess.class);

    private final File outputDirectory;

    public CompressionProcess() {
        var configuration = new Configuration();
        outputDirectory = new File(configuration.getOutputDirectory());
    }

    @Override
    public boolean prepare() {
        LOGGER.debug("Valida que los dos procesos anteriores hayan sido correctos.");
        setValue("Validando el resultado de los procesos...");
        var audienciasDir = new File(outputDirectory, "audiencias");
        var htmlDir = new File(outputDirectory, "html");
        return audienciasDir.exists() && htmlDir.exists();
    }

    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Inicia la compresión de los desplegables.");
        setValue("Comprimiendo los desplegables...");
        var command = getCompressionCommand();
        return CompletableFuture.supplyAsync(()
                -> new ProcessManager(SH, "-c", command)
                .withDirectory(outputDirectory).startAndWait())
                .exceptionally(this::handleError);
    }

    @Override
    public boolean validate() {
        var zip = new File(outputDirectory, getDeploymentVersion() + ".zip");
        var audienciasDir = new File(outputDirectory, "audiencias");
        var htmlDir = new File(outputDirectory, "html");
        var backofficeDir = new File(outputDirectory, "backoffice");
        try {
            LOGGER.debug("Se eliminan los directorios de desplegables.");
            setValue("Eliminando directorios de desplegables...");
            if (audienciasDir.exists()) {
                LOGGER.debug("Eliminando directorio de Audiencias...");
                setValue("Eliminando directorio de Audiencias...");
                FileUtils.deleteDirectory(audienciasDir);
            }
            if (htmlDir.exists()) {
                LOGGER.debug("Eliminando directorio de HTML...");
                setValue("Eliminando directorio de HTML...");
                FileUtils.deleteDirectory(htmlDir);
            }
            if (backofficeDir.exists()) {
                LOGGER.debug("Eliminando directorio de Backoffice...");
                setValue("Eliminando directorio de Backoffice...");
                FileUtils.deleteDirectory(backofficeDir);
            }
        } catch (IOException e) {
            LOGGER.error("Error eliminando las carpetas", e);
            return false;
        }
        LOGGER.debug("Valida que el zip se haya generado.");
        setValue("Validando ZIP...");
        return zip.exists();
    }

    private String getCompressionCommand() {
        var builder = new StringBuilder("zip -r ")
                .append(getDeploymentVersion())
                .append(".zip audiencias/ html/");
        var backofficeDir = new File(outputDirectory, "backoffice");
        if (backofficeDir.exists()) {
            builder.append(" backoffice/");
        }
        LOGGER.debug(builder.toString());
        return builder.toString();
    }

    @SuppressWarnings("SameReturnValue")
    private int handleError(Throwable error) {
        LOGGER.error("Ocurrió un error durante la compresión de los desplegables.", error);
        return ProcessCode.COMPRESSION_GENERATION;
    }
}
