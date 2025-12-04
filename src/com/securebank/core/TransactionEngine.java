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

  // Añadimos el parámetro sourceName
  public void executeTransfer(double amount, String sourceIban, String destIban, String benName, String concept, String sourceName) {

    // NUEVO PROTOCOLO V2 (Incluye Nombre del Emisor al final)
    // TX ## ORIGEN ## DESTINO ## CANTIDAD ## NOMBRE_BENEFICIARIO ## CONCEPTO ## NOMBRE_EMISOR
    String eventData = "TX##" + sourceIban + "##" + destIban + "##" + amount + "##" + benName + "##" + concept + "##" + sourceName;

    notifyObservers(eventData);
  }

  // --- NUEVO: MÉTODO PARA REPORTAR ERRORES SIN MOVER DINERO ---
  public void reportIncident(String type, String details) {
    // Construimos un mensaje de evento
    String eventData = type + " | " + details;

    // Notificamos a los observadores (Dashboard, Logs, etc.)
    notifyObservers(eventData);
  }
}