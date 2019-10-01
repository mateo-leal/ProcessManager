package com.mateolegi.net;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Rest {

    public Rest() {
        TrustAllHosts.trustAllHosts();
    }

    public Response get(String endpoint) throws RestException {
        return executeGetRequest(endpoint);
    }

    @NotNull
    @Contract("_ -> new")
    private Response executeGetRequest(String endpoint) throws RestException {
        try {
            var url = new URL(endpoint);
            var conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RestException(conn);
            }
            var response = new Response(conn);
            conn.disconnect();
            return response;
        } catch (IOException e) {
            throw new RestException(e.getMessage(), e);
        }
    }
}
