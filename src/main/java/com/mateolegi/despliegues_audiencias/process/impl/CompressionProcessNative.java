package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.COMPRESSION_GENERATION;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getDeploymentVersion;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class CompressionProcessNative implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionProcessNative.class);
    private final File outputDirectory;
    private final File zipFile;

    public CompressionProcessNative() {
        var configuration = new Configuration();
        outputDirectory = new File(configuration.getOutputDirectory());
        zipFile = new File(outputDirectory, getDeploymentVersion() + ".zip");
    }

    /**
     * Prepara los archivos y realiza las respectivas validaciones
     * antes de realizar un proceso.
     *
     * @return resultado de la preparación, si es falso no se podría
     * ejecutar el proceso.
     */
    @Override
    public boolean prepare() {
        LOGGER.debug("Valida que los dos procesos anteriores hayan sido correctos.");
        setValue("Validando el resultado de los procesos...");
        var audienciasDir = new File(outputDirectory, "audiencias");
        var htmlDir = new File(outputDirectory, "html");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        return audienciasDir.exists() && htmlDir.exists();
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                new ZipFile(zipFile)
                        .setOutput(System.out)
                        .zip(List.of(
                                new File(outputDirectory, "audiencias"),
                                new File(outputDirectory, "html"),
                                new File(outputDirectory, "backoffice")));
                return 0;
            } catch (IOException e) {
                LOGGER.error("Error comprimiendo archivos", e);
                return COMPRESSION_GENERATION;
            }
        });
    }

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    @Override
    public boolean validate() {
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
        return zipFile.exists();
    }
}
