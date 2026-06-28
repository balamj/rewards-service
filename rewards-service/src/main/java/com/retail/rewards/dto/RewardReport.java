package com.retail.rewards.dto;

import java.util.List;

/** Data Transfer Object (DTO) representing a customer's aggregated rewards report. */
public class RewardReport {
    public Long customerId;
    public String customerName;
    public List<MonthlyPoints> rewardPointsSummary;
    public int totalRewardPoints;

    /** Constructor to initialize all immutable report fields for JSON serialization. */
    public RewardReport(Long id, String name, List<MonthlyPoints> rewardPointsSummary, int totalPoints) {
        this.customerId = id;
        this.customerName = name;
        this.rewardPointsSummary = rewardPointsSummary;
        this.totalRewardPoints = totalPoints;
    }
}