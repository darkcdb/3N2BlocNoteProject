package me.cjc.a3n2_blocnoteproject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe OutilsNip servant au Hash des NIP/PIN et a la vérification de la correspondance.
 */
public class OutilsNip {
    /**
     * Méthode qui hash un NIP/PIN.
     * @param idNote Id de la note.
     * @param nip NIP/PIN à hasher.
     * @return Retourne le hash du NIP/PIN.
     */
    public static String hash(int idNote, String nip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] octets = md.digest((idNote + ":" + nip).getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, octets));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode qui compare deux hash.
     * @param Hash1 Hash à comparer.
     * @param Hash2 Hash à comparer.
     * @return Retourne true si les hash sont identiques, false sinon.
     */
    public static boolean estMemeHash(String Hash1, String Hash2) {
        return Hash1.equals(Hash2);
    }
}
