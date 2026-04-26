package com.cs5500.NEUEat.model.hotelflow;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "hf_service_orders")
public class ServiceOrder {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(updatable = false, nullable = false, length = 36)
  private String id;

  private String bookingId;
  private String roomId;
  private String guestId;
  private LocalDateTime createdAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "hf_service_order_items", joinColumns = @JoinColumn(name = "service_order_id"))
  private List<ServiceItem> items;

  private double totalAmount;
  private boolean postedToFolio;

  @Enumerated(EnumType.STRING)
  private ServiceOrderStatus status;

  public ServiceOrder() {
    this.createdAt = LocalDateTime.now();
    this.status = ServiceOrderStatus.PENDING;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getGuestId() {
    return guestId;
  }

  public void setGuestId(String guestId) {
    this.guestId = guestId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public List<ServiceItem> getItems() {
    return items;
  }

  public void setItems(List<ServiceItem> items) {
    this.items = items;
  }

  public double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(double totalAmount) {
    this.totalAmount = totalAmount;
  }

  public boolean isPostedToFolio() {
    return postedToFolio;
  }

  public void setPostedToFolio(boolean postedToFolio) {
    this.postedToFolio = postedToFolio;
  }

  public ServiceOrderStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceOrderStatus status) {
    this.status = status;
  }
}
