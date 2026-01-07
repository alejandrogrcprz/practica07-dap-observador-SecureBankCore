package com.securebank.strategies;

public interface CommissionStrategy {
  // Recibe el importe original y devuelve el importe final (con comisión descontada)
  double applyCommission(double amount);

  // Método para saber de cuánto fue la comisión (opcional, para el historial)
  String getDescription();
}