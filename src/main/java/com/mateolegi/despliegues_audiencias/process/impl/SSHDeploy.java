package com.mateolegi.despliegues_audiencias.process.impl;

import com.jcraft.jsch.JSchException;
import com.mateolegi.despliegues_audiencias.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getDeploymentVersion;
import static com.mateolegi.despliegues_audiencias.util.ProcessManager.SH;

public class SSHDeploy implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHDeploy.class);
    private static final Configuration CONFIGURATION = new Configuration();

    /**
     * Prepara los archivos y realiza las respectivas validaciones
     * antes de realizar un proceso.
     *
     * @return resultado de la preparación, si es falso no se podría
     * ejecutar el proceso.
     */
    @Override
    public boolean prepare() {
        try (BidirectionalStream stream = new BidirectionalStream()) {
            new ProcessManager(SH, "-c",
                    "git ls-remote --heads https://git.quipux.com/despliegues/backoffice.git "
                            + DeployNumbers.getDeploymentVersion())
                    .withOutput(stream.getOutputStream())
                    .startAndWait();
            String resp = IOUtils.toString(stream.getInputStream(), StandardCharsets.UTF_8);
            return resp.length() > 0;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Encapsula en un futuro un proceso y lo retorna.
     * Este futuro debe retornar un entero como respuesta,
     * siendo 0 cuando el proceso es exitoso, en otro caso debe
     * retornar un error que sea especificado en la
     * clase {@link com.mateolegi.despliegues_audiencias.constant.ProcessCode}
     *
     * @return futuro
     */
    @Override
    public CompletableFuture<Integer> start() {
        return CompletableFuture.supplyAsync(() -> {
            try (var ssh = getSSH()) {
                ssh.runCommand("cd /opt/backoffice/versiones/ && sudo -S -p '' ./update_remote.sh -v "
                        + getDeploymentVersion() + " -u " + CONFIGURATION.getGitUser() + " -p "
                        + CONFIGURATION.getGitPassword());
                Thread.sleep(3000);
                ssh.runCommand("cd /opt/backoffice/bin/ && sudo -S -p '' ./restart.sh");
                Thread.sleep(10000);

            } catch (JSchException | InterruptedException | IOException e) {
                LOGGER.error("Ocurrió un error durante el despliegue de la versión", e);
            }
            return 0;
        });
    }

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     *
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    @Override
    public boolean validate() {
        return false;
    }

    private SSHConnectionManager getSSH() {
        var ssh = new SSHConnectionManager();
        ssh.open();
        return ssh;
    }
}
