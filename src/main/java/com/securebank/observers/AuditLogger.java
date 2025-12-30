package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger implements IBankObserver {

  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Registramos la operación en la consola del sistema en lugar de la ventana visual
    System.out.println("📋 AUDIT: Intento de transferencia de " + tx.getAmount() + "€ detectado. [Ref: " + tx.getId() + "]");
  }
}