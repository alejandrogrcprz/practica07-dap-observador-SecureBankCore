package com.securebank.interfaces;

public interface IBankObserver {
  // El contrato que todos los observadores deben cumplir
  void onTransactionExecuted(String transactionData);
}