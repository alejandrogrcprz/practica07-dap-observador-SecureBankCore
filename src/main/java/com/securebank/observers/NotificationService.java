package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class NotificationService implements IBankObserver {

  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Simulamos el envío de un correo/SMS por consola
    System.out.println("🔔 NOTIF: Enviando SMS de confirmación a " + tx.getSourceIban());
  }
}