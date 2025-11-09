package com.bits.delivery_service.config;

import com.bits.delivery_service.entity.Hub;
import com.bits.delivery_service.entity.Route;
import com.bits.delivery_service.repository.HubRepository;
import com.bits.delivery_service.repository.RouteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedRoutes(RouteRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Route r = new Route();
                r.setId("HYD-CHN-DAILY-001");
                r.setRouteName("Hyderabad - Bangalore - Chennai");
                r.setHubsJson("[\"Hyderabad\",\"Bangalore\",\"Chennai\"]");
                r.setHubArrivalTimesJson("{\"Hyderabad\":\"08:00\",\"Bangalore\":\"14:00\",\"Chennai\":\"20:00\"}");
                repo.save(r);

                Route r2 = new Route();
                r2.setId("DEL-MUM-DAILY-001");
                r2.setRouteName("Delhi - Indore - Mumbai");
                r2.setHubsJson("[\"Delhi\",\"Indore\",\"Mumbai\"]");
                r2.setHubArrivalTimesJson("{\"Delhi\":\"07:00\",\"Indore\":\"13:00\",\"Mumbai\":\"19:00\"}");
                repo.save(r2);
            }
        };
    }

    @Bean
    CommandLineRunner seedHubs(HubRepository hubRepository) {
        return args -> {
            if (hubRepository.count() == 0) {
                Hub h = new Hub();
                h.setName("Hyderabad");
                h.setCode("HYD");
                h.setTimezone("Asia/Kolkata");
                hubRepository.save(h);

                Hub h2 = new Hub();
                h2.setName("Bangalore");
                h2.setCode("BLR");
                h2.setTimezone("Asia/Kolkata");
                hubRepository.save(h2);
            }
        };
    }

    @Bean
    public SqsClient sqsClient() {
        String region = System.getenv("AWS_REGION");

        if (region == null || region.isEmpty()) {
            throw new IllegalStateException("AWS_REGION environment variable is not set");
        }

        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
