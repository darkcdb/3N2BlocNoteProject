package me.cjc.a3n2_blocnoteproject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OutilsNip {
    public static String hash(int idNote, String nip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] octets = md.digest((idNote + ":" + nip).getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, octets));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
