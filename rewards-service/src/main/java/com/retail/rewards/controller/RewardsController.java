package com.retail.rewards.controller;

import com.retail.rewards.dto.RewardReport;
import com.retail.rewards.exception.CustomerNotFoundException;
import com.retail.rewards.service.RewardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * REST Controller that exposes HTTP endpoints for managing customer loyalty reward operations.
 *
 * @RestController Marks this as a web controller and automatically serializes return values into JSON responses.
 * @RequestMapping Binds all endpoints within this controller to the base path '/api/rewards'.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);

    @Autowired
    private RewardsService rewardsService;

    /**
     * Retrieves calculated reward points for all active customers.
     * @return A CompletableFuture enclosing the list of compiled customer RewardReports.
     */
    @GetMapping(produces = "application/json")
    public CompletableFuture<List<RewardReport>> getRewardsForAllCustomers() {
        log.info("Received request to fetch rewards for all customers");
        return rewardsService.getRewardsForAllCustomers();
    }


    /**
     * Retrieves calculated reward points for a specific customer.
     *
     * @param customerId The unique identifier of the customer.
     * @return A CompletableFuture enclosing the customer's RewardReport.
     */
    @GetMapping(value = "/{customerId}", produces = "application/json")
    public CompletableFuture<ResponseEntity<RewardReport>> getRewardForCustomer(@PathVariable String customerId) {
        log.info("Received request to fetch rewards for customer: {}", customerId);

        return rewardsService.getRewardsForCustomer(customerId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new CustomerNotFoundException("Customer not found with ID: " + customerId );
                });
    }

}