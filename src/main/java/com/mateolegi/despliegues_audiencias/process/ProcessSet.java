package com.mateolegi.despliegues_audiencias.process;

import com.mateolegi.despliegues.process.AsyncManager;
import com.mateolegi.despliegues_audiencias.process.impl.*;

public class ProcessSet {

    public static AsyncManager getManager() {
        return new AsyncManager()
                .then(CloneBaseProcess::new)
                .thenParallel(AudienciasGeneration::new, FrontGeneration::new)
                //.then(CompressionProcessCommand::new)
                .then(CompressionProcessNative::new)
                .then(GitUploadProcessCommand::new)
                //.then(GitUploadProcessNative::new)
                .then(SSHDeploy::new)
                .then(StartServices::new)
        ;
    }
}
