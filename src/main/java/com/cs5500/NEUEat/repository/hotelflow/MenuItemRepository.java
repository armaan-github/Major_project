package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

  List<MenuItem> findAllByAvailableTrueOrderByCategoryAscDisplayOrderAscItemNameAsc();

  List<MenuItem> findAllByOrderByCategoryAscDisplayOrderAscItemNameAsc();
}