package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.BookingStatus;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.InventoryItem;
import com.cs5500.NEUEat.model.hotelflow.Invoice;
import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrder;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrderStatus;
import com.cs5500.NEUEat.repository.hotelflow.BookingRepository;
import com.cs5500.NEUEat.repository.hotelflow.FolioRepository;
import com.cs5500.NEUEat.repository.hotelflow.InventoryItemRepository;
import com.cs5500.NEUEat.repository.hotelflow.InvoiceRepository;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import com.cs5500.NEUEat.repository.hotelflow.ServiceOrderRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelFlowDashboardServiceImpl implements HotelFlowDashboardService {

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private ServiceOrderRepository serviceOrderRepository;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private InventoryItemRepository inventoryItemRepository;

  @Autowired
  private FolioRepository folioRepository;

  @Override
  public Map<String, Object> getReceptionDashboard() {
    Map<String, Object> out = new HashMap<>();
    List<Room> allRooms = roomRepository.findAll();
    out.put("totalRooms", allRooms.size());
    out.put("availableRooms", roomRepository.findByStatus(RoomStatus.AVAILABLE).size());
    out.put("occupiedRooms", roomRepository.findByStatus(RoomStatus.OCCUPIED).size());
    out.put("dirtyRooms", roomRepository.findByStatus(RoomStatus.DIRTY).size());

    int reservedBookings = 0;
    for (Booking booking : bookingRepository.findAll()) {
      if (booking.getStatus() == BookingStatus.RESERVED) {
        reservedBookings++;
      }
    }
    out.put("pendingCheckIns", reservedBookings);
    return out;
  }

  @Override
  public Map<String, Object> getKitchenDashboard() {
    Map<String, Object> out = new HashMap<>();
    List<ServiceOrder> all = serviceOrderRepository.findAll();
    int pending = 0;
    int inPreparation = 0;
    int ready = 0;
    for (ServiceOrder order : all) {
      if (order.getStatus() == ServiceOrderStatus.PENDING) {
        pending++;
      } else if (order.getStatus() == ServiceOrderStatus.IN_PREPARATION) {
        inPreparation++;
      } else if (order.getStatus() == ServiceOrderStatus.READY) {
        ready++;
      }
    }
    out.put("pendingOrders", pending);
    out.put("inPreparationOrders", inPreparation);
    out.put("readyOrders", ready);
    out.put("queue", serviceOrderRepository.findByStatusOrderByCreatedAtAsc(ServiceOrderStatus.PENDING));
    return out;
  }

  @Override
  public Map<String, Object> getManagerDashboard() {
    Map<String, Object> out = new HashMap<>();
    List<Invoice> invoices = invoiceRepository.findAll();
    double totalRevenue = 0;
    for (Invoice invoice : invoices) {
      totalRevenue += invoice.getTotal();
    }

    int lowStock = 0;
    for (InventoryItem item : inventoryItemRepository.findAll()) {
      if (item.getCurrentStock() <= item.getReorderLevel()) {
        lowStock++;
      }
    }

    int totalRooms = roomRepository.findAll().size();
    int occupied = roomRepository.findByStatus(RoomStatus.OCCUPIED).size();
    double occupancyRate = totalRooms == 0 ? 0 : ((double) occupied / totalRooms) * 100;

    out.put("totalRevenue", totalRevenue);
    out.put("invoiceCount", invoices.size());
    out.put("lowStockItems", lowStock);
    out.put("occupancyRate", occupancyRate);
    return out;
  }

  @Override
  public Map<String, Object> getGuestDashboard(String guestId) {
    Map<String, Object> out = new HashMap<>();
    List<Booking> bookings = bookingRepository.findByGuestId(guestId);
    out.put("bookings", bookings);

    double totalOutstanding = 0;
    for (Booking booking : bookings) {
      Folio folio = folioRepository.findByBookingId(booking.getId()).orElse(null);
      if (folio != null) {
        totalOutstanding += folio.getTotal();
      }
    }
    out.put("runningBill", totalOutstanding);
    return out;
  }
}
