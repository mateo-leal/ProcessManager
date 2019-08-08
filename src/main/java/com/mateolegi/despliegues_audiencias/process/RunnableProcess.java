package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues_audiencias.constant.ProcessCode;

import java.util.concurrent.CompletableFuture;

/**
 * Representa un proceso que se puede ejecutar en segundo plano y
 * al final retorna una respuesta en un número entero.
 */
public interface RunnableProcess {

    /**
     * Prepara los archivos y realiza las respectivas validaciones
     * antes de realizar un proceso.
     *
     * @return resultado de la preparación, si es falso no se podría
     * ejecutar el proceso.
     */
    default boolean prepare() {
        return true;
    }

    /**
     * Encapsula en un futuro un proceso y lo retorna.
     * Este futuro debe retornar un entero como respuesta,
     * siendo 0 cuando el proceso es exitoso, en otro caso debe
     * retornar un error que sea especificado en la
     * clase {@link ProcessCode}
     * @return futuro
     */
    CompletableFuture<Integer> start();

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     *
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    default boolean validate() {
        return true;
    }
}
