package com.mateolegi.despliegues_audiencias;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.mateolegi.despliegues_audiencias.constant.Constants.HOME_FXML;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;

public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final Options options = new Options();
    private static Stage STAGE;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws IOException if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        STAGE = primaryStage;
        STAGE.setTitle("Generador de despliegues | Audiencias");
        var scene = new Scene(loadFXML(), 376, 270);
        STAGE.setScene(scene);
        STAGE.show();
    }

    @Contract(pure = true)
    static Stage getStage() {
        return STAGE;
    }

    private static Parent loadFXML() throws IOException {
        var fxmlLoader = new FXMLLoader(App.class.getResource(HOME_FXML));
        return fxmlLoader.load();
    }

    private static CommandLine setOptions(String[] args) throws ParseException {
        options.addOption("g", "gui", false, "Muestra la interfaz gráfica.");
        options.addOption("dv", "deploymentVersion",  true, "Versión de despliegue.");
        options.addOption("av", "audienciasVersion", true, "Versión de Audiencias.");
        options.addOption("dn", "deploymentNumber", true, "Número de despliegue de Audiencias.");
        options.addOption("bv", "backofficeVersion", true, "Versión de Backoffice.");
        options.addOption("h", "help", false, "Muestra la ayuda de cómo usar las banderas.");
        return new DefaultParser().parse(options, args);
    }

    private static void setDeployNumbers(CommandLine cmd) throws MissingOptionException {
        if (!cmd.hasOption("dv")) {
            throw new MissingOptionException("La opción --deploymentVersion es obligatoria para el CLI");
        }
        if (!cmd.hasOption("av")) {
            throw new MissingOptionException("La opción --audienciasVersion es obligatoria para el CLI");
        }
        if (!cmd.hasOption("dn")) {
            throw new MissingOptionException("La opción --deploymentNumber es obligatoria para el CLI");
        }
        setDeploymentVersion(cmd.getOptionValue("dv"));
        setAudienciasVersion(cmd.getOptionValue("av"));
        setDeploymentNumber(cmd.getOptionValue("dn"));
        setBackofficeVersion(cmd.getOptionValue("bv", "no aplica"));
    }

    public static void main(String[] args) throws ParseException {
        CommandLine cmd = setOptions(args);
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Despliegues Audiencias", options);
            System.exit(0);
        }
        if (cmd.hasOption("g")) {
            launch();
        } else {
            if (cmd.getOptions().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Despliegues Audiencias", options);
                System.exit(0);
            }
            setDeployNumbers(cmd);
            new MainProcess().onProcessFinished(() -> {})
                    .onSuccess(() -> LOGGER.info("El despligue se realizó correctamente."))
                    .onError(() -> LOGGER.error("Error desplegando la versión."))
                    .run();
        }
    }

}