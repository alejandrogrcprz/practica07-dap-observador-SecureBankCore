package com.securebank.fraud;
import com.securebank.models.Transaction;
import java.util.Arrays;
import java.util.List;

public class BlacklistCheck extends FraudCheck {
  private static final List<String> BLACKLIST = Arrays.asList("DROGA", "ARMA", "SICARIO", "SOBORNO"); // ... resto de tu lista

  @Override
  public void check(Transaction tx) throws SecurityException {
    String concept = tx.getConcept().toUpperCase();
    for (String word : BLACKLIST) {
      if (concept.contains(word)) {
        throw new SecurityException("🚫 Operación denegada: Concepto prohibido (" + word + ").");
      }
    }
    checkNext(tx); // Pasa al siguiente eslabón
  }
}