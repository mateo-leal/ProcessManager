package com.mateolegi.despliegues_audiencias.process.impl;

import com.google.gson.Gson;
import com.mateolegi.despliegues.Root;
import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.constant.Constants;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.DeployNumbers;
import com.mateolegi.despliegues_audiencias.util.ProcessFactory;
import com.mateolegi.despliegues_audiencias.util.VersionResponse;
import com.mateolegi.net.Rest;
import com.mateolegi.sshconnection.SSHConnectionManager;
import com.mateolegi.util.BidirectionalStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mateolegi.despliegues_audiencias.constant.ProcessCode.DEPLOYMENT_ERROR;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.SH;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class SSHDeploy implements AsyncProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHDeploy.class);
    private static final Configuration CONFIGURATION = new Configuration();
    private boolean retry = false;

    /**
     * Prepara los archivos y realiza las respectivas validaciones
     * antes de realizar un proceso.
     *
     * @return resultado de la preparación, si es falso no se podría
     * ejecutar el proceso.
     */
    @Override
    public boolean prepare() {
        if (Root.get().emitConfirmation(Constants.Event.DEPLOY_CONFIRM)) {
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
            restartMemcached();
            try (var ssh = getSSH()) {
                setValue("Reinciando servicios...");
                ssh.runCommand("cd /opt/backoffice/bin/ && sudo -S -p '' ./restart.sh");
                Thread.sleep(10000);
                return 0;
            } catch (Exception e) {
                LOGGER.error("Ocurrió un error durante el despliegue de la versión.", e);
                return DEPLOYMENT_ERROR;
            }
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

    private void restartMemcached() {
        try (var ssh = getSSH()) {
            setValue("Reiniciando caché del servidor...");
            ssh.runCommand("sudo service memcached restart");
        } catch (Exception e) {
            LOGGER.error("Error reiniciando Memcached.", e);
        }
    }

    /**
     * Valida que el proceso se haya realizado de manera existosa.
     *
     * @return {@code true} si el proceso concluyó exitosamente,
     * de otra manera {@code false}.
     */
    @Override
    public boolean validate() {
        var rest = new Rest();
        try {
            setValue("Validando que la versión está desplegada...");
            var version = rest.get(CONFIGURATION.getWebVersionService())
                    .thenApply(HttpResponse::body)
                    .thenApply(s -> new Gson().fromJson(s, VersionResponse.class))
                    .join();
            return Objects.equals(version.getDespliegue(), Integer.parseInt(getDeploymentNumber()))
                    && Objects.equals(version.getVersion(), getAudienciasVersion());
        } catch (Exception e) {
            return startProcess();
        }
    }

    private boolean startProcess() {
        if (!retry) {
            retry = true;
            try (var ssh = getSSH()) {
                setValue("Volviendo a iniciar los servicios...");
                Executors.newSingleThreadExecutor()
                        .submit(() -> ssh.runCommand("cd /opt/backoffice/bin/ && sudo -S -p '' ./start.sh"))
                        .get(1, TimeUnit.MINUTES);
            } catch (Exception ignored) { }
        }
        return validate();
    }

    @NotNull
    private SSHConnectionManager getSSH() {
        var ssh = new SSHConnectionManager(CONFIGURATION.getSSHUser(), CONFIGURATION.getSSHPassword(),
                CONFIGURATION.getSSHHost(), CONFIGURATION.getSSHPort());
        ssh.open();
        return ssh;
    }
}
