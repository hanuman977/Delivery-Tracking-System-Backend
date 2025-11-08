package com.bits.gateway_server.util;

import com.bits.gateway_server.config.AppProperties;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@AllArgsConstructor
@Slf4j
public class JwtUtil {

    AppProperties appProperties;

    public boolean validateToken(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String kid = jwsObject.getHeader().getKeyID();

            JWKSet publicKeys = JWKSet.load(new URL(appProperties.getJwksUri()));
            JWK jwk = publicKeys.getKeyByKeyId(kid);

            if (jwk == null) return false;

            RSAKey rsaKey = (RSAKey) jwk;
            return jwsObject.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(rsaKey.toRSAPublicKey()));
        } catch (Exception e) {
            return false;
        }
    }
}
