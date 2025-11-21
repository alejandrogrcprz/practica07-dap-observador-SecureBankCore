package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.observers.*;

public class MainApp {
  public static void main(String[] args) {
    System.out.println("=== INICIANDO SISTEMA SECUREBANK CORE ===");

    // 1. Instanciar el Sujeto (El Motor Bancario)
    TransactionEngine secureBankEngine = new TransactionEngine();

    // 2. Instanciar los Observadores (Los sistemas reactivos)
    // Aquí es donde entra tu "API simulada" (FraudDetector) y los demás
    FraudDetectorAI fraudDetector = new FraudDetectorAI();
    AuditLogger logger = new AuditLogger();
    NotificationService notifier = new NotificationService();
    GeneralLedger ledger = new GeneralLedger();

    // 3. Suscribir los observadores al motor (Conectamos los cables)
    secureBankEngine.attach(fraudDetector);
    secureBankEngine.attach(logger);
    secureBankEngine.attach(notifier);
    secureBankEngine.attach(ledger);

    // 4. EJECUCIÓN: El Cliente ordena la transferencia
    // El cliente no sabe qué pasa por detrás, solo pide mover dinero.
    secureBankEngine.executeTransfer(5000.00, "ES21-0000-1234", "ES99-9999-8888");

    System.out.println("\n=== SISTEMA FINALIZADO CORRECTAMENTE ===");
  }
}