package com.airline.bakend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tiny JWT (JSON Web Token) implementation.
 * A token is three Base64 parts joined by dots:  header.payload.signature
 * The signature is HMAC-SHA256(header.payload, secret). If anyone edits the payload, the signature no longer matches.
 */
@Service
public class JwtService {

    public record TokenData(String email, String role) {}

    private static final long VALID_SECONDS = 24 * 60 * 60;
    private static final Pattern SUB = Pattern.compile("\"sub\":\"([^\"]+)\"");
    private static final Pattern ROLE = Pattern.compile("\"role\":\"([^\"]+)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generate(String email, String role) {
        long exp = Instant.now().getEpochSecond() + VALID_SECONDS;
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + email + "\",\"role\":\"" + role + "\",\"exp\":" + exp + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    /** Returns the data inside the token, or null if the token is fake, edited or expired. */
    public TokenData parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            byte[] expected = sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(expected, parts[2].getBytes(StandardCharsets.UTF_8))) return null;

            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher sub = SUB.matcher(json);
            Matcher role = ROLE.matcher(json);
            Matcher exp = EXP.matcher(json);
            if (!sub.find() || !role.find() || !exp.find()) return null;
            if (Long.parseLong(exp.group(1)) < Instant.now().getEpochSecond()) return null;
            return new TokenData(sub.group(1), role.group(1));
        } catch (Exception e) {
            return null;
        }
    }

    private String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
