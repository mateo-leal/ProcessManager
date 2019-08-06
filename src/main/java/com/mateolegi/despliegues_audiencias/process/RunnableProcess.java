package com.mateolegi.despliegues_audiencias.process;

import java.util.concurrent.CompletableFuture;

public interface RunnableProcess {

    boolean prepare();

    CompletableFuture<Integer> start();

    boolean validate();
}
