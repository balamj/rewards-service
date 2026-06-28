package com.retail.rewards.repository;

import com.retail.rewards.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data Access Object (DAO) Interface for the Customer Entity.*/
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}