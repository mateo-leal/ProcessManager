package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues_audiencias.exception.RestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestManagerTest {

    @BeforeEach
    void setUp() {
        TrustAllHosts.trustAllHosts();
    }

    @Test
    void callGetService() throws RestException {
        var restManager = new RestManager();
        String response = restManager.callGetService("https://www.google.com/");
        assertThat(response, is(notNullValue()));
        assertThat(response.length(), is(greaterThan(0)));
    }

    @Test()
    void callGetService_when_host_does_not_exists() throws RestException {
        var restManager = new RestManager();
        assertThrows(RestException.class, () -> restManager.callGetService("http://notexistinghost.com"));
    }
}