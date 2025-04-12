package org.example.tennistournament.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        // Generate a random key for HS256 (at least 256 bits)
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // Encode the key in Base64
        String base64EncodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Your secret key: " + base64EncodedKey);
    }
}
