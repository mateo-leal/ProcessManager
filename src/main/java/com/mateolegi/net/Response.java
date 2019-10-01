package com.mateolegi.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Response {

    private final HttpURLConnection connection;
    private int code;
    private String message;
    private String body;
    private HttpMethod method;
    private String contentEncoding;
    private long contentLength;
    private String contentType;
    private Map<String, List<String>> headers;
    private String url;

    public Response(@NotNull HttpURLConnection connection) throws IOException {
        this.connection = connection;
        this.code = connection.getResponseCode();
        this.message = connection.getResponseMessage();
        this.body = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        this.method = HttpMethod.valueOf(connection.getRequestMethod());
        this.contentEncoding = connection.getContentEncoding();
        this.contentLength = connection.getContentLength();
        this.contentType = connection.getContentType();
        this.headers = connection.getHeaderFields();
        this.url = connection.getURL().toString();
    }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public String getBody() {
        return body;
    }
    public <T> T getBody(Class<? extends T> classOfT) {
        return new Gson().fromJson(getBody(), classOfT);
    }
    public <T> List<T> getBodyList(Class<? extends T> classOfT) {
        var listType = new TypeToken<ArrayList<T>>(){}.getType();
        return new Gson().fromJson(getBody(), listType);
    }
    public HttpMethod getMethod() {
        return method;
    }
    public String getContentEncoding() {
        return contentEncoding;
    }
    public long getContentLength() {
        return contentLength;
    }
    public String getContentType() {
        return contentType;
    }
    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(connection.getHeaderField(name));
    }
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    public String getUrl() {
        return url;
    }
}
