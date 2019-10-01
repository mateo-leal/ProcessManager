package com.mateolegi.net;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestTest {

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
                .thenAccept(System.out::println)
                .join();
    }

    @Test
    void callGetService() throws RestException {
        var restManager = new Rest();
        var response = restManager.get("https://www.google.com/");
        assertThat(response, is(notNullValue()));
        System.out.println(response.getBody());
        assertThat(response.getContentLength(), is(greaterThan(0L)));
    }

    @Test()
    void callGetService_when_host_does_not_exists() {
        var restManager = new Rest();
        assertThrows(RestException.class, () -> restManager.get("http://notexistinghost.com"));
    }
}