package com.mateolegi.despliegues_audiencias.util;

public class DeployNumbers {

    private static String deploymentVersion;
    private static String audienciasVersion;
    private static String deploymentNumber;
    private static String backofficeVersion;

    public static String getDeploymentVersion() {
        return deploymentVersion;
    }

    public static void setDeploymentVersion(String deploymentVersion) {
        DeployNumbers.deploymentVersion = deploymentVersion;
    }

    public static String getAudienciasVersion() {
        return audienciasVersion;
    }

    public static void setAudienciasVersion(String audienciasVersion) {
        DeployNumbers.audienciasVersion = audienciasVersion;
    }

    public static String getDeploymentNumber() {
        return deploymentNumber;
    }

    public static void setDeploymentNumber(String deploymentNumber) {
        DeployNumbers.deploymentNumber = deploymentNumber;
    }

    public static String getBackofficeVersion() {
        return backofficeVersion;
    }

    public static void setBackofficeVersion(String backofficeVersion) {
        DeployNumbers.backofficeVersion = backofficeVersion;
    }
}
