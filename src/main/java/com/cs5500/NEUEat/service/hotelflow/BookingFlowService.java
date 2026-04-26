package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.exception.RoomNotAvailableException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import java.time.LocalDate;
import java.util.List;

public interface BookingFlowService {

  Booking createBooking(String guestId, String roomId, LocalDate checkInDate, LocalDate checkOutDate)
      throws RoomNotAvailableException;

  Booking checkIn(String bookingId)
      throws BookingNotFoundException, InvalidBookingStateException;

  List<Booking> getGuestBookings(String guestId);
}
