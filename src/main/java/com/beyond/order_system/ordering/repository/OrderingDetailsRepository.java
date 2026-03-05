package com.beyond.order_system.ordering.repository;

import com.beyond.order_system.ordering.domain.OrderingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingDetailsRepository extends JpaRepository<OrderingDetails, Long> {
}
