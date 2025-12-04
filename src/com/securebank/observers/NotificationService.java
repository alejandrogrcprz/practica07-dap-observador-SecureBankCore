package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;

public class NotificationService implements IBankObserver {
  private BankAdminConsole console;

  public NotificationService(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionExecuted(String data) {
    // FILTRO DE SEGURIDAD:
    // Si el mensaje contiene "BLOQUEADO" (Fraude) o "FALLO" (Saldo),
    // este servicio NO debe hacer nada (no vamos a felicitar al cliente por un error).
    if (!data.contains("BLOQUEADO") && !data.contains("FALLO")) {
      // Solo si es una transferencia real, simulamos el envío
      console.logToSystem("NOTIFICACIÓN: Enviando SMS/Push de confirmación al cliente...");
    }
  }
}