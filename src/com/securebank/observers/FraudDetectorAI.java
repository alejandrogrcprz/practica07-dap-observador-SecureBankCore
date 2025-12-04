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
    if (data.startsWith("BLOQUEADO")) {
      // Formato esperado: "BLOQUEADO | CÓDIGO_ERROR | Detalle..."
      // Ejemplo: "BLOQUEADO | STEP2_PATTERN | Patrón Numérico..."
      try {
        String[] parts = data.split("\\|");
        String errorCode = parts[1].trim(); // "STEP2_PATTERN"
        String detail = parts[2].trim();    // "Patrón Numérico..."

        // Mandamos a la consola el código del paso que falló
        console.animateFraudCheck(errorCode);
        console.logToSystem("🚨 FRAUD DETECTED: " + detail);

      } catch (Exception e) {
        // Fallback por si el formato no es el esperado
        console.animateFraudCheck("UNKNOWN");
      }
    }
    else if (data.startsWith("TX##")) {
      // Transacción legítima: Pasó todos los controles -> "OK"
      console.animateFraudCheck("OK");
    }
  }
}