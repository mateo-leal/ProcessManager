package com.mateolegi.despliegues_audiencias;

import org.junit.jupiter.api.Test;

class MainProcessTest {

    @Test
    void test() {
        MainProcess.getInstance()
                .onProcessFinished(() -> {})
                .onSuccess(() -> {})
                .onError(() -> {})
                .run();
    }

}