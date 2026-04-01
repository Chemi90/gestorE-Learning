package com.gestorelearning.content.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        System.out.println("[DEBUG-AUTH] Initializing JwtService with secret length: " + secret.length());
        this.signingKey = createSigningKey(secret);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey createSigningKey(String secret) {
        try {
            if (secret.length() >= 32) {
                System.out.println("[DEBUG-AUTH] Using plain string secret (length >= 32)");
                return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("[DEBUG-AUTH] Decoding Base64 secret (length < 32)");
            byte[] decoded = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            System.err.println("[DEBUG-AUTH] Error creating signing key: " + e.getMessage());
            throw e;
        }
    }
}
