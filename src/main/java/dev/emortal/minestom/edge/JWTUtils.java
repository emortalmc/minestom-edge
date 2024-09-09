package dev.emortal.minestom.edge;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;

import java.security.Key;
import java.util.Date;

public class JWTUtils {
    private static final long EXPIRATION_TIME = 1000 * 30; // 30 seconds
    private static final String HOSTNAME = System.getenv("HOSTNAME");

    private static final Key SIGNING_KEY;

    static {
        String signingKey = System.getenv("EDGE_ROUTING_KEY");
        SIGNING_KEY = Keys.hmacShaKeyFor(signingKey.getBytes());
    }

    public static @NotNull String generateJWT(@NotNull String username, @NotNull String proxyId) {
        return Jwts.builder()
                .issuedAt(new Date()) // iat
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // exp
                .issuer(HOSTNAME) // iss
                .claim("username", username)
                .claim("proxyId", proxyId)
                .signWith(SIGNING_KEY)
                .compact();
    }
}
