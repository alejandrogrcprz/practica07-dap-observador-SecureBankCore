package com.securebank.fraud;
import com.securebank.models.Transaction;

public abstract class FraudCheck {
  protected FraudCheck next;

  public FraudCheck linkWith(FraudCheck next) {
    this.next = next;
    return next;
  }

  public abstract void check(Transaction tx) throws SecurityException;

  protected void checkNext(Transaction tx) {
    if (next != null) {
      next.check(tx);
    }
  }
}