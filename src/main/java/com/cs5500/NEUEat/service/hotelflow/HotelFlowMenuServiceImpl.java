package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.MenuItem;
import com.cs5500.NEUEat.repository.hotelflow.MenuItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelFlowMenuServiceImpl implements HotelFlowMenuService {

  @Autowired
  private MenuItemRepository menuItemRepository;

  @Override
  public List<MenuItem> getActiveMenu() {
    return menuItemRepository.findAllByAvailableTrueOrderByCategoryAscDisplayOrderAscItemNameAsc();
  }

  @Override
  public List<MenuItem> getAllMenuItems() {
    return menuItemRepository.findAllByOrderByCategoryAscDisplayOrderAscItemNameAsc();
  }

  @Override
  public Optional<MenuItem> findByItemName(String itemName) {
    return menuItemRepository.findAll().stream()
        .filter(item -> item.getItemName().equalsIgnoreCase(itemName))
        .findFirst();
  }

  @Override
  public MenuItem save(MenuItem item) {
    if (item.getCreatedAt() == null) {
      item.setCreatedAt(LocalDateTime.now());
    }
    item.setUpdatedAt(LocalDateTime.now());
    return menuItemRepository.save(item);
  }

  @Override
  public void delete(String id) {
    menuItemRepository.deleteById(id);
  }
}