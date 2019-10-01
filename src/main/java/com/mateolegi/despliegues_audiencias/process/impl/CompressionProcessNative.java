package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class CompressionProcessNative implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionProcessCommand.class);
    private final File outputDirectory;

    public CompressionProcessNative() {
        var configuration = new Configuration();
        outputDirectory = new File(configuration.getOutputDirectory());
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
        return null;
    }

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     *
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    @Override
    public boolean validate() {
        return false;
    }
}
