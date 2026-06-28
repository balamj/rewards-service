package com.retail.rewards.service;

import com.retail.rewards.dto.MonthlyPoints;
import com.retail.rewards.dto.RewardReport;
import com.retail.rewards.entity.Customer;
import com.retail.rewards.entity.Purchase;
import com.retail.rewards.entity.Reward;
import com.retail.rewards.repository.PurchaseRepository;
import com.retail.rewards.repository.RewardRepository;
import com.retail.rewards.strategy.RewardCalculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service responsible for processing customer purchase histories,
 * calculating tiered loyalty reward points,
 * persisting compiled reports to the database.
 */
@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardCalculation rewardStrategy;

    // Total rolling month window to consider for rewards calculation (e.g., 3 months)
    @Value("${retail.rewards.reward-months}")
    private int rewardMonths;


    /**
     * Asynchronously fetches all raw purchase histories, aggregates monthly totals,
     */
    @Async
    public CompletableFuture<List<RewardReport>> getRewardsForAllCustomers() {
        log.info("Starting rewards calculation for all customers. Rolling window: {} months", rewardMonths);
        try {
        List<Purchase> allPurchases = purchaseRepository.findAll();
        LocalDate cutoffDate = LocalDate.now().minusMonths(rewardMonths - 1).withDayOfMonth(1);
        log.debug("Filtering purchases with cutoff date: {}", cutoffDate);

        // --- SECTION 1: Stream Processing - Filter and Group Transactions ---
        Map<Customer, Map<String, Integer>> pointsMap = allPurchases.stream()
                .filter(t -> !t.getPurchase_date().isBefore(cutoffDate))
                .collect(Collectors.groupingBy(
                        Purchase::getCustomer,
                        Collectors.groupingBy(
                                t -> t.getPurchase_date().getMonth().toString(),
                                Collectors.summingInt(t -> rewardStrategy.calculateRewardPoints(t.getPurchase_amount()))
                        )
                ));

        // Direct DTO conversion to send data back to controller immediately
        List<RewardReport> reports = pointsMap.entrySet().stream().map(entry -> {
            Customer customer = entry.getKey();

            List<MonthlyPoints> threeMonthPoints = entry.getValue().entrySet().stream()
                    .map(e -> new MonthlyPoints(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            int totalPoints = threeMonthPoints.stream().mapToInt(m -> m.totalPoints).sum();

            return new RewardReport(customer.getId(), customer.getCustomer_name(), threeMonthPoints, totalPoints);
        }).collect(Collectors.toList());
        log.info("Successfully generated reward reports for {} customers. Triggering async DB save.", reports.size());

        // Fire-and-forget database updates in a separate async thread context
        saveRewardsToDatabaseAsync(pointsMap);

        return CompletableFuture.completedFuture(reports);
        } catch (Exception e) {
            log.error("Error calculating rewards for customers: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }


    /**
     * Asynchronously fetches purchase history for a specific customer, aggregates monthly totals,
     * and triggers a background task to persist the calculated rewards.
     * @param customerId The unique identifier of the customer.
     * @return A CompletableFuture enclosing the compiled customer RewardReport.
     */
    @Async
    public CompletableFuture<RewardReport> getRewardsForCustomer(String customerId) {
        log.info("Starting rewards calculation for customer ID: {}. Window: {} months", customerId, rewardMonths);
        try {
            List<Purchase> customerPurchases = purchaseRepository.findByCustomerId(Long.valueOf(customerId)); // Adjust repo method name as needed
            LocalDate cutoffDate = LocalDate.now().minusMonths(rewardMonths - 1).withDayOfMonth(1);

            Map<Customer, Map<String, Integer>> pointsMap = customerPurchases.stream()
                    .filter(t -> !t.getPurchase_date().isBefore(cutoffDate))
                    .collect(Collectors.groupingBy(
                            Purchase::getCustomer,
                            Collectors.groupingBy(
                                    t -> t.getPurchase_date().getMonth().toString(),
                                    Collectors.summingInt(t -> rewardStrategy.calculateRewardPoints(t.getPurchase_amount()))
                            )
                    ));

            if (pointsMap.isEmpty()) {
                return CompletableFuture.failedFuture(new RuntimeException("No purchases found for customer: " + customerId));
            }

            Map.Entry<Customer, Map<String, Integer>> entry = pointsMap.entrySet().iterator().next();
            Customer customer = entry.getKey();

            List<MonthlyPoints> threeMonthPoints = entry.getValue().entrySet().stream()
                    .map(e -> new MonthlyPoints(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            int totalPoints = threeMonthPoints.stream().mapToInt(m -> m.totalPoints).sum();
            RewardReport report = new RewardReport(customer.getId(), customer.getCustomer_name(), threeMonthPoints, totalPoints);

            saveRewardsToDatabaseAsync(pointsMap); // Reuses your existing async DB method

            return CompletableFuture.completedFuture(report);
        } catch (Exception e) {
            log.error("Error while calculating rewards for customer {}: {}", customerId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Handles database persistence layer operations in a separate non-blocking worker thread.
     */
    @Async
    @Transactional
    public void saveRewardsToDatabaseAsync(Map<Customer, Map<String, Integer>> pointsMap) {

        if (pointsMap == null || pointsMap.isEmpty()) {
            log.warn("Empty points map provided to async DB save task. Aborting.");
            return;
        }
        log.info("Async DB save operation started for {} records", pointsMap.size());

        try {
            pointsMap.entrySet().forEach(entry -> {
                try {
                Customer customer = entry.getKey();

                List<MonthlyPoints> threeMonthPoints = entry.getValue().entrySet().stream()
                        .map(e -> new MonthlyPoints(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());

                int totalPoints = threeMonthPoints.stream().mapToInt(m -> m.totalPoints).sum();

                Reward rewardRecord = rewardRepository.findById(customer.getId())
                        .orElse(new Reward());

                rewardRecord.setCustomerId(customer.getId());
                rewardRecord.setTotalPoints(totalPoints);
                rewardRecord.setTodayDate(LocalDate.now());

                if (threeMonthPoints.size() > 0) {
                    rewardRecord.setFirstMonth(threeMonthPoints.get(0).month);
                    rewardRecord.setFirstMonthPoints(threeMonthPoints.get(0).totalPoints);
                }
                if (threeMonthPoints.size() > 1) {
                    rewardRecord.setSecondMonth(threeMonthPoints.get(1).month);
                    rewardRecord.setSecondMonthPoints(threeMonthPoints.get(1).totalPoints);
                }
                if (threeMonthPoints.size() > 2) {
                    rewardRecord.setThirdMonth(threeMonthPoints.get(2).month);
                    rewardRecord.setThirdMonthPoints(threeMonthPoints.get(2).totalPoints);
                }

                rewardRepository.save(rewardRecord);
                log.debug("Saved/Updated reward records for customer ID: {}", customer.getId());
                } catch (Exception e) {
                    log.error("Failed to update record for customer ID: {}. Reason: {}",
                            entry.getKey() != null ? entry.getKey().getId() : "Unknown", e.getMessage(), e);
                }
            });
            log.info("Async DB persistence completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during async database operations", e);
        }
    }
}