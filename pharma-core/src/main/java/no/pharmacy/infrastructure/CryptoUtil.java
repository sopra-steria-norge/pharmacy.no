package no.pharmacy.infrastructure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.SneakyThrows;

public class CryptoUtil {

    private static CertificateFactory certificateFactory;

    public static byte[] sha256(String text) {
        byte[] hash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(text.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.softenException(e);
        }
        return hash;
    }

    public static SecretKey aesKey(byte[] key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            return new SecretKeySpec(Arrays.copyOf(sha.digest(key), 16), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    @SneakyThrows(CertificateException.class)
    public static synchronized X509Certificate decodeCertificate(String key) {
        if (certificateFactory == null) {
            certificateFactory = CertificateFactory.getInstance("X.509");
        }
        return (X509Certificate) certificateFactory.generateCertificate(IOUtil.asInputStream(key));
    }

}
