package com.retail.rewards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@Table(name = "rewards")
public class Reward {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    private String firstMonth;
    private int firstMonthPoints;

    private String secondMonth;
    private int secondMonthPoints;

    private String thirdMonth;
    private int thirdMonthPoints;

    private int totalPoints;
    private LocalDate todayDate;

    public Reward() {
    }

}