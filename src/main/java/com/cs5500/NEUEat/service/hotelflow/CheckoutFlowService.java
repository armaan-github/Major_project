package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.exception.InvalidBookingStateException;
import com.cs5500.NEUEat.model.hotelflow.Invoice;

public interface CheckoutFlowService {

  Invoice checkout(String bookingId)
      throws BookingNotFoundException, FolioNotFoundException, InvalidBookingStateException;

  Invoice settleInvoice(String invoiceId, double paymentAmount)
      throws BookingNotFoundException;
}
