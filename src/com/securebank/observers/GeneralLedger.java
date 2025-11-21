package com.securebank.observers;
import com.securebank.interfaces.IBankObserver;

public class GeneralLedger implements IBankObserver {
  @Override
  public void onTransactionExecuted(String transactionData) {
    System.out.println("   -> [CONTABILIDAD] Actualizando Libro Mayor (Debe/Haber). Balance cuadrado.");
  }
}