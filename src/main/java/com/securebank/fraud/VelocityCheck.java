package com.securebank.fraud;

import com.securebank.models.Transaction;
import java.util.HashMap;
import java.util.Map;

public class VelocityCheck extends FraudCheck {

  // Memoria para guardar la última hora de operación de cada IBAN
  private final Map<String, Long> lastTime = new HashMap<>();

  // Umbral: 5 segundos (5000 ms)
  private static final long TIME_THRESHOLD = 5000;

  @Override
  public void check(Transaction tx) throws SecurityException {
    String iban = tx.getSourceIban();
    long now = System.currentTimeMillis();

    if (lastTime.containsKey(iban)) {
      long diff = now - lastTime.get(iban);

      if (diff < TIME_THRESHOLD) {
        throw new SecurityException("⚠️ Error 429: Solicitud duplicada. Sistema saturado, espere 5 segundos.");
      }
    }

    // Si pasa la validación, actualizamos la hora y seguimos
    lastTime.put(iban, now);
    checkNext(tx);
  }
}