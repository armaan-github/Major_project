package com.cs5500.NEUEat.service.hotelflow;

import java.util.Map;

public interface HotelFlowDashboardService {

  Map<String, Object> getReceptionDashboard();

  Map<String, Object> getKitchenDashboard();

  Map<String, Object> getManagerDashboard();

  Map<String, Object> getGuestDashboard(String guestId);
}
