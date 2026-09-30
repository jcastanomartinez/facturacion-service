package com.facturacion.facturacion_service.support;

import io.jsonwebtoken.Jwts;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

/**
 * facturacion-service SOLO tiene la clave pública de auth-service (RsaKeyProvider
 * carga keys/public_key.pem). Para poder firmar tokens "válidos" en los tests sin
 * tocar ninguna clave real, generamos aquí un par RSA propio de test y lo usamos
 * en dos sitios:
 *   1. Aquí, para firmar el token (con la clave PRIVADA de test).
 *   2. En TestSecurityBeans, para sustituir el RsaKeyProvider real por uno que
 *      valida con la clave PÚBLICA de test (ver esa clase).
 */
public final class JwtTestSupport {

    public static final KeyPair TEST_KEY_PAIR = generateKeyPair();

    private JwtTestSupport() {
    }

    public static PublicKey testPublicKey() {
        return TEST_KEY_PAIR.getPublic();
    }

    public static PrivateKey testPrivateKey() {
        return TEST_KEY_PAIR.getPrivate();
    }

    /** Token válido, expira en 1 hora, con el email y los roles indicados. */
    public static String validToken(String email, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3_600_000L);

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(testPrivateKey())
                .compact();
    }

    /** Token ya caducado, útil para probar el camino de rechazo. */
    public static String expiredToken(String email, List<String> roles) {
        Date past = new Date(System.currentTimeMillis() - 3_600_000L);
        Date evenMorePast = new Date(past.getTime() - 60_000L);

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(evenMorePast)
                .expiration(past)
                .signWith(testPrivateKey())
                .compact();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se ha podido generar el par de claves RSA de test", e);
        }
    }
}
