package com.mateolegi.despliegues_audiencias.constant;

/**
 * Constantes para los códigos de errores de los
 * procesos que se realizan.
 */
public class ProcessCode {

    /** Error ocurrido durante la generación del jar de Audiencias. */
    public static final int AUDIENCIAS_JAR_GENERATION = -1;

    /** Error ocurrido durante la generación de las fuentes de front. */
    public static final int FRONT_GENERATION = -2;

    /** Error ocurrido durante la compresión de los desplegables. */
    public static final int COMPRESSION_GENERATION = -3;

    /** Error ocurrido durante algún proceso de Git */
    public static final int GIT_ERROR = -4;

    /** Error ocurrido consultando el archivo de configuración conf.properties. */
    public static final int CONF_FILE_NOT_FOUND = -5;

    /** Error ocurrido durante el despliegue de la versión en el ambiente */
    public static final int DEPLOYMENT_ERROR = -6;

    public static final int STARTUP_ERROR = -7;
}
