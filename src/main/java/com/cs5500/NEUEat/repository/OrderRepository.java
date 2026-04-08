package com.cs5500.NEUEat.repository;

import com.cs5500.NEUEat.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository()
public interface OrderRepository extends JpaRepository<Order, String> {

}
