package com.mateolegi.despliegues_audiencias.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class DeployNumbersTest {

    private static final String TEST = "Test";

    @Test
    void test() {
        DeployNumbers.setAudienciasVersion(TEST);
        assertThat(DeployNumbers.getAudienciasVersion(), is(equalTo(TEST)));
        DeployNumbers.setBackofficeVersion(TEST);
        assertThat(DeployNumbers.getBackofficeVersion(), is(equalTo(TEST)));
        DeployNumbers.setDeploymentNumber(TEST);
        assertThat(DeployNumbers.getDeploymentNumber(), is(equalTo(TEST)));
        DeployNumbers.setDeploymentVersion(TEST);
        assertThat(DeployNumbers.getDeploymentVersion(), is(equalTo(TEST)));
    }
}