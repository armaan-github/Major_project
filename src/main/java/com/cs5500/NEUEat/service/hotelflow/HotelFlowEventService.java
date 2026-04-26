package com.cs5500.NEUEat.service.hotelflow;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface HotelFlowEventService {

  SseEmitter subscribe(String channel);

  void publish(String eventType, Object payload);
}
