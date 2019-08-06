package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.ProcessManager;
import com.mateolegi.despliegues_audiencias.util.ProcessCode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.ProcessManager.*;

public class FrontGeneration implements RunnableProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontGeneration.class);

    private final Configuration configuration = new Configuration();
    private final File outputDirectory = new File(configuration.getOutputDirectory());
    private final File frontDirectory = new File(configuration.getFrontDirectory());

    @Override
    public boolean prepare() {
        LOGGER.debug("Se valida que existan las fuentes del front.");
        setValue("Validando fuentes de front...");
        if (!frontDirectory.exists()) {
            LOGGER.error("No existen las fuentes de front, se cancela la generación de este.");
            return false;
        }
        LOGGER.debug("Si existe algún despliegue anterior del front generaco se elimina.");
        var deployFolder = new File(frontDirectory, "deploy");
        if (deployFolder.exists()) {
            try {
                LOGGER.debug("Se elimina despliegue anterior...");
                setValue("Eliminando despliegue anterior...");
                FileUtils.deleteDirectory(deployFolder);
            } catch (IOException e) {
                LOGGER.error("Error eliminando el directorio de despliegues del front", e);
                return false;
            }
        }
        LOGGER.debug("Si existe directorio de front en la carpeta de despliegues se elimina.");
        var htmlFolder = new File(outputDirectory, "html");
        if (htmlFolder.exists()) {
            try {
                LOGGER.debug("Se elimina despliegue anterior...");
                setValue("Eliminando despliegue anterior...");
                FileUtils.deleteDirectory(htmlFolder);
            } catch (IOException e) {
                LOGGER.error("Error eliminando el directorio de despliegues del front", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public CompletableFuture<Integer> start() {
        LOGGER.debug("Inicia la generación del desplegable del front");
        setValue("Generando desplegable del front...");
        return CompletableFuture.supplyAsync(()
                -> new ProcessManager(SH, "-c", "npm run generate")
                .withDirectory(frontDirectory).startAndWait())
                .thenApplyAsync(this::moveDeployFolder)
                .thenApplyAsync(this::renameDeployFolder)
                .thenComposeAsync(this::unzipDeploy)
                .thenApplyAsync(this::deleteZip)
                .exceptionally(this::handleError);
    }

    @Override
    public boolean validate() {
        var htmlFolder = new File(outputDirectory, "html");
        if (htmlFolder.exists()) {
            File backofficeFolder = new File(htmlFolder, "backoffice");
            if (backofficeFolder.exists()) {
                return Objects.requireNonNull(backofficeFolder.listFiles()).length > 0;
            }
        }
        LOGGER.debug("El front no se generó correctamente.");
        setValue("El front no se generó correctamente.");
        return false;
    }

    private int moveDeployFolder(int resp) {
        LOGGER.debug("Se mueve la carpeta de despliegues del las fuentes de front a la carpeta de despliegues.");
        setValue("Moviendo la carpeta de deploy de las fuentes de front a la carpeta de despliegues...");
        var deployFolder = new File(frontDirectory, "deploy");
        if (deployFolder.exists()) {
            try {
                FileUtils.moveDirectoryToDirectory(deployFolder, outputDirectory, true);
            } catch (IOException e) {
                LOGGER.error("Ocurrió un error moviendo el directorio " +
                        "de despliegues del front a la carpeta de salida", e);
                throw new UncheckedIOException(e);
            }
        }
        return resp;
    }

    private int renameDeployFolder(int resp) {
        LOGGER.debug("Se renombra la carpeta de despliegues de deploy a html/backoffice");
        setValue("Renombrando carpeta de despliegues de deploy a html/backoffice");
        var deployFolder = new File(outputDirectory, "deploy");
        var htmlFolder = new File(outputDirectory, "html");
        if (deployFolder.renameTo(htmlFolder)) {
            File backofficeFolder = new File(htmlFolder, "backoffice");
            if (backofficeFolder.mkdirs()) {
                return resp;
            }
        }
        return ProcessCode.FRONT_GENERATION;
    }

    private CompletableFuture<Integer> unzipDeploy(int resp) {
        assert resp == 0;
        LOGGER.debug("Inicia la descompresión del zip generado por el front.");
        setValue("Descomprimiendo zip generado por el front...");
        return CompletableFuture.supplyAsync(()
                -> new ProcessManager(SH, "-c", "unzip html/sd_front.zip -d html/backoffice/")
                .withDirectory(outputDirectory).startAndWait());
    }

    private int deleteZip(int resp) {
        LOGGER.debug("Se elimina el zip generado por el front anteriormente");
        setValue("Eliminando zip generado por el front...");
        var htmlFolder = new File(outputDirectory, "html");
        var sdFrontZip = new File(htmlFolder, "sd_front.zip");
        if (sdFrontZip.delete()) {
            return resp;
        }
        return ProcessCode.FRONT_GENERATION;
    }

    @SuppressWarnings("SameReturnValue")
    private int handleError(Throwable error) {
        LOGGER.error("Ocurrió un error durante la generación del front", error);
        return ProcessCode.FRONT_GENERATION;
    }
}
