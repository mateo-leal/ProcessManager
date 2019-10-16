package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.RemoteVersion;
import com.mateolegi.sshconnection.SSHConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getAudienciasVersion;
import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.getDeploymentNumber;
import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class StartServices implements AsyncProcess {

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
        return true;
    }

    /**
     * Encapsula en un proceso en un futuro y lo retorna.
     * Este futuro debe retornar un {@code boolean} como respuesta,
     * siendo {@code true} cuando el proceso es exitoso, en otro caso debe
     * retornar {@code false}.
     *
     * @return futuro
     */
    @Override
    public CompletableFuture<Integer> start() {
        return CompletableFuture.supplyAsync(() -> {
            restartMemcached();
            startProcess();
            return 0;
        });
    }

    @Override
    public boolean validate() {
        setValue("Validando que la versión está desplegada...");
        return new RemoteVersion().getRemoteVersion()
                .map(version -> Objects.equals(version.getDespliegue(), Integer.parseInt(getDeploymentNumber()))
                        && Objects.equals(version.getVersion(), getAudienciasVersion()))
                .orElse(false);
    }

    private void startProcess() {
        try (var ssh = getSSH()) {
            setValue("Iniciando los servicios...");
            Executors.newSingleThreadExecutor()
                    .submit(() -> ssh.runCommand("cd /opt/backoffice/bin/ && sudo -S -p '' ./start.sh"))
                    .get(1, TimeUnit.MINUTES);
        } catch (Exception ignored) { }
    }

    private void restartMemcached() {
        try (var ssh = getSSH()) {
            setValue("Reiniciando caché del servidor...");
            ssh.runCommand("sudo service memcached restart");
        } catch (Exception e) {
            LOGGER.error("Error reiniciando Memcached.", e);
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
