package com.securebank.observers;
import com.securebank.interfaces.IBankObserver;

public class NotificationService implements IBankObserver {
  @Override
  public void onTransactionExecuted(String transactionData) {
    System.out.println("   -> [NOTIFICACIÓN] Enviando SMS al cliente: 'Su transferencia se ha realizado'.");
  }
}