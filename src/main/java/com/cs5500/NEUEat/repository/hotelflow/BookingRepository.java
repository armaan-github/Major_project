package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.Guest;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

  List<Booking> findByGuestId(String guestId);

  @Query("SELECT b FROM Booking b JOIN Guest g ON b.guestId = g.id "
      + "WHERE g.phoneNumber = :query OR g.id = :query OR b.id = :query")
  List<Booking> findByQuickSearch(@Param("query") String query);
}
