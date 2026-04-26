package com.cs5500.NEUEat.service.hotelflow;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class HotelFlowEventServiceImpl implements HotelFlowEventService {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @Override
  public SseEmitter subscribe(String channel) {
    SseEmitter emitter = new SseEmitter(0L);
    emitters.add(emitter);

    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((ex) -> emitters.remove(emitter));

    try {
      emitter.send(SseEmitter.event().name("connected").data("subscribed:" + channel));
    } catch (IOException e) {
      emitters.remove(emitter);
    }

    return emitter;
  }

  @Override
  public void publish(String eventType, Object payload) {
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name(eventType).data(payload));
      } catch (IOException e) {
        emitter.complete();
        emitters.remove(emitter);
      }
    }
  }
}
