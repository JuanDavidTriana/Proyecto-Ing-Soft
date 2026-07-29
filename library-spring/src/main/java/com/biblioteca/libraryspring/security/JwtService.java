package com.biblioteca.libraryspring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Encapsula TODO lo relacionado a crear y leer JWT (JSON Web Tokens).
 *
 * Flujo general:
 *   1. En /auth/login, si username+password son correctos, se llama a
 *      generarToken(username) y se devuelve el string resultante al cliente.
 *   2. El cliente manda ese string en cada request futura, en el header
 *      "Authorization: Bearer <token>".
 *   3. JwtAuthFilter (ver esa clase) intercepta cada request, toma el
 *      token del header, y usa validarToken()/extraerUsername() de esta
 *      clase para saber si es válido y de quién es.
 *
 * El token NO se guarda en ningún lado del servidor (no hay sesión en
 * memoria ni en BD): su propia FIRMA es la prueba de que es válido.
 */
@Service
public class JwtService {

    // Se inyecta desde application.properties (jwt.secret). Debe ser una
    // cadena larga y aleatoria: HS256 exige una clave de al menos 256 bits
    // (32 caracteres) para firmar de forma segura.
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Tiempo de vida del token, en milisegundos (por defecto, 1 hora).
    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Convierte el secreto (texto plano) en una SecretKey que la
        // librería JJWT puede usar para firmar/verificar con HMAC-SHA256.
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Genera un JWT firmado. "subject" (sub) es el campo estándar de JWT
     * para identificar de quién es el token; aquí usamos el username. */
    public String generarToken(String username) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrae el username ("sub") del token. Si el token fue manipulado o
     * expiró, parseSignedClaims lanza JwtException. */
    public String extraerUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** true si el token tiene una firma válida y no ha expirado. */
    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
