package com.retail.rewards;

import com.retail.rewards.dto.RewardReport;
import com.retail.rewards.entity.Customer;
import com.retail.rewards.entity.Purchase;
import com.retail.rewards.repository.PurchaseRepository;
import com.retail.rewards.repository.RewardRepository;
import com.retail.rewards.service.RewardsService;
import com.retail.rewards.strategy.RewardPointsCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Use org.springframework.boot.test.mock.mockito.MockBean if on older Boot versions
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RewardsServiceApplicationTests {

    @MockitoBean
    private PurchaseRepository purchaseRepository;

    @MockitoBean
    private RewardRepository rewardRepository;

    @Autowired
    private RewardPointsCalculator rewardPointsCalculator;

    @Autowired
    private RewardsService rewardsService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(rewardPointsCalculator, "lowestPurchaseLimit", 50);
        ReflectionTestUtils.setField(rewardPointsCalculator, "highestPurchaseLimit", 100);
        ReflectionTestUtils.setField(rewardPointsCalculator, "multiplierBetween", 1);
        ReflectionTestUtils.setField(rewardPointsCalculator, "multiplierAboveHigh", 2);
        ReflectionTestUtils.setField(rewardsService, "rewardMonths", 3);
    }

    // ELIGIBILITY FOCUS Test Cases
    @Test
    public void testHigherTransactionCustomer() throws ExecutionException, InterruptedException {
        Customer alice = new Customer(101L, "Alice Smith");
        Purchase p1 = new Purchase(1L, alice, 120.0, LocalDate.of(2026, 4, 10));
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(p1));

        CompletableFuture<List<RewardReport>> future = rewardsService.getRewardsForAllCustomers();
        List<RewardReport> reports = future.get();

        assertFalse(reports.isEmpty());
        assertTrue(reports.get(0).totalRewardPoints > 0);
    }

    @Test
    public void testLowerTransactionCustomer() throws ExecutionException, InterruptedException {
        Customer emma = new Customer(105L, "Emma Wilson");
        Purchase p9 = new Purchase(9L, emma, 50.0, LocalDate.of(2026, 6, 1));
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(p9));

        CompletableFuture<List<RewardReport>> future = rewardsService.getRewardsForAllCustomers();
        List<RewardReport> reports = future.get();

        assertFalse(reports.isEmpty());
        assertEquals(0, reports.get(0).totalRewardPoints);
    }

    @Test
    public void testOutsideOfferPeriodCustomer() throws ExecutionException, InterruptedException {
        Customer john = new Customer(106L, "John Smith");
        Purchase p10 = new Purchase(10L, john, 250.0, LocalDate.of(2026, 1, 1));
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(p10));

        CompletableFuture<List<RewardReport>> future = rewardsService.getRewardsForAllCustomers();
        List<RewardReport> reports = future.get();

        assertTrue(reports.isEmpty());
    }

    // CALCULATION FOCUS Testcases
    @Test
    public void testHighPurchaseRewardPoints() {
        double purchaseAmount = 120.0;
        int highThresholdPoints = (int) ReflectionTestUtils.invokeMethod(rewardPointsCalculator, "calculatePointsForHighThreshold", purchaseAmount);
        assertEquals(40, highThresholdPoints);
    }

    @Test
    public void testRangePurchaseRewardPoints() {
        double purchaseAmount = 75.0;
        int rangePoints = (int) ReflectionTestUtils.invokeMethod(rewardPointsCalculator, "calculatePointsForRange", purchaseAmount);
        assertEquals(25, rangePoints);
    }

    @Test
    public void testHigherAndRangePurchaseRewardPoints() throws ExecutionException, InterruptedException {
        Customer charlie = new Customer(103L, "Charlie Brown");
        Purchase p5 = new Purchase(5L, charlie, 110.0, LocalDate.of(2026, 4, 5));
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(p5));

        CompletableFuture<List<RewardReport>> future = rewardsService.getRewardsForAllCustomers();
        List<RewardReport> reports = future.get();

        assertFalse(reports.isEmpty());
        assertEquals(70, reports.get(0).totalRewardPoints);
    }
}