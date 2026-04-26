package com.cs5500.NEUEat.model.hotelflow;

import java.time.LocalDateTime;
import javax.persistence.Embeddable;

@Embeddable
public class FolioLine {

  private String category;
  private String referenceId;
  private String description;
  private double amount;
  private LocalDateTime postedAt;

  public FolioLine() {
  }

  public FolioLine(String category, String referenceId, String description, double amount,
      LocalDateTime postedAt) {
    this.category = category;
    this.referenceId = referenceId;
    this.description = description;
    this.amount = amount;
    this.postedAt = postedAt;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public LocalDateTime getPostedAt() {
    return postedAt;
  }

  public void setPostedAt(LocalDateTime postedAt) {
    this.postedAt = postedAt;
  }
}
