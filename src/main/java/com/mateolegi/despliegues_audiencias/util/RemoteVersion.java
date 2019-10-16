package com.mateolegi.despliegues_audiencias.util;

import com.google.gson.Gson;
import com.mateolegi.net.Rest;

import java.net.http.HttpResponse;
import java.util.Optional;

public class RemoteVersion {

    private static final Configuration CONFIGURATION = new Configuration();

    public Optional<VersionResponse> getRemoteVersion() {
        var rest = new Rest();
        try {
            return Optional.ofNullable(rest.get(CONFIGURATION.getWebVersionService())
                    .thenApply(HttpResponse::body)
                    .thenApply(s -> new Gson().fromJson(s, VersionResponse.class))
                    .join());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
