package com.mateolegi.despliegues_audiencias.util;

import com.jcraft.jsch.JSchException;
import com.mateolegi.sshconnection.exception.SSHException;
import com.mateolegi.sshconnection.SSHConnectionManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class SSHConnectionManagerTest {

    private static final Configuration CONFIGURATION = new Configuration();

    @Test
    void open() {
        try (var ssh = new SSHConnectionManager(CONFIGURATION.getSSHUser(), CONFIGURATION.getSSHPassword(),
                CONFIGURATION.getSSHHost(), CONFIGURATION.getSSHPort())) {
            ssh.open();
        } catch (SSHException e) {
            fail(e);
        }
    }

    @Test
    void runCommand() {
        try (var ssh = new SSHConnectionManager(CONFIGURATION.getSSHUser(), CONFIGURATION.getSSHPassword(),
                CONFIGURATION.getSSHHost(), CONFIGURATION.getSSHPort())) {
            ssh.open();
            var resp = ssh.runCommand("uname -a");
            assertNotNull(resp);
            assertThat(resp, containsString("Linux"));
        } catch (JSchException | IOException e) {
            fail(e);
        }
    }
}