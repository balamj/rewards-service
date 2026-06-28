package com.retail.rewards.strategy;

/**
 * Standard contract for reward point calculation algorithms.
 * Allows decoupling the core mathematical formulas from core service workflows.
 */
public interface RewardCalculation {

    /**
     * Executes the point calculation logic based on transaction value.
     * @param purchaseAmount Total transaction amount for an individual purchase.
     * @return Calculated point total (guaranteed non-negative integer).
     */
    int calculateRewardPoints(double purchaseAmount);
}