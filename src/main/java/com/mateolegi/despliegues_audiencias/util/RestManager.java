package com.mateolegi.despliegues_audiencias.util;

import com.mateolegi.despliegues_audiencias.exception.RestException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestManager {

    public String callGetService(String endpoint) throws RestException {
        try {
            var url = new URL(endpoint);
            var conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RestException(conn.getResponseMessage(), conn.getResponseCode());
            }
            var response = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
            conn.disconnect();
            return response;
        } catch (IOException e) {
            throw new RestException(e.getMessage(), e);
        }
    }
}
