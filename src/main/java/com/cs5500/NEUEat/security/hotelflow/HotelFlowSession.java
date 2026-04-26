package com.cs5500.NEUEat.security.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;

public class HotelFlowSession {

  private final String token;
  private final String userId;
  private final String userName;
  private final HotelFlowRole role;

  public HotelFlowSession(String token, String userId, String userName, HotelFlowRole role) {
    this.token = token;
    this.userId = userId;
    this.userName = userName;
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public HotelFlowRole getRole() {
    return role;
  }
}
