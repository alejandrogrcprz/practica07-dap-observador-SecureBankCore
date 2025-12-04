package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;
import java.security.MessageDigest;

public class GeneralLedger implements IBankObserver {
  private BankAdminConsole console;

  public GeneralLedger(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionExecuted(String data) {
    // LÓGICA: Calcular Hash
    String hash = calculateSHA256(data);

    // VISUAL: Llamamos al método CORRECTO de la consola
    console.addLedgerEntry(data, hash);
  }

  private String calculateSHA256(String data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(data.getBytes("UTF-8"));
      StringBuilder hexString = new StringBuilder();
      for (byte b : encodedhash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      return "ERROR-CRYPTO";
    }
  }
}