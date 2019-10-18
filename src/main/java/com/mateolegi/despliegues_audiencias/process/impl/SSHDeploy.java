package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues.process.Event;
import com.mateolegi.despliegues_audiencias.constant.Constants;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.despliegues_audiencias.util.ProcessFactory;
import com.mateolegi.sshconnection.SSHConnectionManager;
import com.mateolegi.util.BidirectionalStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.DEPLOYMENT_ERROR;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getDeploymentVersion;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.SH;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

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
        if (Event.Confirmation.APPROVED != Root.get().emitConfirmation(Constants.Event.DEPLOY_CONFIRM)) {
            return false;
        }
        try (BidirectionalStream stream = new BidirectionalStream()) {
            setValue("Validando que la rama se haya subido...");
            new ProcessFactory(SH, "-c",
                    "git ls-remote --heads https://git.quipux.com/despliegues/backoffice.git "
                            + DeployNumbers.getDeploymentVersion())
                    .withOutput(stream.getOutputStream())
                    .startAndWait();
            var resp = IOUtils.toString(stream.getInputStream(), StandardCharsets.UTF_8);
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
        LOGGER.debug("Desplegando en ambiente de pruebas.");
        setValue("Desplegando en ambiente de pruebas...");
        return CompletableFuture.supplyAsync(() -> {
            try (var ssh = getSSH()) {
                setValue("Desplegando versión en el servidor de pruebas...");
                ssh.runCommand("cd /opt/backoffice/versiones/ && sudo -S -p '' ./update_remote.sh -v "
                        + getDeploymentVersion() + " -u " + CONFIGURATION.getGitUser() + " -p "
                        + CONFIGURATION.getGitPassword());
                Thread.sleep(5000);
            } catch (Exception e) {
                LOGGER.error("Ocurrió un error durante el despliegue de la versión.", e);
                return DEPLOYMENT_ERROR;
            }
            updateTemplates();
            return 0;
        });
    }

    private void updateTemplates() {
        try (var ssh = getSSH()) {
            setValue("Actualizando plantillas...");
            ssh.runCommand("cd /opt/ssdd/plantillas/audiencias/ && git pull origin development");
        } catch (Exception e) {
            LOGGER.error("Error actualizando plantillas.", e);
        }
    }

    @NotNull
    private SSHConnectionManager getSSH() {
        var ssh = new SSHConnectionManager(CONFIGURATION.getSSHUser(), CONFIGURATION.getSSHPassword(),
                CONFIGURATION.getSSHHost(), CONFIGURATION.getSSHPort());
        ssh.open();
        return ssh;
    }
}
