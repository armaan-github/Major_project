package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.MenuItem;
import java.util.List;
import java.util.Optional;

public interface HotelFlowMenuService {

  List<MenuItem> getActiveMenu();

  List<MenuItem> getAllMenuItems();

  Optional<MenuItem> findByItemName(String itemName);

  MenuItem save(MenuItem item);

  void delete(String id);
}