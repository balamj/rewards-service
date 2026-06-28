package com.retail.rewards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Retail Rewards Spring Boot Application.
 */
@SpringBootApplication
@EnableAsync
public class RewardsServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(RewardsServiceApplication.class);

    public static void main(String[] args) {
        log.info("Starting Retail Rewards Application...");
        SpringApplication.run(RewardsServiceApplication.class, args);
        log.info("Retail Rewards Application started successfully.");
    }

}
