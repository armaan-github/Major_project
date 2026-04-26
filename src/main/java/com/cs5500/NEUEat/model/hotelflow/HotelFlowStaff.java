package com.cs5500.NEUEat.model.hotelflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "hf_staff")
public class HotelFlowStaff {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(updatable = false, nullable = false, length = 36)
  private String id;

  @Column(unique = true)
  private String userName;

  private String password;

  @Enumerated(EnumType.STRING)
  private HotelFlowRole role;

  public HotelFlowStaff() {
  }

  public HotelFlowStaff(String userName, String password, HotelFlowRole role) {
    this.userName = userName;
    this.password = password;
    this.role = role;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public HotelFlowRole getRole() {
    return role;
  }

  public void setRole(HotelFlowRole role) {
    this.role = role;
  }
}
