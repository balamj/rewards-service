package com.retail.rewards.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Strategy implementation that handles both the flat rate promotion
 * and the tiered limit based point calculations.
 */
@Component
public class RewardPointsCalculator implements RewardCalculation {

    private static final Logger log = LoggerFactory.getLogger(RewardPointsCalculator.class);

    @Value("${retail.rewards.limit.low}")
    private int lowestPurchaseLimit;

    @Value("${retail.rewards.limit.high}")
    private int highestPurchaseLimit;

    @Value("${retail.rewards.multiplier.between}")
    private int multiplierBetween;

    @Value("${retail.rewards.multiplier.above-high}")
    private int multiplierAboveHigh;

    @Override
    public int calculateRewardPoints(double purchaseAmount) {
        log.info("calculateRewardPoints...");
        try {
            if (purchaseAmount < 0) {
                throw new IllegalArgumentException("Purchase amount cannot be negative: " + purchaseAmount);
            }
            if (purchaseAmount == 0) return 0;

            int totalPoints = calculatePointsForHighThreshold(purchaseAmount) + calculatePointsForRange(purchaseAmount);
            log.debug("Calculated {} points for purchase amount: ${}", totalPoints, purchaseAmount);
            return totalPoints;
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for reward calculation: {}", e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("Unexpected error calculating rewards for amount ${}", purchaseAmount, e);
            return 0;
        }
    }

    // Handles the rule: 2 points for every dollar spent over $100
    int calculatePointsForHighThreshold(double purchaseAmount) {
        log.info("calculatePointsForHighThreshold...");
        if (purchaseAmount > highestPurchaseLimit) {
            return (int) (purchaseAmount - highestPurchaseLimit) * multiplierAboveHigh;
        }
        return 0;
    }

    // Handles the rule: 1 point for every dollar spent between $50 and $100
    int calculatePointsForRange(double purchaseAmount) {
        log.info("calculatePointsForRange...");
        if (purchaseAmount > lowestPurchaseLimit) {
            double midTierAmount = Math.min(purchaseAmount, highestPurchaseLimit);
            return (int) (midTierAmount - lowestPurchaseLimit) * multiplierBetween;
        }
        return 0;
    }
}