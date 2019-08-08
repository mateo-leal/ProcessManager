package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues_audiencias.constant.ProcessCode;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.mateolegi.despliegues_audiencias.constant.Constants.CONF_PROPERTIES;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class Configuration {

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public Configuration() {
        try {
            loadPropertiesFile();
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

    private void loadPropertiesFile() throws IOException {
        InputStream inputStream = new FileInputStream(CONF_PROPERTIES);
        PROPERTIES.load(inputStream);
    }
}
