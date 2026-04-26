package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.BookingStatus;
import com.cs5500.NEUEat.model.hotelflow.FolioLine;
import com.cs5500.NEUEat.model.hotelflow.ServiceItem;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrder;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrderStatus;
import com.cs5500.NEUEat.repository.hotelflow.BookingRepository;
import com.cs5500.NEUEat.repository.hotelflow.ServiceOrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceOrderFlowServiceImpl implements ServiceOrderFlowService {

  @Autowired
  private ServiceOrderRepository serviceOrderRepository;

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private FolioFlowService folioFlowService;

  @Autowired
  private HotelFlowEventService hotelFlowEventService;

  @Override
  public ServiceOrder placeQrOrder(String bookingId, List<ServiceItem> items)
      throws BookingNotFoundException, InvalidBookingStateException, FolioNotFoundException {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

    if (booking.getStatus() != BookingStatus.CHECKED_IN) {
      throw new InvalidBookingStateException("Guest must be checked in before placing QR order");
    }

    double total = 0;
    for (ServiceItem item : items) {
      total += item.getLineTotal();
    }

    ServiceOrder order = new ServiceOrder();
    order.setBookingId(bookingId);
    order.setGuestId(booking.getGuestId());
    order.setRoomId(booking.getRoomId());
    order.setItems(items);
    order.setTotalAmount(total);
    order.setStatus(ServiceOrderStatus.PENDING);
    ServiceOrder saved = serviceOrderRepository.save(order);

    FolioLine line = new FolioLine("FOOD", saved.getId(),
        "QR order posted to room", total, LocalDateTime.now());
    folioFlowService.addCharge(bookingId, line);

    saved.setPostedToFolio(true);
    ServiceOrder finalOrder = serviceOrderRepository.save(saved);
    hotelFlowEventService.publish("order.placed", finalOrder);
    hotelFlowEventService.publish("folio.charge.posted", line);
    return finalOrder;
  }

  @Override
  public ServiceOrder updateOrderStatus(String orderId, ServiceOrderStatus status)
      throws BookingNotFoundException {
    Optional<ServiceOrder> orderOptional = serviceOrderRepository.findById(orderId);
    if (orderOptional.isEmpty()) {
      throw new BookingNotFoundException("Service order not found");
    }

    ServiceOrder order = orderOptional.get();
    order.setStatus(status);
    ServiceOrder updated = serviceOrderRepository.save(order);
    hotelFlowEventService.publish("order.status.updated", updated);
    return updated;
  }

  @Override
  public List<ServiceOrder> getKitchenQueue() {
    List<ServiceOrder> orders = new ArrayList<>();
    orders.addAll(serviceOrderRepository.findByStatusOrderByCreatedAtAsc(ServiceOrderStatus.PENDING));
    orders.addAll(serviceOrderRepository.findByStatusOrderByCreatedAtAsc(ServiceOrderStatus.IN_PREPARATION));
    orders.sort(Comparator.comparing(ServiceOrder::getCreatedAt));
    return orders;
  }
}
