package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.model.hotelflow.ServiceItem;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrder;
import com.cs5500.NEUEat.model.hotelflow.ServiceOrderStatus;
import java.util.List;

public interface ServiceOrderFlowService {

  ServiceOrder placeQrOrder(String bookingId, List<ServiceItem> items)
      throws BookingNotFoundException, InvalidBookingStateException, FolioNotFoundException;

  ServiceOrder updateOrderStatus(String orderId, ServiceOrderStatus status)
      throws BookingNotFoundException;

  List<ServiceOrder> getKitchenQueue();
}
