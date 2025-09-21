package org.example.authcalc.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int SALT_LEN = 16; // bytes
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256; // bits

    public static String generateSaltBase64() {
        byte[] salt = new byte[SALT_LEN];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(char[] password, byte[] salt, String expectedHashBase64) {
        String hashed = hashPassword(password, salt);
        return MessageDigest.isEqual(hashed.getBytes(), expectedHashBase64.getBytes());
    }
}
