package no.pharmacy.infrastructure;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

    public static SecretKey pbeKey(char[] password) {
        try {
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            return keyFac.generateSecret(new PBEKeySpec(password));
        } catch (GeneralSecurityException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public static SecretKey aesKey(byte[] key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            return new SecretKeySpec(Arrays.copyOf(sha.digest(key), 16), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

}
