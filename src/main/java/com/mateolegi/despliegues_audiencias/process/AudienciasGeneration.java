package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.ProcessCode;
import com.mateolegi.despliegues_audiencias.util.ProcessManager;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.ProcessManager.SH;
import static com.mateolegi.despliegues_audiencias.util.ProcessManager.setValue;

public class AudienciasGeneration implements RunnableProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudienciasGeneration.class);

    private final File outputDirectory;

    public AudienciasGeneration() {
        var configuration = new Configuration();
        outputDirectory = new File(configuration.getOutputDirectory());
    }

    @Override
    public boolean prepare() {
        var audienciasOutput = new File(outputDirectory, "audiencias");
        if (audienciasOutput.exists()) {
            LOGGER.debug("Se elimina el directorio de jar antiguo");
            Platform.runLater(() -> setValue("Eliminando directorio de jar antiguo..."));
            try {
                FileUtils.deleteDirectory(audienciasOutput);
            } catch (IOException e) {
                LOGGER.error("Error eliminando el directorio del jar de Audiencias", e);
                return false;
            }
        }
        LOGGER.debug("Creando directorio para el jar de Audiencias");
        return audienciasOutput.mkdirs();
    }

    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Generando Jar de Audiencias...");
        setValue("Generando jar de Audiencias...");
        return CompletableFuture.supplyAsync(()
                -> new ProcessManager(SH, "-c", "ant")
                .withDirectory(outputDirectory).startAndWait())
                .exceptionally(this::handleError);
    }

    @Override
    public boolean validate() {
        LOGGER.debug("Validando la creación del Jar y la exportación de las librerías.");
        setValue("Validando la creación del jar y la exportación de las librerías...");
        var audienciasOutput = new File(outputDirectory, "audiencias");
        if (audienciasOutput.exists() && audienciasOutput.isDirectory()) {
            var audienciasJar = new File(audienciasOutput, "audiencias.jar");
            var audienciasLib = new File(audienciasOutput, "audiencias_lib");
            LOGGER.debug("Se generó correctamente el Jar de Audiencias.");
            return audienciasJar.exists() && audienciasLib.exists();
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private int handleError(Throwable error) {
        LOGGER.error("Ocurrió un error durante la generación del jar de Audiencias", error);
        return ProcessCode.AUDIENCIAS_JAR_GENERATION;
    }
}
