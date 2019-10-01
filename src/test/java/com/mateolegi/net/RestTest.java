package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.net.RestException;
import com.mateolegi.git.TrustAllHosts;
import com.mateolegi.net.Rest;
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
        var restManager = new Rest();
        var response = restManager.get("https://www.google.com/");
        assertThat(response, is(notNullValue()));
        assertThat(response.getContentLength(), is(greaterThan(0L)));
    }

    @Test()
    void callGetService_when_host_does_not_exists() {
        var restManager = new Rest();
        assertThrows(RestException.class, () -> restManager.get("http://notexistinghost.com"));
    }
}