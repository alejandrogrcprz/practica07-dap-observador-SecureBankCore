package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.interfaces.IBankObserver;
import com.securebank.observers.*;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// Esta clase es la INTERFAZ GRÁFICA.
// Fíjate que implementa IBankObserver. ¡La ventana también es un observador!
public class BankDashboard extends JFrame implements IBankObserver {

  private JTextArea logArea;
  private JProgressBar progressBar;
  private JButton executeButton;
  private TransactionEngine engine;

  public BankDashboard() {
    // 1. Configuración básica de la ventana
    setTitle("SecureBank Core - Monitor de Transacciones");
    setSize(700, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Centrar en pantalla
    setLocationRelativeTo(null);

    // 2. Panel Superior (Botón y Barra)
    JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    executeButton = new JButton("EJECUTAR TRANSACCIÓN SIMULADA (5.000€)");
    executeButton.setFont(new Font("Arial", Font.BOLD, 14));
    executeButton.setBackground(new Color(70, 130, 180)); // Azul acero
    executeButton.setForeground(Color.WHITE);

    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString("Sistema en espera...");

    topPanel.add(executeButton);
    topPanel.add(progressBar);
    add(topPanel, BorderLayout.NORTH);

    // 3. Panel Central (Log tipo Hacker/Consola)
    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setBackground(Color.BLACK);
    logArea.setForeground(new Color(0, 255, 0)); // Verde brillante
    logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane scrollPane = new JScrollPane(logArea);
    scrollPane.setBorder(BorderFactory.createTitledBorder(" Registro de Eventos en Tiempo Real "));
    add(scrollPane, BorderLayout.CENTER);

    // 4. Inicializar el Motor y los Observadores
    initBankSystem();

    // 5. Acción del botón
    executeButton.addActionListener(e -> runSimulation());
  }

  private void initBankSystem() {
    // --- AQUÍ CONECTAMOS TU CÓDIGO ---
    engine = new TransactionEngine();

    // Creamos los observadores normales
    FraudDetectorAI fraud = new FraudDetectorAI();
    AuditLogger logger = new AuditLogger();
    NotificationService notifier = new NotificationService();
    GeneralLedger ledger = new GeneralLedger();

    // Los conectamos al motor
    engine.attach(fraud);
    engine.attach(logger);
    engine.attach(notifier);
    engine.attach(ledger);

    // ¡IMPORTANTE! Nos conectamos a nosotros mismos (la ventana) como observador
    // Así podremos imprimir en la pantalla negra lo que ocurre.
    engine.attach(this);

    logToScreen(">>> SISTEMA INICIALIZADO CORRECTAMENTE.");
    logToScreen(">>> Esperando peticiones...");
  }

  private void runSimulation() {
    // Desactivar botón para que no le den dos veces
    executeButton.setEnabled(false);
    progressBar.setValue(0);
    progressBar.setString("Procesando Transacción...");
    logToScreen("\n--- NUEVA SOLICITUD DE TRANSFERENCIA RECIBIDA ---");

    // Usamos un hilo separado para no congelar la ventana
    new Thread(() -> {
      try {
        // Simulación visual de carga
        for (int i = 0; i <= 50; i+=10) {
          progressBar.setValue(i);
          Thread.sleep(100);
        }

        // EJECUTAMOS TU MOTOR (Esto dispara el notifyObservers)
        engine.executeTransfer(5000.0, "ES-21-1234", "ES-99-5678");

        // Completar barra
        progressBar.setValue(100);
        progressBar.setString("Transacción Finalizada");

      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        // Reactivar botón
        executeButton.setEnabled(true);
      }
    }).start();
  }

  // Este es el método del Patrón Observador.
  // Cuando el motor avisa, la ventana recibe el dato y lo pinta.
  @Override
  public void onTransactionExecuted(String data) {
    // Añadimos el mensaje al log visual
    logToScreen("[EVENTO RECIBIDO] Notificación del Core: " + data);

    // Simulamos que los otros sistemas están respondiendo también en pantalla
    logToScreen("   > [FRAUDE] Analizando riesgo... OK.");
    logToScreen("   > [AUDITORÍA] Guardando en BBDD inmutable... OK.");
    logToScreen("   > [NOTIFICACIÓN] Enviando SMS... ENVIADO.");
    logToScreen("   > [CONTABILIDAD] Cuadrando Libro Mayor... OK.");
    logToScreen(">>> PROCESO COMPLETADO CON ÉXITO.");
  }

  // Método auxiliar para escribir con la hora
  private void logToScreen(String msg) {
    String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    // SwingUtilities asegura que pintemos en el hilo correcto
    SwingUtilities.invokeLater(() -> {
      logArea.append("[" + timeStamp + "] " + msg + "\n");
      // Bajar el scroll automáticamente
      logArea.setCaretPosition(logArea.getDocument().getLength());
    });
  }

  // Método Main para lanzar la ventana directamente
  public static void main(String[] args) {
    // Iniciar la ventana
    SwingUtilities.invokeLater(() -> {
      new BankDashboard().setVisible(true);
    });
  }
}