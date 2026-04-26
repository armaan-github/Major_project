package com.cs5500.NEUEat.model.hotelflow;

import javax.persistence.Embeddable;

@Embeddable
public class ServiceItem {

  private String itemName;
  private int quantity;
  private double unitPrice;

  public ServiceItem() {
  }

  public ServiceItem(String itemName, int quantity, double unitPrice) {
    this.itemName = itemName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public double getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(double unitPrice) {
    this.unitPrice = unitPrice;
  }

  public double getLineTotal() {
    return quantity * unitPrice;
  }
}
