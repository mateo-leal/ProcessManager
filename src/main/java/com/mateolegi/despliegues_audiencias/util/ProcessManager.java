package com.mateolegi.despliegues_audiencias.util;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ProcessManager {

    public static final String SH = "C:\\Program Files\\Git\\bin\\sh.exe";
    public static final SimpleStringProperty STRING_PROPERTY = new SimpleStringProperty();
    private final ProcessBuilder builder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);

    public static void setValue(String value) {
        try {
            Platform.runLater(() -> STRING_PROPERTY.setValue(value));
        } catch (IllegalStateException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public ProcessManager(String... commands) {
        if (commands.length == 0) {
            throw new IllegalArgumentException("Commands are required.");
        }
        builder = new ProcessBuilder(commands)
                .redirectErrorStream(true)
                .inheritIO();
    }

    public ProcessManager withDirectory(File directory) {
        builder.directory(directory);
        return this;
    }

    public int startAndWait() {
        try {
            return builder.start().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
