package com.fastfur.messaging.producer.stocks;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateInstaller {

    static void installVantageCertificate() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"),
                    "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath),
                    "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            try (InputStream caInput = new BufferedInputStream(
                    // this files is shipped with the application
                    CertificateInstaller.class.getResourceAsStream("/LetsEncryptAuthorityX3.cer"))) {
                Certificate crt = cf.generateCertificate(caInput);
                System.out.println("Added Cert for " + ((X509Certificate) crt)
                        .getSubjectDN());

                keyStore.setCertificateEntry("DSTRootCAX3", crt);
            }

            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {
        // signed by default trusted CAs.
        // connectToVantage();
        testUrl(new URL("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=QZZNHVR9BNNVZW01\n"));
        testUrl(new URL("https://www.thawte.com"));

        // signed by letsencrypt
        testUrl(new URL("https://helloworld.letsencrypt.org"));
        // signed by LE's cross-sign CA
        testUrl(new URL("https://letsencrypt.org"));
        // expired
        testUrl(new URL("https://tv.eurosport.com/"));
        // self-signed
        testUrl(new URL("https://www.pcwebshop.co.uk/"));

        testUrl(new URL("https://www.pcwebshop.co.uk/"));

    }

    static void testUrl(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        try {
            connection.connect();
            System.out.println("Headers of " + url + " => "
                    + connection.getHeaderFields());
        } catch (SSLHandshakeException e) {
            System.out.println("Untrusted: " + url);
        }
    }


}
