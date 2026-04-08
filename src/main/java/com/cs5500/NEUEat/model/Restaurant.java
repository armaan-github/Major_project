package com.cs5500.NEUEat.model;

import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "restaurants")
public class Restaurant extends User {
  @Embedded
  private RestaurantInfo information;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "restaurant_menu", joinColumns = @JoinColumn(name = "restaurant_id"))
  private List<Dish> menu;

  public Restaurant() {
    this.setType("restaurant");
  }

  public Restaurant(String userName, String password, String phoneNumber, String address,
      String city, String state, String zip, RestaurantInfo information,
      List<Dish> menu) {
    super(userName, password, phoneNumber, address, city, state, zip);
    this.setType("restaurant");
    this.information = information;
    this.menu = menu;
  }

  public Restaurant(String userName, String password, String phoneNumber, String address,
      String city, String state, String zip) {
    super(userName, password, phoneNumber, address, city, state, zip);
    this.setType("restaurant");
  }

  public RestaurantInfo getInformation() {
    return information;
  }

  public void setInformation(RestaurantInfo information) {
    this.information = information;
  }

  public List<Dish> getMenu() {
    return menu;
  }

  public void setMenu(List<Dish> menu) {
    this.menu = menu;
  }

  @Override
  public String toString() {
    return "Restaurant{" +
        "information=" + information +
        ", menu=" + menu +
        '}';
  }
}
