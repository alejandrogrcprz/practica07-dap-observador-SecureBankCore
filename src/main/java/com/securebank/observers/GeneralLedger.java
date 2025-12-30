package com.securebank.observers;

import com.securebank.interfaces.IBankObserver;
import com.securebank.main.BankAdminConsole;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;

@Component
public class GeneralLedger implements IBankObserver {
  private BankAdminConsole console;

  public GeneralLedger(BankAdminConsole console) {
    this.console = console;
  }

  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Construimos un string único con los datos de la transacción para el Hash
    String rawData = tx.getSourceIban() + tx.getDestIban() + tx.getAmount() + tx.getConcept();
    String hash = calculateSHA256(rawData);

    // Lo mandamos a la consola visual
    console.addLedgerEntry(rawData, hash);
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
      return "ERROR-HASH";
    }
  }
}