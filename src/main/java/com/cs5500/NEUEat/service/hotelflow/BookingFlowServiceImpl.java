package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.exception.RoomNotAvailableException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.BookingStatus;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.FolioLine;
import com.cs5500.NEUEat.model.hotelflow.Guest;
import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.repository.hotelflow.GuestRepository;
import com.cs5500.NEUEat.repository.hotelflow.BookingRepository;
import com.cs5500.NEUEat.repository.hotelflow.FolioRepository;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private FolioRepository folioRepository;

  @Autowired
  private HotelFlowEventService hotelFlowEventService;

  @Autowired
  private FolioFlowService folioFlowService;

  @Autowired
  private GuestRepository guestRepository;

  @Autowired
  private PricingService pricingService;

  private static final LocalTime CHECK_IN_TIME = LocalTime.of(12, 0);
  private static final double EARLY_CHECK_IN_FEE = 25.0;

  @Override
  public Booking createBooking(String guestId, String roomId, LocalDate checkInDate,
      LocalDate checkOutDate) throws RoomNotAvailableException {
    Optional<Room> roomOptional = roomRepository.findById(roomId);
    if (roomOptional.isEmpty() || roomOptional.get().getStatus() != RoomStatus.AVAILABLE) {
      throw new RoomNotAvailableException("Room is not available");
    }
    if (checkOutDate == null || checkInDate == null || !checkOutDate.isAfter(checkInDate)) {
      throw new RoomNotAvailableException("Check-out date must be after check-in date");
    }

    Room room = roomOptional.get();
    double quotedNightlyRate = pricingService.calculateQuote(room);
    room.setStatus(RoomStatus.RESERVED);
    roomRepository.save(room);

    Booking booking = new Booking();
    booking.setGuestId(guestId);
    booking.setRoomId(roomId);
    booking.setCheckInDate(checkInDate);
    booking.setCheckOutDate(checkOutDate);
    booking.setQuotedNightlyRate(quotedNightlyRate);
    booking.setStatus(BookingStatus.RESERVED);
    Booking savedBooking = bookingRepository.save(booking);
    hotelFlowEventService.publish("booking.created", savedBooking);
    hotelFlowEventService.publish("room.status.changed", room);
    return savedBooking;
  }

  @Override
  public Booking checkIn(String bookingId)
      throws BookingNotFoundException, InvalidBookingStateException, FolioNotFoundException {
    Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
    if (bookingOptional.isEmpty()) {
      throw new BookingNotFoundException("Booking not found");
    }

    Booking booking = bookingOptional.get();
    if (booking.getStatus() != BookingStatus.RESERVED) {
      throw new InvalidBookingStateException("Only reserved bookings can check in");
    }

    Optional<Room> roomOptional = roomRepository.findById(booking.getRoomId());
    if (roomOptional.isEmpty()) {
      throw new BookingNotFoundException("Room linked to booking not found");
    }

    Room room = roomOptional.get();
    room.setStatus(RoomStatus.OCCUPIED);
    roomRepository.save(room);

    booking.setStatus(BookingStatus.CHECKED_IN);
    Booking updatedBooking = bookingRepository.save(booking);
    Optional<Guest> guestOptional = guestRepository.findById(updatedBooking.getGuestId());

    Folio folio = folioRepository.findByBookingId(updatedBooking.getId()).orElseGet(Folio::new);
    folio.setBookingId(updatedBooking.getId());
    folio.setGuestId(updatedBooking.getGuestId());
    folio.setRoomId(updatedBooking.getRoomId());

    if (folio.getLines() == null) {
      folio.setLines(new ArrayList<>());
    }

    long nights = ChronoUnit.DAYS.between(updatedBooking.getCheckInDate(), updatedBooking.getCheckOutDate());
    if (nights <= 0) {
      nights = 1;
    }
    double nightlyRate = updatedBooking.getQuotedNightlyRate() > 0
        ? updatedBooking.getQuotedNightlyRate()
        : room.getNightlyRate();
    double roomCharge = nights * nightlyRate;
    FolioLine line = new FolioLine("ROOM", updatedBooking.getId(),
        "Room charge for " + nights + " night(s)", roomCharge, LocalDateTime.now());
    folio.getLines().add(line);
    folio.setUpdatedAt(LocalDateTime.now());
    Folio savedFolio = folioRepository.save(folio);

    if (LocalTime.now().isBefore(CHECK_IN_TIME)) {
      FolioLine earlyFeeLine = new FolioLine("SERVICE", updatedBooking.getId(),
          "Early Check-in Fee", EARLY_CHECK_IN_FEE, LocalDateTime.now());
      savedFolio = folioFlowService.addCharge(updatedBooking.getId(), earlyFeeLine);
    }

    String guestName = guestOptional.map(Guest::getUserName).orElse(updatedBooking.getGuestId());
    hotelFlowEventService.publish("booking.checked_in", guestName);
    hotelFlowEventService.publish("checkin.completed", updatedBooking);
    hotelFlowEventService.publish("room.status.changed", room);
    hotelFlowEventService.publish("folio.updated", savedFolio);

    return updatedBooking;
  }

  @Override
  public List<Booking> getGuestBookings(String guestId) {
    return bookingRepository.findByGuestId(guestId);
  }

  @Override
  public List<Booking> quickSearch(String query) {
    return bookingRepository.findByQuickSearch(query);
  }
}
