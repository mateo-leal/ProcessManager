package com.mateolegi.despliegues_audiencias.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class VersionGetter {

    private static final Configuration CONFIGURATION = new Configuration();
    private Properties confProperties = new Properties();

    public VersionGetter() {
        try {
            var audienciasPath = new File(CONFIGURATION.getDirectoryWorkspace(), "audiencias");
            var confFile = new File(audienciasPath, "audiencias/src/main/java/conf.properties");
            if (confFile.exists()) {
                confProperties = new Properties();
                confProperties.load(new FileInputStream(confFile));
            }
        } catch (Exception ignored) { }
    }

    public String getAudienciasVersion() {
        return confProperties.getProperty("audiencias.version", "");
    }

    public String getAudienciasDeploy() {
        return confProperties.getProperty("audiencias.despliegue", "");
    }
}
