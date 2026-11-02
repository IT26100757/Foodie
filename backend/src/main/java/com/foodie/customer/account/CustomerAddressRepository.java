package com.foodie.customer.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findAllByCustomer_IdOrderByDefaultAddressDescIdAsc(
            Long customerId
    );

    Optional<CustomerAddress> findByIdAndCustomer_Id(
            Long addressId,
            Long customerId
    );
}