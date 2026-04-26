package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.BookingStatus;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.Invoice;
import com.cs5500.NEUEat.model.hotelflow.InvoiceStatus;
import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.repository.hotelflow.BookingRepository;
import com.cs5500.NEUEat.repository.hotelflow.FolioRepository;
import com.cs5500.NEUEat.repository.hotelflow.InvoiceRepository;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckoutFlowServiceImpl implements CheckoutFlowService {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private FolioRepository folioRepository;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private HotelFlowEventService hotelFlowEventService;

  @Override
  public Invoice checkout(String bookingId)
      throws BookingNotFoundException, FolioNotFoundException, InvalidBookingStateException {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

    if (booking.getStatus() != BookingStatus.CHECKED_IN) {
      throw new InvalidBookingStateException("Only checked-in bookings can check out");
    }

    Folio folio = folioRepository.findByBookingId(bookingId)
        .orElseThrow(() -> new FolioNotFoundException("Folio not found for booking"));

    double subtotal = folio.getTotal();
    double tax = subtotal * 0.08;
    double total = subtotal + tax;

    Invoice invoice = invoiceRepository.findByBookingId(bookingId).orElseGet(Invoice::new);
    invoice.setBookingId(bookingId);
    invoice.setFolioId(folio.getId());
    invoice.setSubtotal(subtotal);
    invoice.setTax(tax);
    invoice.setTotal(total);
    invoice.setStatus(InvoiceStatus.UNPAID);
    invoice.setCreatedAt(LocalDateTime.now());
    Invoice savedInvoice = invoiceRepository.save(invoice);

    booking.setStatus(BookingStatus.CHECKED_OUT);
    bookingRepository.save(booking);

    Room room = roomRepository.findById(booking.getRoomId())
        .orElseThrow(() -> new BookingNotFoundException("Room linked to booking not found"));
    room.setStatus(RoomStatus.DIRTY);
    roomRepository.save(room);

    folio.setClosed(true);
    folio.setUpdatedAt(LocalDateTime.now());
    folioRepository.save(folio);

    hotelFlowEventService.publish("invoice.generated", savedInvoice);
    hotelFlowEventService.publish("checkout.completed", booking);
    hotelFlowEventService.publish("room.status.changed", room);

    return savedInvoice;
  }

  @Override
  public Invoice settleInvoice(String invoiceId, double paymentAmount)
      throws BookingNotFoundException {
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new BookingNotFoundException("Invoice not found"));

    if (paymentAmount >= invoice.getTotal()) {
      invoice.setStatus(InvoiceStatus.PAID);
    } else if (paymentAmount > 0) {
      invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
    }

    return invoiceRepository.save(invoice);
  }
}
