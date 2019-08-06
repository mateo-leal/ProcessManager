package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.TestProcess;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

class AudienciasGenerationTest {

    @Test
    void test() throws ExecutionException, InterruptedException {
        TestProcess.test(new AudienciasGeneration());
    }

    @AfterAll
    static void after() {
        var configuration = new Configuration();
        var outputDirectory = new File(configuration.getOutputDirectory());
        var audienciasOutput = new File(outputDirectory, "audiencias");
        if (audienciasOutput.exists()) {
            try {
                FileUtils.deleteDirectory(audienciasOutput);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}