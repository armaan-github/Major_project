package com.cs5500.NEUEat.model.hotelflow;

import com.cs5500.NEUEat.model.User;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "hf_guests")
public class Guest extends User {

  @Embedded
  private GuestProfile profile;

  public Guest() {
    this.setType("guest");
  }

  public Guest(String userName, String password, String phoneNumber, String address,
      String city, String state, String zip) {
    super(userName, password, phoneNumber, address, city, state, zip);
    this.setType("guest");
  }

  public GuestProfile getProfile() {
    return profile;
  }

  public void setProfile(GuestProfile profile) {
    this.profile = profile;
  }
}
