package com.securebank.observers;
import com.securebank.interfaces.IBankObserver;

public class AuditLogger implements IBankObserver {
  @Override
  public void onTransactionExecuted(String transactionData) {
    System.out.println("   -> [AUDITORÍA] Escribiendo registro inmutable en disco...");
    System.out.println("   -> [AUDITORÍA] Datos guardados: " + transactionData);
  }
}