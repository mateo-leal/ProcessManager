package com.mateolegi.net;

import com.google.gson.Gson;
import com.mateolegi.despliegues_audiencias.util.Configuration;
import com.mateolegi.despliegues_audiencias.util.VersionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestTest {

    @BeforeAll
    static void before() {
        TrustAllHosts.trustAllHosts();
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    void test() {
        var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2).build();
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://git.quipux.com/api/v4/projects/168/repository/branches"))
                .header("Private-Token", "zsvNypp4yB4xdTJwNFpc")
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::headers)
                .thenApply(HttpHeaders::map)
                .thenAccept(System.out::println)
                .join();
    }

    @Test
    void getVersion() {
        var configuration = new Configuration();
        new Rest().get(configuration.getWebVersionService())
                .thenApply(HttpResponse::body)
                .thenApply(s -> new Gson().fromJson(s, VersionResponse.class))
                .thenApply(VersionResponse::getFullVersion)
                .thenAccept(Assertions::assertNotNull)
                .join();
    }

    @Test
    void callGetService() {
        var restManager = new Rest();
        var response = restManager.get("https://www.google.com/");
        assertThat(response, is(notNullValue()));
//        System.out.println(response.getBody());
//        assertThat(response.getContentLength(), is(greaterThan(0L)));
    }

    @Test()
    void callGetService_when_host_does_not_exists() {
        var restManager = new Rest();
        assertThrows(RestException.class, () -> restManager.get("http://notexistinghost.com"));
    }
}