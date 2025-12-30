package com.securebank.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CryptoHolding {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String symbol; // BTC, ETH, SOL
  private double amount; // Cantidad de monedas (ej: 0.5 BTC)

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

  // Getters y Setters
  public String getSymbol() { return symbol; }
  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }
}