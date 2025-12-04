package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;

public class FraudDetectorAI implements IBankObserver {
  private BankAdminConsole console;

  public FraudDetectorAI(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionExecuted(String data) {
    // LÓGICA: Analizar si está bloqueado
    boolean isFraud = data.contains("BLOQUEADO");

    // VISUAL: Llamamos al método CORRECTO de la consola
    console.animateFraudCheck(isFraud);
  }
}