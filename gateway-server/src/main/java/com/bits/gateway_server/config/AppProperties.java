package com.bits.gateway_server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppProperties {
    @Value("${COGNITO_REGION:}")
    String region;
    @Value("${COGNITO_USER_POOL_ID:}")
    String userPoolId;

    @Value("${COGNITO_JWKS_URI:}")
    String jwksUri;
    @Value("${COGNITO_ISSUER:}")
    String issuer;
}
