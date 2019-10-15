package com.mateolegi.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class TrustAllHosts {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrustAllHosts.class);

    public static SSLContext trustAllHosts() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509ExtendedTrustManager() {
                        @Override public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        @Override public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        @Override public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                        @Override public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) { }
                        @Override public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) { }
                        @Override public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) { }
                        @Override public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) { }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            return sc;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
