package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues_audiencias.constant.Constants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Objects;

public class ProcessFactory {

    public static final String SH = "C:\\Program Files\\Git\\bin\\sh.exe";
    public static final SimpleStringProperty STRING_PROPERTY = new SimpleStringProperty();
    private final ProcessBuilder builder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFactory.class);
    private OutputStream outputStream = null;

    public static void setValue(String value) {
        try {
            Platform.runLater(() -> STRING_PROPERTY.setValue(value));
            Root.get().emit(Constants.Event.LOG_OUTPUT, Map.of("content", value + "\n"));
        } catch (IllegalStateException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public ProcessFactory(String... commands) {
        if (commands.length == 0) {
            throw new IllegalArgumentException("Commands are required.");
        }
        builder = new ProcessBuilder(commands);
    }

    public ProcessFactory withDirectory(File directory) {
        builder.directory(directory);
        return this;
    }

    public ProcessFactory withOutput(OutputStream out) {
        this.outputStream = out;
        return this;
    }

    public int startAndWait() {
        try {
            if (Objects.isNull(outputStream)) {
                builder.inheritIO();
            }
            Process p = builder.start();
            if (Objects.nonNull(outputStream)) {
                syncOutputs(p.getInputStream());
            }
            return p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void syncOutputs(InputStream processOutput) {
        new Thread(() -> {
            try {
                var buffer = new byte[8192];
                int len;
                while ((len = processOutput.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                LOGGER.error("Error sincronizando salidas.", e);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Error cerrando outputstream.", e);
                }
            }
        }).start();
    }
}
