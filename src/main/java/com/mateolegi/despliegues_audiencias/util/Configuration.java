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

    public String getDirectoryWorkspace() {
        return PROPERTIES.getProperty("dir.workspace");
    }

    public String getUserProfile() {
        return System.getenv("USERPROFILE");
    }

    public String getOutputDirectory() {
        return PROPERTIES.getProperty("output.directory");
    }

    public String getFrontDirectory() {
        return PROPERTIES.getProperty("front.path");
    }

    public String getSSHUser() {
        return PROPERTIES.getProperty("ssh.user");
    }

    public String getSSHPassword() {
        return PROPERTIES.getProperty("ssh.password");
    }

    public String getSSHHost() {
        return PROPERTIES.getProperty("ssh.host");
    }

    public int getSSHPort() {
        return Integer.parseInt(PROPERTIES.getProperty("ssh.port"));
    }

    public String getGitUser() {
        return PROPERTIES.getProperty("git.user");
    }

    public String getGitPassword() {
        return PROPERTIES.getProperty("git.password");
    }

    public String getGitRemote() {
        return PROPERTIES.getProperty("git.remote");
    }

    public String getWebVersionService() {
        return PROPERTIES.getProperty("web.version-service");
    }

    public String getWebBackofficeStatus() {
        return PROPERTIES.getProperty("web.backoffice.status");
    }

    public Boolean shouldUploadGit() {
        var resp = PROPERTIES.getProperty("always.git");
        if (resp != null) {
            return Boolean.parseBoolean(resp);
        }
        return null;
    }

    public Boolean shouldDeploy() {
        var resp = PROPERTIES.getProperty("always.deploy");
        if (resp != null) {
            return Boolean.parseBoolean(resp);
        }
        return null;
    }

    private void loadPropertiesFile() throws IOException {
        InputStream inputStream = new FileInputStream(CONF_PROPERTIES);
        PROPERTIES.load(inputStream);
    }
}
