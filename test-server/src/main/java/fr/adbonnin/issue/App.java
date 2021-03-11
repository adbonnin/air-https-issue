package fr.adbonnin.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import fr.adbonnin.issue.dto.ConfigureDTO;
import fr.adbonnin.issue.dto.ParamsDTO;
import fr.adbonnin.issue.utils.KeyStoreUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static fr.adbonnin.issue.utils.ArrayUtil.nullToEmpty;

public class App {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private final KeyStore ks = buildKeyStore();

    private HttpServer testServer = null;

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    public void start() throws IOException {
        final HttpServer configServer = buildConfigServer();
        configServer.start();
    }

    private void handleParams(HttpExchange exchange) throws IOException {
        try {
            final SSLContext sslContext = buildSSLContext(ks);

            final ParamsDTO params = new ParamsDTO();
            params.setCipherSuites(sslContext.getSupportedSSLParameters().getCipherSuites());
            params.setProtocols(sslContext.getSupportedSSLParameters().getProtocols());

            final String response = MAPPER.writeValueAsString(params);

            exchange.sendResponseHeaders(200, response.length());
            final OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void handleConfigure(HttpExchange exchange) throws IOException {
        final String response = "";

        final ConfigureDTO configure;
        try (InputStream input = exchange.getRequestBody()) {
            configure = MAPPER.readValue(input, ConfigureDTO.class);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (testServer != null) {
            testServer.stop(0);
        }

        try {
            testServer = buildTestServer(configure);
            testServer.start();
        }
        catch (Exception e) {
            testServer = null;
            throw new IOException(e);
        }

        exchange.sendResponseHeaders(200, response.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handleTest(HttpExchange exchange) throws IOException {
        final String response = "test";

        exchange.sendResponseHeaders(200, response.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public HttpServer buildConfigServer() throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/params", this::handleParams);
        server.createContext("/configure", this::handleConfigure);
        return server;
    }

    private HttpServer buildTestServer(ConfigureDTO configure) throws IOException, GeneralSecurityException {
        final HttpsServer server = HttpsServer.create(new InetSocketAddress(443), 0);
        server.setHttpsConfigurator(new CustomHttpsConfigurator(buildSSLContext(ks), configure.getProtocols(), configure.getCipherSuites()));
        server.createContext("/test", this::handleTest);
        return server;
    }

    private static KeyStore buildKeyStore() {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null);
            KeyStoreUtil.updateWithSelfSignedServerCertificate(keyStore);
            return keyStore;
        }
        catch (IOException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static SSLContext buildSSLContext(KeyStore ks) throws GeneralSecurityException {
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    private static class CustomHttpsConfigurator extends HttpsConfigurator {

        private final String[] protocols;

        private final String[] cipherSuites;

        public CustomHttpsConfigurator(SSLContext context, String[] protocols, String[] cipherSuites) {
            super(context);
            this.protocols = nullToEmpty(protocols);
            this.cipherSuites = nullToEmpty(cipherSuites);
        }

        @Override
        public void configure(HttpsParameters params) {
            final SSLContext context = getSSLContext();
            context.createSSLEngine();
            final SSLParameters defaultSSLParameters = context.getDefaultSSLParameters();

            final boolean clientAuth = true;
            params.setWantClientAuth(clientAuth);
            defaultSSLParameters.setWantClientAuth(clientAuth);

            params.setProtocols(protocols);
            defaultSSLParameters.setProtocols(protocols);

            params.setCipherSuites(cipherSuites);
            defaultSSLParameters.setCipherSuites(cipherSuites);

            params.setSSLParameters(defaultSSLParameters);
        }
    }
}
