package com.mateolegi.despliegues_audiencias;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

import static javafx.scene.control.Alert.AlertType.ERROR;

public class Configuration {

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public Configuration() {
        try {
            InputStream stream = new FileInputStream("conf.properties");
            PROPERTIES.load(stream);
        } catch (IOException e) {
            LOGGER.error("Error abriendo el archivo conf.properties", e);
            Platform.runLater(() -> {
                Alert alert = new Alert(ERROR, "No se encontró el archivo conf.properties");
                alert.showAndWait().ifPresent(buttonType -> {
                    Platform.exit();
                    System.exit(ProcessCode.CONF_FILE_NOT_FOUND);
                });
            });
        }
    }

    public String getOutputDirectory() {
        return PROPERTIES.getProperty("output.directory");
    }

    public String getFrontDirectory() {
        return PROPERTIES.getProperty("front.path");
    }
}
