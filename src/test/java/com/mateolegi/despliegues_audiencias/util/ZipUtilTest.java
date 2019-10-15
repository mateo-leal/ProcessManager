package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues_audiencias.process.impl.AudienciasGeneration;
import com.mateolegi.despliegues_audiencias.process.impl.FrontGeneration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

class ZipUtilTest {

    private static final Configuration CONFIGURATION = new Configuration();

    // @BeforeEach
    void setUp() {
        var audienciasGeneration = new AudienciasGeneration();
        audienciasGeneration.prepare();
        audienciasGeneration.start().join();
        var frontGeneration = new FrontGeneration();
        frontGeneration.prepare();
        frontGeneration.start().join();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void zipDirs() {
        var audiencias = new File(CONFIGURATION.getOutputDirectory(), "audiencias");
        var html = new File(CONFIGURATION.getOutputDirectory(), "html");
//        var stream = ZipFile.zip(Arrays.asList(audiencias, html),
//                new File(CONFIGURATION.getOutputDirectory(), "test.zip").getAbsolutePath());
//        Scanner scanner = new Scanner(stream);
//        while (scanner.hasNext()) {
//            System.out.println(scanner.next());
//        }
    }
}