package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.exception.BookingNotFoundException;
import com.cs5500.NEUEat.exception.FolioNotFoundException;
import com.cs5500.NEUEat.model.hotelflow.Folio;
import com.cs5500.NEUEat.model.hotelflow.FolioLine;

public interface FolioFlowService {

  Folio addCharge(String bookingId, FolioLine line)
      throws FolioNotFoundException;

  Folio getFolioByBookingId(String bookingId)
      throws FolioNotFoundException;

  Folio ensureFolio(String bookingId)
      throws BookingNotFoundException;
}
