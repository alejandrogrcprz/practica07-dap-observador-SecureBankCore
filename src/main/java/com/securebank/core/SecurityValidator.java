package com.securebank.core;

import com.securebank.main.IBankRepository;
import com.securebank.models.TransactionRequest;
import java.util.List;

public class SecurityValidator {
  private IBankRepository repository;
  private long lastTransferTimestamp = 0;

  public SecurityValidator(IBankRepository repository) {
    this.repository = repository;
  }

  public String validate(TransactionRequest request) {
    // 1. Velocity Check
    long currentTime = System.currentTimeMillis();
    if ((currentTime - lastTransferTimestamp) < 10000 && lastTransferTimestamp != 0) {
      return "STEP1_VELOCITY|Alta velocidad detectada";
    }

    // 2. Blacklist de conceptos (CSV)
    List<String> badWords = repository.getCsvBlockedConcepts();
    for (String word : badWords) {
      if (request.concept.toUpperCase().contains(word)) {
        return "STEP4_BLACKLIST|Concepto no permitido: " + word;
      }
    }

    // 3. Patrón numérico
    if (request.amount >= 1000 && (request.amount % 500 == 0)) {
      return "STEP2_PATTERN|Patrón de importe inusual";
    }

    return "OK"; // Pasa todas las pruebas
  }

  public void updateTimestamp() {
    this.lastTransferTimestamp = System.currentTimeMillis();
  }
}