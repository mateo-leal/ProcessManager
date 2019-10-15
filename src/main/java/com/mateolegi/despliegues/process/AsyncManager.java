package com.mateolegi.despliegues.process;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.exception.ProcessException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Clase gestora de promesas, permite concatenear promesas, realizar operaciones entre cada una,
 * ejecutar acciones cuando concluye la serie. Puede procesar promesas individuales o en paralelo.
 * @author <a href="https://mateolegi.github.com">Mateo Leal</a>
 */
public class AsyncManager implements Runnable {

    /** Cola de promesas */
    private Queue<QueueItemProcess> queue = new LinkedList<>();
    /** Estado actual del objeto */
    private volatile boolean isClosed = false;

    /**
     * Proceso que se va a ejecutar después del agregado anteriormente.
     * @param process proceso
     * @return el objeto
     * @throws IllegalStateException si el objeto está cerrado
     */
    public AsyncManager then(@NotNull Supplier<AsyncProcess> process) {
        requireOpen();
        queue.offer(new QueueItemProcess(process.get()));
        return this;
    }

    /**
     * Procesos que se van a ejecutar simultaneamente después del anterior
     * @param a proceso 1
     * @param b proceso 2
     * @return el objeto
     * @throws IllegalStateException si el objeto está cerrado
     */
    public AsyncManager thenParallel(@NotNull Supplier<AsyncProcess> a, @NotNull Supplier<AsyncProcess> b) {
        requireOpen();
        queue.offer(new QueueItemProcess(a.get(), b.get()));
        return this;
    }

    /**
     * Ejecuta una promesa.<br>
     * Valida que el método {@link AsyncProcess#prepare()} retorne {@code true} entonces ejecuta el método
     * {@link AsyncProcess#start()} del proceso, seguido del evento {@link Root#PROCESS_FINISHED}
     * y retorna el valor dado por el método {@link AsyncProcess#validate()}
     * @param process proceso que se va a ejecutar
     * @return promesa
     */
    private CompletableFuture<Boolean> runProcess(@NotNull AsyncProcess process) {
        if (process.prepare()) {
            return process.start()
                    .thenAccept(code -> Root.get().emit(Root.PROCESS_FINISHED,
                            Map.of(Event.SOURCE, AsyncManager.class, Event.CODE, code)))
                    .thenApply(__ -> process.validate());
        }
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Si durante la promesa anterior se arrojó una excepción entonces este detiene la serie y arroja la excepción.<br>
     * Obtiene el siguiente elemento de la cola de promesas si la respuesta del proceso anterior es {@code true},
     * ejecute el o los procesos, si el siguiente es paralelo, y llama el siguiente.<br>
     * Si ya no hay más procesos en la cola y el último retornó {@code true} entonces llama el
     * evento {@link Root#SUCCESS}, de lo contrario, llama el evento {@link Root#ERROR} y cierra el objeto.
     * @param prev resultado del proceso previo
     * @param err error del proceso anterior, si hubo
     */
    @Contract("_, !null -> fail")
    private void next(boolean prev, Throwable err) {
        if (err != null) {
            throw new ProcessException(err);
        }
        if (prev && !queue.isEmpty()) {
            var process = queue.poll();
            var a = process.getA();
            var b = process.getB();
            if (b != null) {
                runProcess(a).thenCombine(runProcess(b), (resA, resB) -> resA && resB)
                      .whenComplete(this::next);
            } else {
                runProcess(a).whenComplete(this::next);
            }
        } else {
            Root.get().emit(prev ? Root.SUCCESS : Root.ERROR, Map.of(Event.SOURCE, AsyncManager.class));
            isClosed = true;
        }
    }

    /** Valida que el objeto esté abierto, si no está arroja una excepción. */
    private void requireOpen() {
        if (isClosed) {
            throw new IllegalStateException("AsyncManager is closed.");
        }
    }

    /**
     * Inicia la serie de promesas.<br>
     * Si ocurre un error dentro del proceso este se arroja y finaliza la serie y el objeto.
     * @throws IllegalStateException si el objeto está cerrado
     */
    @Override
    public void run() {
        requireOpen();
        next(true, null);
    }

    /**
     * Encapsula el proceso individual o ambos si es una acción en paralelo
     */
    static class QueueItemProcess {
        private AsyncProcess a;
        private AsyncProcess b = null;

        @Contract(pure = true)
        QueueItemProcess(AsyncProcess a) {
            this.a = a;
        }
        @Contract(pure = true)
        QueueItemProcess(AsyncProcess a, AsyncProcess b) {
            this.a = a;
            this.b = b;
        }
        AsyncProcess getA() {
            return a;
        }
        AsyncProcess getB() {
            return b;
        }
    }
}
