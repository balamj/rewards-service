package com.retail.rewards.repository;

import com.retail.rewards.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data Access Object (DAO) Interface for the Reward Entity.*/
@Repository
    public interface RewardRepository extends JpaRepository<Reward, Long> {
}