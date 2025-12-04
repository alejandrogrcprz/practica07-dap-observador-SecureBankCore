package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.observers.*;
import javax.swing.*;

public class MainSystem {
  public static void main(String[] args) {
    TransactionEngine engine = new TransactionEngine();
    BankRepository repository = new BankRepository();

    // 1. Crear Ventanas
    BankAdminConsole adminWindow = new BankAdminConsole();
    MobilePhoneSimulator myPhone = new MobilePhoneSimulator(engine, repository);

    // 2. Crear Observadores (Inyectando la consola)
    FraudDetectorAI fraud = new FraudDetectorAI(adminWindow);
    AuditLogger logger = new AuditLogger(adminWindow);
    GeneralLedger ledger = new GeneralLedger(adminWindow);
    NotificationService notif = new NotificationService(adminWindow);

    // 3. Conectar todo al motor
    engine.attach(fraud);
    engine.attach(logger);
    engine.attach(ledger);
    engine.attach(notif);
    engine.attach(myPhone);
    engine.attach(adminWindow);

    // 4. Mostrar
    SwingUtilities.invokeLater(() -> {
      myPhone.setLocation(100, 100);
      adminWindow.setLocation(500, 100);
      myPhone.setVisible(true);
      adminWindow.setVisible(true);
    });
  }
}