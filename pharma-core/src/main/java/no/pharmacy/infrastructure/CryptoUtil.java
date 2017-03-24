package no.pharmacy.infrastructure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

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

}
