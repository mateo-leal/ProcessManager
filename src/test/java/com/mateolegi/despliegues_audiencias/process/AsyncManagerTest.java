package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues.process.AsyncManager;
import com.mateolegi.despliegues.process.exception.ProcessException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class AsyncManagerTest {

    @Test
    void when_is_okay() {
        StringBuilder builder = new StringBuilder();
        new AsyncManager()
                .then(() -> () -> CompletableFuture.supplyAsync(() -> {
                    builder.append("Esto ");
                    return 0;
                }))
                .thenParallel(() -> () -> CompletableFuture.supplyAsync(() -> {
                    builder.append("es ");
                    return 0;
                }), () -> () -> CompletableFuture.supplyAsync(() -> {
                    builder.append("una prueba");
                    return 0;
                }))
                //.onProcessFinished(() -> System.out.println("Parcial: " + builder.toString()))
//                .onSuccess(aBoolean -> {
//                    assertTrue(aBoolean);
//                    assertEquals("Esto es una prueba", builder.toString());
//                })
                .run();
    }

    @Test
    void when_a_process_fails() {
        new AsyncManager()
                .then(() -> () -> CompletableFuture.failedFuture(new Exception()))
                .then(() -> () -> CompletableFuture.completedFuture(0))
                .run();
        assertThrows(ProcessException.class, () -> {});
    }

    @Test
    void onSuccess() {
    }
}