package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.ServiceOrder;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, String> {

  List<ServiceOrder> findByBookingId(String bookingId);

  List<ServiceOrder> findByStatusOrderByCreatedAtAsc(ServiceOrderStatus status);
}
