package com.securebank.strategies;

public class StandardCommission implements CommissionStrategy {
  private static final double COMMISSION_RATE = 0.015; // 1.5%

  @Override
  public double applyCommission(double amount) {
    return amount * (1 - COMMISSION_RATE); // Devuelve el 98.5% del dinero
  }

  @Override
  public String getDescription() {
    return "Comisión Estándar (1.5%)";
  }
}