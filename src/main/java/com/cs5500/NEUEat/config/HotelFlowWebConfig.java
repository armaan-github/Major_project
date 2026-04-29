package com.cs5500.NEUEat.config;

import com.cs5500.NEUEat.security.hotelflow.HotelFlowAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HotelFlowWebConfig implements WebMvcConfigurer {

  @Autowired
  private HotelFlowAuthInterceptor hotelFlowAuthInterceptor;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/hotelflow/**")
        .allowedOrigins("http://localhost:4200")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(hotelFlowAuthInterceptor)
        .addPathPatterns("/api/hotelflow/**")
        .excludePathPatterns("/api/hotelflow/auth/**");
  }
}
