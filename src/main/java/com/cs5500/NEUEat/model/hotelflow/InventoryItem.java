package com.cs5500.NEUEat.model.hotelflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "hf_inventory_items")
public class InventoryItem {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(updatable = false, nullable = false, length = 36)
  private String id;

  @Column(unique = true)
  private String itemName;
  private int currentStock;
  private int reorderLevel;

  public InventoryItem() {
  }

  public InventoryItem(String itemName, int currentStock, int reorderLevel) {
    this.itemName = itemName;
    this.currentStock = currentStock;
    this.reorderLevel = reorderLevel;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public int getCurrentStock() {
    return currentStock;
  }

  public void setCurrentStock(int currentStock) {
    this.currentStock = currentStock;
  }

  public int getReorderLevel() {
    return reorderLevel;
  }

  public void setReorderLevel(int reorderLevel) {
    this.reorderLevel = reorderLevel;
  }
}
