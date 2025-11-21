package com.securebank.core;

import com.securebank.interfaces.IBankObserver;
import com.securebank.interfaces.ITransactionSubject;
import java.util.ArrayList;
import java.util.List;

public class TransactionEngine implements ITransactionSubject {

  // Lista de suscriptores (Agregación - El rombo blanco del diagrama)
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
    // Recorremos la lista y avisamos a cada uno
    for (IBankObserver observer : observers) {
      observer.onTransactionExecuted(data);
    }
  }

  // LÓGICA DE NEGOCIO PURA
  public void executeTransfer(double amount, String sourceAccount, String destinationAccount) {
    System.out.println("\n--- INICIANDO TRANSACCIÓN ---");
    System.out.println("[MOTOR] Procesando transferencia de " + amount + "€...");
    System.out.println("[MOTOR] Validando saldo de " + sourceAccount + "...");
    System.out.println("[MOTOR] Moviendo dinero a " + destinationAccount + "...");

    // Simulamos un pequeño tiempo de procesamiento
    try { Thread.sleep(1000); } catch (InterruptedException e) { }

    System.out.println("[MOTOR] ¡Transferencia realizada con éxito!");

    // EL PASO CLAVE DEL PATRÓN:
    String transactionDetails = "TX_ID: " + System.currentTimeMillis() + " | " + sourceAccount + " -> " + destinationAccount + " | IMPORTE: " + amount + "€";

    System.out.println("[MOTOR] Notificando a los sistemas observadores...");
    notifyObservers(transactionDetails);
  }
}