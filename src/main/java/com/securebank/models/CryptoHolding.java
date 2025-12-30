package com.securebank.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CryptoHolding {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String symbol;
  private double amount;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User owner;

  public CryptoHolding() {}
  public CryptoHolding(String symbol, double amount, User owner) {
    this.symbol = symbol;
    this.amount = amount;
    this.owner = owner;
  }

  public String getSymbol() { return symbol; }
  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }
}