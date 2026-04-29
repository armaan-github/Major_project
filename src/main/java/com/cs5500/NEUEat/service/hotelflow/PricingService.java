package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Room;

public interface PricingService {

  double getOccupancyRate();

  double calculateQuote(double baseRate);

  double calculateQuote(Room room);
}