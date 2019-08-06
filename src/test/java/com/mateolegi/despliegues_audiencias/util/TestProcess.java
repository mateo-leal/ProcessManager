package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues_audiencias.process.RunnableProcess;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestProcess {

    public static void test(RunnableProcess process)
            throws ExecutionException, InterruptedException {
        assertThat(process.prepare(), is(true));
        //assertTrue(process::prepare);
        CompletableFuture<Integer> future = process.start();
        assertThat(future, is(notNullValue()));
        //assertNotNull(future);
        int resp = future.get();
        assertThat(resp, is(equalTo(0)));
        //assertEquals(0, resp);
        assertThat(process.validate(), is(true));
        //assertTrue(process::validate);
    }
}
