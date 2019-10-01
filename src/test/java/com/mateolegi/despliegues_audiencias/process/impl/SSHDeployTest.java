package com.mateolegi.despliegues_audiencias.process.impl;

import com.mateolegi.net.TrustAllHosts;
import org.junit.jupiter.api.Test;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
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

    @Test
    void validate() {
        setAudienciasVersion("3.5.0");
        setDeploymentNumber("16");
        SSHDeploy sshDeploy = new SSHDeploy();
        TrustAllHosts.trustAllHosts();
        assertThat(sshDeploy.validate(), is(true));
    }
}