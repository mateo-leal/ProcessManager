package com.mateolegi.despliegues_audiencias.util;

import com.jcraft.jsch.*;
import com.mateolegi.despliegues_audiencias.exception.SSHConnectionException;
import com.mateolegi.despliegues_audiencias.exception.SSHNotInitializedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestiona una conexión con un servidor por el protocolo
 * SSH y permite la ejecución de comandos.
 */
public class SSHConnectionManager implements AutoCloseable {

    /** Archivo de configuración */
    private static final Configuration CONFIGURATION = new Configuration();
    private static final Logger LOGGER = LoggerFactory.getLogger(SSHConnectionManager.class);

    private Session session;

    private final String username = CONFIGURATION.getSSHUser();
    private final String password = CONFIGURATION.getSSHPassword();
    private final String hostname = CONFIGURATION.getSSHHost();
    private final int port = CONFIGURATION.getSSHPort();

    public void open() {
        try {
            var jSch = new JSch();
            session = jSch.getSession(username, hostname, port);
            var config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);
            session.setPassword(password);
            LOGGER.info("Conectando SSH a " + hostname + " - Por favor espere unos segundos... ");
            session.connect();
            LOGGER.info("Se ha establecido la conexión.");
        } catch (JSchException e) {
            throw new SSHConnectionException(e);
        }
    }

    public String runCommand(String command) throws JSchException, IOException {
        if (!session.isConnected()) {
            throw new SSHNotInitializedException("No conectado a una sesión abierta.  Llama el método open() primero.");
        }
        var channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        var in = channel.getInputStream();
        channel.connect(60000);
        var ret = getChannelOutput(channel, in);
        channel.disconnect();
        return ret;
    }

    private String getChannelOutput(Channel channel, InputStream in) throws IOException {
        var buffer = new byte[1024];
        var strBuilder = new StringBuilder();
        while (true) {
            while (in.available() > 0) {
                int i = in.read(buffer, 0, 1024);
                if (i < 0) {
                    break;
                }
                var line = new String(buffer, 0, i);
                strBuilder.append(line);
                LOGGER.debug(line);
            }
            if (channel.isClosed()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) { }
        }
        return strBuilder.toString();
    }

    public void close() {
        session.disconnect();
        LOGGER.info("Se ha desconectado de la sesión.");
    }
}