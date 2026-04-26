package com.cs5500.NEUEat.service.hotelflow;

import com.cs5500.NEUEat.model.hotelflow.Guest;
import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.model.hotelflow.HotelFlowStaff;
import com.cs5500.NEUEat.repository.hotelflow.GuestRepository;
import com.cs5500.NEUEat.repository.hotelflow.HotelFlowStaffRepository;
import com.cs5500.NEUEat.security.hotelflow.HotelFlowSession;
import com.cs5500.NEUEat.service.PasswordService;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelFlowAuthServiceImpl implements HotelFlowAuthService {

  @Autowired
  private GuestRepository guestRepository;

  @Autowired
  private HotelFlowStaffRepository hotelFlowStaffRepository;

  private final PasswordService passwordService = new PasswordService();
  private final ConcurrentMap<String, HotelFlowSession> sessions = new ConcurrentHashMap<>();

  @Override
  public HotelFlowSession loginGuest(String userName, String password) {
    for (Guest guest : guestRepository.findAll()) {
      if (guest.getUserName().equals(userName)
          && passwordService.passwordMatch(password, guest.getPassword())) {
        return createSession(guest.getId(), guest.getUserName(), HotelFlowRole.GUEST);
      }
    }
    return null;
  }

  @Override
  public HotelFlowSession loginStaff(String userName, String password) {
    Optional<HotelFlowStaff> staffOptional = hotelFlowStaffRepository.findByUserName(userName);
    if (staffOptional.isEmpty()) {
      return null;
    }

    HotelFlowStaff staff = staffOptional.get();
    if (!passwordService.passwordMatch(password, staff.getPassword())) {
      return null;
    }

    return createSession(staff.getId(), staff.getUserName(), staff.getRole());
  }

  @Override
  public Optional<HotelFlowSession> findByToken(String token) {
    return Optional.ofNullable(sessions.get(token));
  }

  @Override
  public boolean hasRequiredRole(HotelFlowSession session, HotelFlowRole[] roles) {
    for (HotelFlowRole role : roles) {
      if (session.getRole() == role) {
        return true;
      }
    }
    return false;
  }

  private HotelFlowSession createSession(String userId, String userName, HotelFlowRole role) {
    String token = UUID.randomUUID().toString();
    HotelFlowSession session = new HotelFlowSession(token, userId, userName, role);
    sessions.put(token, session);
    return session;
  }
}
