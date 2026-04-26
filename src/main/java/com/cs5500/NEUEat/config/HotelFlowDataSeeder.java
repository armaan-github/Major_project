package com.cs5500.NEUEat.config;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.model.hotelflow.HotelFlowStaff;
import com.cs5500.NEUEat.model.hotelflow.InventoryItem;
import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.repository.hotelflow.HotelFlowStaffRepository;
import com.cs5500.NEUEat.repository.hotelflow.InventoryItemRepository;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import com.cs5500.NEUEat.service.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class HotelFlowDataSeeder implements CommandLineRunner {

  private final HotelFlowStaffRepository staffRepository;
  private final RoomRepository roomRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final PasswordService passwordService;

  public HotelFlowDataSeeder(HotelFlowStaffRepository staffRepository, RoomRepository roomRepository,
      InventoryItemRepository inventoryItemRepository) {
    this.staffRepository = staffRepository;
    this.roomRepository = roomRepository;
    this.inventoryItemRepository = inventoryItemRepository;
    this.passwordService = new PasswordService();
  }

  @Override
  public void run(String... args) {
    seedStaffIfMissing("reception", "reception123", HotelFlowRole.RECEPTION);
    seedStaffIfMissing("kitchen", "kitchen123", HotelFlowRole.KITCHEN);
    seedStaffIfMissing("manager", "manager123", HotelFlowRole.MANAGER);

    if (roomRepository.count() == 0) {
      Room room1 = new Room();
      room1.setRoomNumber("101");
      room1.setRoomType("Standard");
      room1.setCapacity(2);
      room1.setNightlyRate(120);
      room1.setStatus(RoomStatus.AVAILABLE);
      roomRepository.save(room1);

      Room room2 = new Room();
      room2.setRoomNumber("102");
      room2.setRoomType("Deluxe");
      room2.setCapacity(3);
      room2.setNightlyRate(180);
      room2.setStatus(RoomStatus.AVAILABLE);
      roomRepository.save(room2);
    }

    if (inventoryItemRepository.count() == 0) {
      inventoryItemRepository.save(new InventoryItem("Eggs", 12, 20));
      inventoryItemRepository.save(new InventoryItem("Coffee Beans", 40, 25));
      inventoryItemRepository.save(new InventoryItem("Bread", 15, 15));
    }
  }

  private void seedStaffIfMissing(String userName, String rawPassword, HotelFlowRole role) {
    if (staffRepository.findByUserName(userName).isEmpty()) {
      String hashed = passwordService.generatePassword(rawPassword);
      staffRepository.save(new HotelFlowStaff(userName, hashed, role));
    }
  }
}
