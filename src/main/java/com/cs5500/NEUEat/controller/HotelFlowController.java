package com.cs5500.NEUEat.controller;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.exception.RoomNotAvailableException;
import com.cs5500.NEUEat.model.hotelflow.Booking;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.Guest;
import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.model.hotelflow.GuestProfile;
import com.cs5500.NEUEat.model.hotelflow.InventoryItem;
import com.cs5500.NEUEat.model.hotelflow.Invoice;
import com.cs5500.NEUEat.model.hotelflow.Room;
import com.cs5500.NEUEat.model.hotelflow.RoomStatus;
import com.cs5500.NEUEat.model.hotelflow.ServiceItem;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrder;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrderStatus;
import com.cs5500.NEUEat.repository.hotelflow.GuestRepository;
import com.cs5500.NEUEat.repository.hotelflow.InventoryItemRepository;
import com.cs5500.NEUEat.repository.hotelflow.RoomRepository;
import com.cs5500.NEUEat.security.hotelflow.HotelFlowSession;
import com.cs5500.NEUEat.security.hotelflow.RequiredHotelFlowRoles;
import com.cs5500.NEUEat.service.PasswordService;
import com.cs5500.NEUEat.service.hotelflow.BookingFlowService;
import com.cs5500.NEUEat.service.hotelflow.CheckoutFlowService;
import com.cs5500.NEUEat.service.hotelflow.FolioFlowService;
import com.cs5500.NEUEat.service.hotelflow.HotelFlowAuthService;
import com.cs5500.NEUEat.service.hotelflow.HotelFlowDashboardService;
import com.cs5500.NEUEat.service.hotelflow.HotelFlowEventService;
import com.cs5500.NEUEat.service.hotelflow.PricingService;
import com.cs5500.NEUEat.service.hotelflow.ServiceOrderFlowService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/hotelflow")
public class HotelFlowController {

  private final BookingFlowService bookingFlowService;
  private final FolioFlowService folioFlowService;
  private final ServiceOrderFlowService serviceOrderFlowService;
  private final CheckoutFlowService checkoutFlowService;
  private final HotelFlowAuthService hotelFlowAuthService;
  private final HotelFlowDashboardService hotelFlowDashboardService;
  private final HotelFlowEventService hotelFlowEventService;
  private final PricingService pricingService;
  private final GuestRepository guestRepository;
  private final RoomRepository roomRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final PasswordService passwordService;

  @Autowired
  public HotelFlowController(BookingFlowService bookingFlowService, FolioFlowService folioFlowService,
      ServiceOrderFlowService serviceOrderFlowService, CheckoutFlowService checkoutFlowService,
      HotelFlowAuthService hotelFlowAuthService, HotelFlowDashboardService hotelFlowDashboardService,
      HotelFlowEventService hotelFlowEventService, PricingService pricingService,
      GuestRepository guestRepository, RoomRepository roomRepository,
      InventoryItemRepository inventoryItemRepository) {
    this.bookingFlowService = bookingFlowService;
    this.folioFlowService = folioFlowService;
    this.serviceOrderFlowService = serviceOrderFlowService;
    this.checkoutFlowService = checkoutFlowService;
    this.hotelFlowAuthService = hotelFlowAuthService;
    this.hotelFlowDashboardService = hotelFlowDashboardService;
    this.hotelFlowEventService = hotelFlowEventService;
    this.pricingService = pricingService;
    this.guestRepository = guestRepository;
    this.roomRepository = roomRepository;
    this.inventoryItemRepository = inventoryItemRepository;
    this.passwordService = new PasswordService();
  }

  @PostMapping(path = "/auth/staff-login")
  public Map<String, String> staffLogin(@RequestBody String jsonLogin) {
    JSONObject object = new JSONObject(jsonLogin);
    HotelFlowSession session = hotelFlowAuthService.loginStaff(
        object.getString("userName"), object.getString("password"));
    if (session == null) {
      throw new IllegalArgumentException("Invalid staff credentials");
    }
    Map<String, String> out = new HashMap<>();
    out.put("token", session.getToken());
    out.put("userId", session.getUserId());
    out.put("role", session.getRole().name());
    return out;
  }

