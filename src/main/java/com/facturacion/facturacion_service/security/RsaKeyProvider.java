package com.facturacion.facturacion_service.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaKeyProvider {

    private final PublicKey publicKey;

    public RsaKeyProvider() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKey = loadPublicKey("keys/public_key.pem");
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private PublicKey loadPublicKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String contenido = leerArchivoLimpio(path, "PUBLIC KEY");
        byte[] bytes = Base64.getDecoder().decode(contenido);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String leerArchivoLimpio(String path, String tipo) throws IOException {
        InputStream is = new ClassPathResource(path).getInputStream();
        String contenido = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return contenido
                .replace("-----BEGIN " + tipo + "-----", "")
                .replace("-----END " + tipo + "-----", "")
                .replaceAll("\\s", "");
    }
}