package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.Event;
import com.mateolegi.despliegues_audiencias.process.ProcessSet;
import com.mateolegi.util.EmitterOutputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;

import static com.mateolegi.despliegues_audiencias.constant.Constants.*;
import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.STARTUP_ERROR;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;

public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final Options options = new Options();
    private static Stage STAGE;

    private static void onSuccess(Event event) {
        LOGGER.info("El despligue se realizó correctamente.");
    }

    private static void onError(Event event) {
        LOGGER.error("Error desplegando la versión.");
    }

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
        var scene = new Scene(loadFXML(MAIN_FXML), PREFERED_WIDTH, PREFERED_HEIGHT);
        STAGE.setScene(scene);
        STAGE.show();
    }

    @Override
    public void stop() {
        //Thread.getAllStackTraces().keySet().forEach(Thread::interrupt);
        Platform.exit();
        System.exit(0);
    }

    @Contract(pure = true)
    static Stage getStage() {
        return STAGE;
    }

    static <T> T loadFXML(String fxml) throws IOException {
        var fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
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

    private static void requireDeployNumbers(CommandLine cmd) throws MissingOptionException {
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

    public static void main(String[] args) {
        try {
            var cmd = setOptions(args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Despliegues Audiencias", options);
                System.exit(0);
            }
            if (cmd.hasOption("g")) {
                setSystemOutput();
                launch();
            } else {
                launchCLI(cmd);
            }
        } catch (ParseException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(STARTUP_ERROR);
        }
    }

    private static void launchCLI(@NotNull CommandLine cmd) throws MissingOptionException {
        if (cmd.getOptions().length == 0) {
            var formatter = new HelpFormatter();
            formatter.printHelp("Despliegues Audiencias", options);
            System.exit(0);
        }
        requireDeployNumbers(cmd);
        Root.get()
                .on(Root.PROCESS_FINISHED, event -> {})
                .on(Root.SUCCESS, App::onSuccess)
                .on(Root.ERROR, App::onError)
                .withManager(ProcessSet.getManager())
                .run();
    }

    private static void setSystemOutput() throws IOException {
        var printStream = new PrintStream(new EmitterOutputStream());
        System.setOut(printStream);
    }
}