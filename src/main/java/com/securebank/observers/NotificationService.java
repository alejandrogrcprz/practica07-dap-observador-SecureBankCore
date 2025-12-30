package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class NotificationService implements IBankObserver {
  private BankAdminConsole console;

  public NotificationService(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Simulamos el envío de un correo
    console.logToSystem("NOTIF: Enviando SMS de confirmación a " + tx.getSourceIban());
  }
}