package com.mateolegi.despliegues_audiencias;

import com.google.gson.Gson;
import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.VersionResponse;
import com.mateolegi.net.Rest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.mateolegi.despliegues_audiencias.constant.Constants.Event.RELOAD_STATUS;

public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private static final Configuration CONFIGURATION = new Configuration();
    private static Map<String, Pane> tabs = new HashMap<>();

    @FXML private RadioButton rbtnAW;
    @FXML private RadioButton rbtnBO;
    @FXML private Label lblActualVersion;
    @FXML private Pane content;

    @FXML
    public void initialize() {
        reloadStatus();
        Root.get().on(RELOAD_STATUS, event -> {
            reloadStatus();
        });
    }

    public void setView(@NotNull ActionEvent event) throws IOException {
        var node = (Node) event.getSource() ;
        var fxml = (String) node.getUserData();
        var newContent = tabs.get(fxml);
        if (newContent == null) {
            newContent = App.loadFXML(fxml);
            tabs.put(fxml, newContent);
        }
        var children = content.getChildren();
        if (children.size() > 0) {
            children.set(0, newContent);
        } else {
            children.add(newContent);
        }
    }

    private void reloadStatus() {
        loadBackofficeStatus();
        loadActualVersion();
    }

    private void loadBackofficeStatus() {
        new Thread(() -> new Rest().get(CONFIGURATION.getWebBackofficeStatus())
                .thenApply(HttpResponse::body)
                .thenAccept(s -> rbtnBO.setSelected(s.equals("OK")))
                .join())
                .start();
    }

    private void loadActualVersion() {
        new Thread(() -> new Rest().get(CONFIGURATION.getWebVersionService())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        setActualVersion("No disponible", false);
                        throw new RuntimeException("No disponible");
                    }
                    return response;
                })
                .thenApply(HttpResponse::body)
                .thenApply(s -> new Gson().fromJson(s, VersionResponse.class))
                .thenApply(VersionResponse::getFullVersion)
                .thenAccept(s -> setActualVersion(s, true))
                .join()).start();
    }

    private void setActualVersion(String actualVersion, boolean isOn) {
        Platform.runLater(() -> {
            lblActualVersion.setText(actualVersion);
            rbtnAW.setSelected(isOn);
        });
    }
}
