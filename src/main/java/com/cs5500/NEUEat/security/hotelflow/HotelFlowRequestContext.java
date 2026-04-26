package com.cs5500.NEUEat.security.hotelflow;

public final class HotelFlowRequestContext {

  private static final ThreadLocal<HotelFlowSession> CURRENT = new ThreadLocal<>();

  private HotelFlowRequestContext() {
  }

  public static void set(HotelFlowSession session) {
    CURRENT.set(session);
  }

  public static HotelFlowSession get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
