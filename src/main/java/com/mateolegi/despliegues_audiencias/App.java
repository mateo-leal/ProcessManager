package com.mateolegi.despliegues_audiencias;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.mateolegi.despliegues_audiencias.constant.Constants.HOME_FXML;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Generador de despliegues | Audiencias");
        var scene = new Scene(loadFXML(), 376, 270);
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML() throws IOException {
        var fxmlLoader = new FXMLLoader(App.class.getResource(HOME_FXML));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}