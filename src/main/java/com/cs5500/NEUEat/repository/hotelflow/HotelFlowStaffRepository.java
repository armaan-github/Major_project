package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowStaff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelFlowStaffRepository extends JpaRepository<HotelFlowStaff, String> {

  Optional<HotelFlowStaff> findByUserName(String userName);
}
