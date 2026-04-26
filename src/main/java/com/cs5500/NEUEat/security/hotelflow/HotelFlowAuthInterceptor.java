package com.cs5500.NEUEat.security.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.service.hotelflow.HotelFlowAuthService;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HotelFlowAuthInterceptor implements HandlerInterceptor {

  @Autowired
  private HotelFlowAuthService hotelFlowAuthService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    RequiredHotelFlowRoles requiredRoles = handlerMethod.getMethodAnnotation(RequiredHotelFlowRoles.class);

    if (requiredRoles == null) {
      requiredRoles = handlerMethod.getBeanType().getAnnotation(RequiredHotelFlowRoles.class);
    }

    if (requiredRoles == null) {
      return true;
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
      return false;
    }

    String token = authHeader.substring("Bearer ".length());
    Optional<HotelFlowSession> sessionOptional = hotelFlowAuthService.findByToken(token);
    if (sessionOptional.isEmpty()) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
      return false;
    }

    HotelFlowSession session = sessionOptional.get();
    HotelFlowRole[] allowed = requiredRoles.value();
    if (!hotelFlowAuthService.hasRequiredRole(session, allowed)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Role not allowed for this endpoint");
      return false;
    }

    HotelFlowRequestContext.set(session);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
      Exception ex) {
    HotelFlowRequestContext.clear();
  }
}
