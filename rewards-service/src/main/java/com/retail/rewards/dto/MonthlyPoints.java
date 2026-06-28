package com.retail.rewards.dto;

/** Data Transfer Object (DTO) holding the calculated reward points for a single month. */
public class MonthlyPoints {
    public String month;
    public int totalPoints;

    /** Constructor to initialize the month name and its corresponding point total. */
    public MonthlyPoints(String month, int totalPoints) {
        this.month = month;
        this.totalPoints = totalPoints;
    }
}