package com.cs5500.NEUEat.model.hotelflow;

import javax.persistence.Embeddable;

@Embeddable
public class GuestProfile {

  private String roomPreference;
  private String foodAllergies;
  private String notes;

  public GuestProfile() {
  }

  public GuestProfile(String roomPreference, String foodAllergies, String notes) {
    this.roomPreference = roomPreference;
    this.foodAllergies = foodAllergies;
    this.notes = notes;
  }

  public String getRoomPreference() {
    return roomPreference;
  }

  public void setRoomPreference(String roomPreference) {
    this.roomPreference = roomPreference;
  }

  public String getFoodAllergies() {
    return foodAllergies;
  }

  public void setFoodAllergies(String foodAllergies) {
    this.foodAllergies = foodAllergies;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
