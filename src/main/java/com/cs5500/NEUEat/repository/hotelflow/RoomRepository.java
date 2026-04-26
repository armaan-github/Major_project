package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

  Optional<Room> findByRoomNumber(String roomNumber);

  List<Room> findByStatus(RoomStatus status);
}
