package com.mateolegi.despliegues_audiencias.process.impl;

import org.junit.jupiter.api.Test;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.setDeploymentVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SSHDeployTest {

    @Test
    void test_prepare_when_branch_exists() {
        setDeploymentVersion("1.0.0");
        SSHDeploy sshDeploy = new SSHDeploy();
        assertThat(sshDeploy.prepare(), is(true));
    }

    @Test
    void test_prepare_when_branch_not_exists() {
        setDeploymentVersion("not-existing-branch");
        SSHDeploy sshDeploy = new SSHDeploy();
        assertThat(sshDeploy.prepare(), is(false));
    }
}