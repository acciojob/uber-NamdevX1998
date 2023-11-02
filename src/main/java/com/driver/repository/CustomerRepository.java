package com.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.driver.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
    @Query(value = "delete from customer where customer_id = :customerId ")
    void deleteCustomer(Integer customerId);
}
