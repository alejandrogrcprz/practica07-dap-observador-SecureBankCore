package com.securebank.models;

import java.util.HashMap;
import java.util.Map;

public class AccountDTO {
  public String iban;
  public String ownerName;
  // Mapa para soportar: "EUR" -> 1500.0, "BTC" -> 0.005
  public Map<String, Double> balances = new HashMap<>();

  public AccountDTO(String iban, String ownerName, double initialEur) {
    this.iban = iban;
    this.ownerName = ownerName;
    this.balances.put("EUR", initialEur);
  }
}