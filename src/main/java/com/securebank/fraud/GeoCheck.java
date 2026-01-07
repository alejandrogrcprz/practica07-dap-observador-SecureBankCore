package com.securebank.fraud;

import com.securebank.models.Transaction;
import java.util.Arrays;
import java.util.List;

public class GeoCheck extends FraudCheck {

  // Lista de prefijos de países sancionados
  private static final List<String> FORBIDDEN_COUNTRIES = Arrays.asList("RU", "KP", "IR", "KY", "SY");

  @Override
  public void check(Transaction tx) throws SecurityException {
    String destIban = tx.getDestIban();

    // Solo comprobamos si hay un destino y es lo suficientemente largo
    if (destIban != null && destIban.length() >= 2) {
      String prefix = destIban.substring(0, 2).toUpperCase();

      if (FORBIDDEN_COUNTRIES.contains(prefix)) {
        throw new SecurityException("🌐 Destino no autorizado (" + prefix + ") por sanciones internacionales.");
      }
    }

    // Si todo está bien, pasamos al siguiente eslabón
    checkNext(tx);
  }
}