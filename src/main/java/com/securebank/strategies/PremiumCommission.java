package com.securebank.strategies;

public class PremiumCommission implements CommissionStrategy {
  @Override
  public double applyCommission(double amount) {
    return amount; // No toca el dinero, devuelve lo mismo
  }

  @Override
  public String getDescription() {
    return "Tasa Premium (0%)";
  }
}