package com.fastfur.messaging.producer.stocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VantageConnector {

    private static final String API_KEY = "&apikey=";
    private static final String API_VALUE = "QZZNHVR9BNNVZW01";

    VantageConnector() {
        CertificateInstaller.installVantageCertificate();
    }

    public String getData(String baseUri) throws IOException {
        String jsonResponse = null;

        URL request;
        URLConnection connection;

        request = new URL(baseUri + API_KEY + API_VALUE);
        connection = request.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(3000);

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder responseBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
            System.out.println("--------------------------------------");
            jsonResponse = responseBuilder.toString();
            System.out.println(jsonResponse);
            System.out.println("--------------------------------------");
        }
        return jsonResponse;
    }
}
