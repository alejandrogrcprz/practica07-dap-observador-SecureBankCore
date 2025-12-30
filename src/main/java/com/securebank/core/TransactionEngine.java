package com.securebank.core;

import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionEngine {
  private List<IBankObserver> observers = new ArrayList<>();

  public void attach(IBankObserver observer) {
    observers.add(observer);
  }

  // Método actualizado para aceptar Transaction en lugar de String
  public void notifyObservers(Transaction tx) {
    for (IBankObserver observer : observers) {
      observer.onTransactionAttempt(tx);
    }
  }
}