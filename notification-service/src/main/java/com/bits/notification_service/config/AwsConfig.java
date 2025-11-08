package com.bits.notification_service.config;

import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.context.annotation.Bean;

@Configuration
public class AwsConfig {
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "ap-south-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "ap-south-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