  @PostMapping(path = "/auth/guest-login")
  public Map<String, String> guestLogin(@RequestBody String jsonLogin) {
    JSONObject object = new JSONObject(jsonLogin);
    HotelFlowSession session = hotelFlowAuthService.loginGuest(
        object.getString("userName"), object.getString("password"));
    if (session == null) {
      throw new IllegalArgumentException("Invalid guest credentials");
    }
    Map<String, String> out = new HashMap<>();
    out.put("token", session.getToken());
    out.put("userId", session.getUserId());
    out.put("role", session.getRole().name());
    return out;
  }

  @RequiredHotelFlowRoles({HotelFlowRole.GUEST, HotelFlowRole.RECEPTION, HotelFlowRole.KITCHEN,
      HotelFlowRole.MANAGER})
  @GetMapping(path = "/events/subscribe")
  public SseEmitter subscribe(@RequestParam(name = "channel", defaultValue = "all") String channel) {
    return hotelFlowEventService.subscribe(channel);
  }

  @PostMapping(path = "/guest/register")
  public Guest registerGuest(@RequestBody String jsonGuest) {
    JSONObject object = new JSONObject(jsonGuest);

    Guest guest = new Guest(
        object.getString("userName"),
        passwordService.generatePassword(object.getString("password")),
        object.getString("phoneNumber"),
        object.getString("address"),
        object.getString("city"),
        object.getString("state"),
        object.getString("zip"));

    GuestProfile profile = new GuestProfile(
        object.optString("roomPreference", ""),
        object.optString("foodAllergies", ""),
        object.optString("notes", ""));
    guest.setProfile(profile);

    return guestRepository.save(guest);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.MANAGER})
  @PostMapping(path = "/room/create")
  public Room createRoom(@RequestBody String jsonRoom) {
    JSONObject object = new JSONObject(jsonRoom);
    Room room = new Room();
    room.setRoomNumber(object.getString("roomNumber"));
    room.setRoomType(object.getString("roomType"));
    room.setCapacity(object.getInt("capacity"));
    room.setNightlyRate(object.getDouble("nightlyRate"));
    room.setStatus(RoomStatus.AVAILABLE);
    return roomRepository.save(room);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/room/available")
  public List<RoomQuoteView> getAvailableRooms() {
    List<RoomQuoteView> availableRooms = new ArrayList<>();
    for (Room room : roomRepository.findByStatus(RoomStatus.AVAILABLE)) {
      availableRooms.add(new RoomQuoteView(room, pricingService.calculateQuote(room)));
    }
    return availableRooms;
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @PatchMapping(path = "/room/{id}/status")
  public Room updateRoomStatus(@PathVariable("id") String roomId, @RequestBody String jsonRoom)
      throws BookingNotFoundException {
    Room room = roomRepository.findById(roomId)
        .orElseThrow(() -> new BookingNotFoundException("Room not found"));

    JSONObject object = new JSONObject(jsonRoom);
    RoomStatus targetStatus = RoomStatus.valueOf(object.getString("status"));
    if (!isValidRoomStatusTransition(room.getStatus(), targetStatus)) {
      throw new IllegalArgumentException("Invalid room status transition");
    }

    room.setStatus(targetStatus);
    Room savedRoom = roomRepository.save(room);
    hotelFlowEventService.publish("room.status.changed", savedRoom);
    return savedRoom;
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION})
  @PostMapping(path = "/booking/create")
  public Booking createBooking(@RequestBody String jsonBooking) throws RoomNotAvailableException {
    JSONObject object = new JSONObject(jsonBooking);
    String guestId = object.getString("guestId");
    String roomId = object.getString("roomId");
    LocalDate checkInDate = LocalDate.parse(object.getString("checkInDate"));
    LocalDate checkOutDate = LocalDate.parse(object.getString("checkOutDate"));
    return bookingFlowService.createBooking(guestId, roomId, checkInDate, checkOutDate);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION})
  @PostMapping(path = "/booking/checkin")
  public Booking checkIn(@RequestBody String jsonBooking)
      throws BookingNotFoundException, InvalidBookingStateException, FolioNotFoundException {
    JSONObject object = new JSONObject(jsonBooking);
    String bookingId = object.getString("bookingId");
    return bookingFlowService.checkIn(bookingId);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/booking/search")
  public List<Booking> searchBookings(@RequestParam(name = "query") String query) {
    return bookingFlowService.quickSearch(query);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.GUEST, HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/booking/guest/{guestId}")
  public List<Booking> getGuestBookings(@PathVariable("guestId") String guestId) {
    return bookingFlowService.getGuestBookings(guestId);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.GUEST})
  @PostMapping(path = "/order/qr")
  public ServiceOrder placeQrOrder(@RequestBody String jsonOrder)
      throws BookingNotFoundException, InvalidBookingStateException, FolioNotFoundException {
    JSONObject object = new JSONObject(jsonOrder);
    String bookingId = object.getString("bookingId");
    JSONArray itemsArray = object.getJSONArray("items");

    List<ServiceItem> items = new ArrayList<>();
    for (int i = 0; i < itemsArray.length(); i++) {
      JSONObject itemObj = itemsArray.getJSONObject(i);
      ServiceItem item = new ServiceItem(
          itemObj.getString("itemName"),
          itemObj.getInt("quantity"),
          itemObj.getDouble("unitPrice"));
      items.add(item);
    }

    return serviceOrderFlowService.placeQrOrder(bookingId, items);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.KITCHEN, HotelFlowRole.MANAGER})
  @PostMapping(path = "/order/status")
  public ServiceOrder updateOrderStatus(@RequestBody String jsonOrder)
      throws BookingNotFoundException {
    JSONObject object = new JSONObject(jsonOrder);
    String orderId = object.getString("orderId");
    ServiceOrderStatus status = ServiceOrderStatus.valueOf(object.getString("status"));
    return serviceOrderFlowService.updateOrderStatus(orderId, status);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.KITCHEN, HotelFlowRole.MANAGER})
  @GetMapping(path = "/kds/queue")
  public List<ServiceOrder> getKitchenQueue() {
    return serviceOrderFlowService.getKitchenQueue();
  }

  @RequiredHotelFlowRoles({HotelFlowRole.GUEST, HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/folio/{bookingId}")
  public Folio getFolio(@PathVariable("bookingId") String bookingId) throws FolioNotFoundException {
    return folioFlowService.getFolioByBookingId(bookingId);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @PostMapping(path = "/checkout")
  public Invoice checkout(@RequestBody String jsonCheckout)
      throws BookingNotFoundException, FolioNotFoundException, InvalidBookingStateException {
    JSONObject object = new JSONObject(jsonCheckout);
    String bookingId = object.getString("bookingId");
    return checkoutFlowService.checkout(bookingId);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @PostMapping(path = "/invoice/pay")
  public Invoice payInvoice(@RequestBody String jsonPayment)
      throws BookingNotFoundException {
    JSONObject object = new JSONObject(jsonPayment);
    String invoiceId = object.getString("invoiceId");
    double amount = object.getDouble("amount");
    return checkoutFlowService.settleInvoice(invoiceId, amount);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.MANAGER})
  @PostMapping(path = "/inventory/item")
  public InventoryItem addInventoryItem(@RequestBody String jsonItem) {
    JSONObject object = new JSONObject(jsonItem);
    InventoryItem item = new InventoryItem(
        object.getString("itemName"),
        object.getInt("currentStock"),
        object.getInt("reorderLevel"));
    InventoryItem saved = inventoryItemRepository.save(item);
    if (saved.getCurrentStock() <= saved.getReorderLevel()) {
      hotelFlowEventService.publish("inventory.low_stock", saved);
    }
    return saved;
  }

  @RequiredHotelFlowRoles({HotelFlowRole.MANAGER, HotelFlowRole.KITCHEN})
  @GetMapping(path = "/inventory/low-stock")
  public List<InventoryItem> getLowStockItems() {
    List<InventoryItem> all = inventoryItemRepository.findAll();
    List<InventoryItem> low = new ArrayList<>();
    for (InventoryItem item : all) {
      if (item.getCurrentStock() <= item.getReorderLevel()) {
        low.add(item);
      }
    }
    return low;
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/dashboard/reception")
  public Map<String, Object> receptionDashboard() {
    return hotelFlowDashboardService.getReceptionDashboard();
  }

  @RequiredHotelFlowRoles({HotelFlowRole.KITCHEN, HotelFlowRole.MANAGER})
  @GetMapping(path = "/dashboard/kitchen")
  public Map<String, Object> kitchenDashboard() {
    return hotelFlowDashboardService.getKitchenDashboard();
  }

  @RequiredHotelFlowRoles({HotelFlowRole.MANAGER})
  @GetMapping(path = "/dashboard/manager")
  public Map<String, Object> managerDashboard() {
    return hotelFlowDashboardService.getManagerDashboard();
  }

  @RequiredHotelFlowRoles({HotelFlowRole.GUEST, HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/dashboard/guest/{guestId}")
  public Map<String, Object> guestDashboard(@PathVariable("guestId") String guestId) {
    return hotelFlowDashboardService.getGuestDashboard(guestId);
  }

  @RequiredHotelFlowRoles({HotelFlowRole.RECEPTION, HotelFlowRole.MANAGER})
  @GetMapping(path = "/room/status-summary")
  public Map<String, Object> getRoomStatusSummary() {
    Map<String, Object> out = new HashMap<>();
    out.put("totalRooms", roomRepository.count());

    Map<RoomStatus, List<String>> grouped = new HashMap<>();
    for (RoomStatus status : RoomStatus.values()) {
      grouped.put(status, new ArrayList<>());
    }
    for (Room room : roomRepository.findAll()) {
      grouped.get(room.getStatus()).add(room.getRoomNumber());
    }

    out.put("available", grouped.get(RoomStatus.AVAILABLE));
    out.put("reserved", grouped.get(RoomStatus.RESERVED));
    out.put("occupied", grouped.get(RoomStatus.OCCUPIED));
    out.put("dirty", grouped.get(RoomStatus.DIRTY));
    out.put("cleaningInProgress", grouped.get(RoomStatus.CLEANING_IN_PROGRESS));
    out.put("outOfService", grouped.get(RoomStatus.OUT_OF_SERVICE));
    return out;
  }

  private boolean isValidRoomStatusTransition(RoomStatus currentStatus, RoomStatus targetStatus) {
    if (currentStatus == targetStatus) {
      return true;
    }

    if (currentStatus == RoomStatus.DIRTY && targetStatus == RoomStatus.CLEANING_IN_PROGRESS) {
      return true;
    }

    if (currentStatus == RoomStatus.CLEANING_IN_PROGRESS && targetStatus == RoomStatus.AVAILABLE) {
      return true;
    }

    if (currentStatus == RoomStatus.DIRTY && targetStatus == RoomStatus.AVAILABLE) {
      return true;
    }

    return currentStatus == RoomStatus.OUT_OF_SERVICE && targetStatus == RoomStatus.CLEANING_IN_PROGRESS;
  }

  private static class RoomQuoteView {
    private final String id;
    private final String roomNumber;
    private final String roomType;
    private final int capacity;
    private final double nightlyRate;
    private final RoomStatus status;
    private final double currentQuote;

    RoomQuoteView(Room room, double currentQuote) {
      this.id = room.getId();
      this.roomNumber = room.getRoomNumber();
      this.roomType = room.getRoomType();
      this.capacity = room.getCapacity();
      this.nightlyRate = room.getNightlyRate();
      this.status = room.getStatus();
      this.currentQuote = currentQuote;
    }

    public String getId() {
      return id;
    }

    public String getRoomNumber() {
      return roomNumber;
    }

    public String getRoomType() {
      return roomType;
    }

    public int getCapacity() {
      return capacity;
    }

    public double getNightlyRate() {
      return nightlyRate;
    }

    public RoomStatus getStatus() {
      return status;
    }

    public double getCurrentQuote() {
      return currentQuote;
    }
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
      RoomNotAvailableException.class,
      BookingNotFoundException.class,
      FolioNotFoundException.class,
      InvalidBookingStateException.class,
      IllegalArgumentException.class
  })
  public String handleException(Exception e) {
    return e.getMessage();
  }
}
