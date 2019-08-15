package com.mateolegi.despliegues_audiencias.util;

import com.jcraft.jsch.JSchException;
import com.mateolegi.despliegues_audiencias.exception.SSHException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class SSHConnectionManagerTest {

    @Test
    void open() {
        try (var ssh = new SSHConnectionManager()) {
            ssh.open();
        } catch (SSHException e) {
            fail(e);
        }
    }

    @Test
    void runCommand() {
        try (var ssh = new SSHConnectionManager()) {
            ssh.open();
            var resp = ssh.runCommand("uname -a");
            assertNotNull(resp);
            assertThat(resp, containsString("Linux"));
        } catch (JSchException | IOException e) {
            fail(e);
        }
    }
}