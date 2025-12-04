package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditLogger implements IBankObserver {
  private BankAdminConsole console;

  public AuditLogger(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionExecuted(String data) {
    // LÓGICA: Crear sello de tiempo
    String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    String logEntry = "> [" + timestamp + "] " + data;

    // VISUAL: Llamamos al método CORRECTO de la consola
    console.logToSystem(logEntry);
  }
}