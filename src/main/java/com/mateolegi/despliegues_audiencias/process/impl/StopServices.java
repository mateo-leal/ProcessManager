package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.despliegues.process.AsyncProcess;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.sshconnection.SSHConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mateolegi.despliegues_audiencias.util.ProcessFactory.setValue;

public class StopServices implements AsyncProcess {

    private static final Configuration CONFIGURATION = new Configuration();

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
        return null;
    }

    private void stopServices() {
        try (var ssh = getSSH()) {
            setValue("Deteniendo los servicios...");
            Executors.newSingleThreadExecutor()
                    .submit(() -> ssh.runCommand("cd /opt/backoffice/bin/ && sudo -S -p '' ./stop.sh"))
                    .get(1, TimeUnit.MINUTES);
        } catch (Exception ignored) { }
    }

    @NotNull
    private SSHConnectionManager getSSH() {
        var ssh = new SSHConnectionManager(CONFIGURATION.getSSHUser(), CONFIGURATION.getSSHPassword(),
                CONFIGURATION.getSSHHost(), CONFIGURATION.getSSHPort());
        ssh.open();
        return ssh;
    }
}
