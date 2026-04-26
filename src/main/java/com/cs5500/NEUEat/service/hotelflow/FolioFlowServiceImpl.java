package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.FolioLine;
import com.cs5500.NEUEat.repository.hotelflow.BookingRepository;
import com.cs5500.NEUEat.repository.hotelflow.FolioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolioFlowServiceImpl implements FolioFlowService {

  @Autowired
  private FolioRepository folioRepository;

  @Autowired
  private BookingRepository bookingRepository;

  @Override
  public Folio addCharge(String bookingId, FolioLine line) throws FolioNotFoundException {
    Optional<Folio> folioOptional = folioRepository.findByBookingId(bookingId);
    if (folioOptional.isEmpty()) {
      throw new FolioNotFoundException("Folio not found for booking");
    }
    Folio folio = folioOptional.get();
    if (folio.getLines() == null) {
      folio.setLines(new ArrayList<>());
    }
    folio.getLines().add(line);
    folio.setUpdatedAt(LocalDateTime.now());
    return folioRepository.save(folio);
  }

  @Override
  public Folio getFolioByBookingId(String bookingId) throws FolioNotFoundException {
    return folioRepository.findByBookingId(bookingId)
        .orElseThrow(() -> new FolioNotFoundException("Folio not found for booking"));
  }

  @Override
  public Folio ensureFolio(String bookingId) throws BookingNotFoundException {
    Optional<Folio> folioOptional = folioRepository.findByBookingId(bookingId);
    if (folioOptional.isPresent()) {
      return folioOptional.get();
    }

    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

    Folio folio = new Folio();
    folio.setBookingId(booking.getId());
    folio.setGuestId(booking.getGuestId());
    folio.setRoomId(booking.getRoomId());
    return folioRepository.save(folio);
  }
}
