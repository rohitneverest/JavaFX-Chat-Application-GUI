package org.chat.javachatapplication.util;

import java.util.Base64;

public class EncryptionUtil {
    public static String encrypt(String msg) {
        if (msg == null) return null;
        return Base64.getEncoder().encodeToString(msg.getBytes());
    }
    public static String decrypt(String cipher) {
        if (cipher == null) return null;
        try {
            return new String(Base64.getDecoder().decode(cipher));
        } catch (IllegalArgumentException e) {
            // not a base64 string — return as-is
            return cipher;
        }
    }
}
