package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.observers.*;
import javax.swing.*;

public class MainSystem {
  public static void main(String[] args) {
    TransactionEngine engine = new TransactionEngine();

    // Si mañana creamos 'SqlBankRepository', solo cambiamos esta línea.
    IBankRepository repository = new BankRepository();

    // 1. Crear Ventanas
    BankAdminConsole adminWindow = new BankAdminConsole();

    // Fíjate que los móviles ya no se quejan, aceptan la interfaz
    MobilePhoneSimulator phone1 = new MobilePhoneSimulator(engine, repository);
    phone1.setTitle("Smartphone - Dispositivo A");

    MobilePhoneSimulator phone2 = new MobilePhoneSimulator(engine, repository);
    phone2.setTitle("Smartphone - Dispositivo B");

    // 2. Crear Observadores de Lógica (Backend)
    FraudDetectorAI fraud = new FraudDetectorAI(adminWindow);
    AuditLogger logger = new AuditLogger(adminWindow);
    GeneralLedger ledger = new GeneralLedger(adminWindow);
    NotificationService notif = new NotificationService(adminWindow);

    // 3. Conectar todo al motor (PATRÓN OBSERVADOR)
    engine.attach(fraud);
    engine.attach(logger);
    engine.attach(ledger);
    engine.attach(notif);

    // Conectamos los dos móviles.
    // Ambos recibirán TODAS las notificaciones (Broadcast),
    // pero cada uno filtrará internamente si el mensaje es para su usuario logueado.
    engine.attach(phone1);
    engine.attach(phone2);

    engine.attach(adminWindow);

    // 4. Mostrar y colocar en pantalla para la demo
    SwingUtilities.invokeLater(() -> {
      // Consola del Admin a la izquierda
      adminWindow.setLocation(50, 50);
      adminWindow.setVisible(true);

      // Móvil 1 en el centro
      phone1.setLocation(900, 100);
      phone1.setVisible(true);

      // Móvil 2 a la derecha
      phone2.setLocation(1300, 100);
      phone2.setVisible(true);
    });
  }
}