package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;
import com.securebank.models.Transaction; // Nuevo import
import org.springframework.stereotype.Component;

@Component
public class AuditLogger implements IBankObserver {
  private BankAdminConsole console;

  public AuditLogger(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Registramos la operación en la consola con el formato nuevo
    console.logToSystem("AUDIT: Intento de transferencia de " + tx.getAmount() + "€ detectado.");
  }
}