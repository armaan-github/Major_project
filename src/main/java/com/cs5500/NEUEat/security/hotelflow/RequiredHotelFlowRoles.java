package com.cs5500.NEUEat.security.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredHotelFlowRoles {

  HotelFlowRole[] value();
}
