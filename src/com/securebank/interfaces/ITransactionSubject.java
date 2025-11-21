package com.securebank.interfaces;

public interface ITransactionSubject {
  void attach(IBankObserver observer); // Suscribir
  void detach(IBankObserver observer); // Desuscribir
  void notifyObservers(String data);   // Avisar a todos
}