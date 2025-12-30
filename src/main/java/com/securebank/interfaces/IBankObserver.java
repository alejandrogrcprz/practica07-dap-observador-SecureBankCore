package com.securebank.interfaces;

import com.securebank.models.Transaction;

public interface IBankObserver {
  /**
   * Se ejecuta cuando se intenta realizar una transacción.
   * @param tx El objeto con todos los datos (origen, destino, cantidad).
   * @throws SecurityException Si el observador detecta un fraude, lanza esto para bloquear.
   */
  void onTransactionAttempt(Transaction tx) throws SecurityException;
}