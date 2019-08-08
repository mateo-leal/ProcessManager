package com.mateolegi.despliegues_audiencias.util;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ProcessManager {

    public static final String SH = "C:\\Program Files\\Git\\bin\\sh.exe";
    public static final SimpleStringProperty STRING_PROPERTY = new SimpleStringProperty();
    private final ProcessBuilder builder;

    public static void setValue(String value) {
        try {
            Platform.runLater(() -> STRING_PROPERTY.setValue(value));
        } catch (IllegalStateException ignored) {
        }
    }

    public ProcessManager(String... commands) {
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
