package com.mateolegi.net;

import java.io.IOException;
import java.net.HttpURLConnection;

public class RestException extends Exception {

    private String message;
    private int code;
    private HttpURLConnection response;

    public RestException(HttpURLConnection response) {
        super();
        this.response = response;
        try {
            this.message = response.getResponseMessage();
            this.code = response.getResponseCode();
        } catch (IOException ignored) { }
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return message;
    }
    public int getCode() {
        return code;
    }
    public HttpURLConnection getResponse() {
        return response;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        return (message != null) ? (s + ": " + code + " - " + message) : s;
    }
}
