package ws.http;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HTTPSTrustManager implements X509TrustManager {

    private static TrustManager[] trustManagers;
    private static final X509Certificate[] acceptedIssuers = new X509Certificate[]{};

    @Override
    public void checkClientTrusted(
            java.security.cert.X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void checkServerTrusted(
            java.security.cert.X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

    /**
     * Trust all certificates
     *
     * @param sslProtocol default is TLS
     * @return
     * @see https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext
     */
    public static SSLContext getTrustAllSSLContext(String sslProtocol) {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

        });

        if (sslProtocol == null || sslProtocol.equals("")) {
            sslProtocol = "TLS";
        }

        SSLContext context = null;
        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new HTTPSTrustManager()};
        }

        try {
            context = SSLContext.getInstance(sslProtocol);
            context.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return context;
    }

    /**
     * Trust the specified certificate
     *
     * @param certificateFile
     * @param sslProtocol
     * @return
     */
    public static SSLContext getTrustSSLContext(File certificateFile, String sslProtocol)
    {
        if (sslProtocol == null || sslProtocol.equals("")) {
            sslProtocol = "TLS";
        }

        SSLContext context = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream in = new FileInputStream(certificateFile);
            Certificate ca = cf.generateCertificate(in);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("ca", ca);
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keystore);
            // Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance(sslProtocol);
            context.init(null, tmf.getTrustManagers(), null);
        } catch (Exception e){
            e.printStackTrace();
        }
        return context;
    }


}
