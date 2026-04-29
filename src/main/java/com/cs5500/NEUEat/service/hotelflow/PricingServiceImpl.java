package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PricingServiceImpl implements PricingService {

  private static final double HIGH_OCCUPANCY_SURCHARGE = 1.20;

  @Autowired
  private RoomRepository roomRepository;

  @Override
  public double getOccupancyRate() {
    long totalRooms = roomRepository.count();
    if (totalRooms == 0) {
      return 0;
    }

    long occupiedRooms = roomRepository.findByStatus(RoomStatus.OCCUPIED).size();
    return ((double) occupiedRooms / totalRooms) * 100;
  }

  @Override
  public double calculateQuote(double baseRate) {
    double quotedRate = baseRate;
    if (getOccupancyRate() > 80) {
      quotedRate = baseRate * HIGH_OCCUPANCY_SURCHARGE;
    }
    return BigDecimal.valueOf(quotedRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  @Override
  public double calculateQuote(Room room) {
    return calculateQuote(room.getNightlyRate());
  }
}