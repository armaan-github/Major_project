package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.security.hotelflow.HotelFlowSession;
import java.util.Optional;

public interface HotelFlowAuthService {

  HotelFlowSession loginGuest(String userName, String password);

  HotelFlowSession loginStaff(String userName, String password);

  Optional<HotelFlowSession> findByToken(String token);

  boolean hasRequiredRole(HotelFlowSession session, HotelFlowRole[] roles);
}
