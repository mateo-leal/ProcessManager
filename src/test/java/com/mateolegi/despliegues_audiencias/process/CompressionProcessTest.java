package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues_audiencias.util.TestProcess;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class CompressionProcessTest {

    @Test
    void test() throws ExecutionException, InterruptedException {
        TestProcess.test(new CompressionProcess());
    }
}