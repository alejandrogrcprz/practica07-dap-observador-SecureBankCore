package com.securebank.core;

import com.securebank.interfaces.IBankObserver;
import com.securebank.interfaces.ITransactionSubject;
import java.util.ArrayList;
import java.util.List;

public class TransactionEngine implements ITransactionSubject {

  private List<IBankObserver> observers = new ArrayList<>();

  @Override
  public void attach(IBankObserver observer) {
    observers.add(observer);
  }

  @Override
  public void detach(IBankObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers(String data) {
    for (IBankObserver observer : observers) {
      observer.onTransactionExecuted(data);
    }
  }

  // LÓGICA DE NEGOCIO (MOVIMIENTO DE DINERO)
  public void executeTransfer(double amount, String sourceAccount, String details) {
    // Aquí iría la lógica real de mover dinero en BBDD...

    // Notificamos el ÉXITO
    String transactionData = "TRANSFERENCIA | " + sourceAccount + " -> " + details + " | IMPORTE: " + amount + "€";
    notifyObservers(transactionData);
  }

  // --- NUEVO: MÉTODO PARA REPORTAR ERRORES SIN MOVER DINERO ---
  public void reportIncident(String type, String details) {
    // Construimos un mensaje de evento
    String eventData = type + " | " + details;

    // Notificamos a los observadores (Dashboard, Logs, etc.)
    notifyObservers(eventData);
  }
}