package com.facturacion.facturacion_service.support;

import com.facturacion.facturacion_service.security.RsaKeyProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.security.PublicKey;

/**
 * Sustituye el RsaKeyProvider real (que carga keys/public_key.pem de producción)
 * por uno que valida con la clave pública de test generada en JwtTestSupport.
 * Así los tests firman y validan tokens con su propio par de claves, sin tocar
 * nada de auth-service ni de producción.
 *
 * Úsala con @Import(TestSecurityBeans.class) en los tests que necesiten pasar
 * la cadena real de seguridad (integración/e2e).
 */
@TestConfiguration
public class TestSecurityBeans {

    @Bean
    @Primary
    public RsaKeyProvider testRsaKeyProvider() throws Exception {
        PublicKey testPublicKey = JwtTestSupport.testPublicKey();
        return new RsaKeyProvider() {
            @Override
            public PublicKey getPublicKey() {
                return testPublicKey;
            }
        };
    }
}
