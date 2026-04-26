package com.cs5500.NEUEat.repository.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Invoice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

  Optional<Invoice> findByBookingId(String bookingId);
}
