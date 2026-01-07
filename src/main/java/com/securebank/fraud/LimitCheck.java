package com.securebank.fraud;
import com.securebank.models.Transaction;

public class LimitCheck extends FraudCheck {
  @Override
  public void check(Transaction tx) throws SecurityException {
    if (tx.getAmount() > 10000) {
      throw new SecurityException("⛔ Límite excedido (Normativa 1024/2024).");
    }
    checkNext(tx);
  }
}