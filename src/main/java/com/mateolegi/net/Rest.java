package com.mateolegi.net;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Rest {

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        return getClient().sendAsync(getRequest(endpoint), HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient getClient() {
        return HttpClient.newBuilder()
                .sslContext(TrustAllHosts.trustAllHosts())
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    private HttpRequest getRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(endpoint))
                .build();
    }
}
