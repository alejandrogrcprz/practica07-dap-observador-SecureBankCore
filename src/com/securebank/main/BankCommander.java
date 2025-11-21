package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.interfaces.IBankObserver;
import com.securebank.observers.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class BankCommander extends JFrame implements IBankObserver {

  // --- COMPONENTES DEL MOTOR ---
  private TransactionEngine engine;

  // --- COMPONENTES FORMULARIO ---
  private JComboBox<String> comboAccounts;
  private JTextField txtIbanDest, txtBeneficiary, txtAmount, txtConcept;
  private JButton btnSignAndSend;

  // --- COMPONENTES PANELES VISUALES ---
  private JLabel lblCheckIP, lblCheckBlacklist, lblCheckAmount, lblFraudResult;
  private JTextArea ledgerArea;
  private List<String> secretLedgerData = new ArrayList<>();
  private JTextArea smsArea;
  private JTextArea auditLog;

  public BankCommander() {
    setTitle("SECUREBANK OPS CENTER - Panel de Control Financiero");
    setSize(1100, 750);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    // TEMA OSCURO (DARK MODE)
    UIManager.put("Panel.background", new Color(30, 30, 30));
    UIManager.put("Label.foreground", Color.WHITE);
    UIManager.put("OptionPane.background", new Color(50, 50, 50));
    UIManager.put("OptionPane.messageForeground", Color.WHITE);
    getContentPane().setBackground(new Color(30, 30, 30));

    // ========================================================================
    // 1. PANEL SUPERIOR: FORMULARIO DE TRANSFERENCIA
    // ========================================================================
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(new Color(45, 45, 45));
    formPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
      " NUEVA TRANSFERENCIA SEPA ",
      TitledBorder.LEFT, TitledBorder.TOP,
      new Font("Segoe UI", Font.BOLD, 14), Color.CYAN));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Fila 1: Cuenta Origen
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Cuenta Origen:"), gbc);

    gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
    // Definimos las cuentas iniciales
    String[] accounts = {
      "ES21 **** 0045 | Cuenta Nómina (Saldo: 24.500,00 €)",
      "ES99 **** 8899 | Cuenta Ahorro (Saldo: 150.000,00 €)",
      "ES55 **** 1122 | Business Black (Saldo: 1.200.450,00 €)"
    };
    comboAccounts = new JComboBox<>(accounts);
    comboAccounts.setBackground(Color.WHITE);
    comboAccounts.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    formPanel.add(comboAccounts, gbc);

    // Fila 2: IBAN Destino y Beneficiario
    gbc.gridwidth = 1;
    gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("IBAN Destino:"), gbc);
    gbc.gridx = 1; gbc.gridy = 1;
    txtIbanDest = new JTextField("ES88 2038 5555 0000 9999"); // Valor por defecto
    formPanel.add(txtIbanDest, gbc);

    gbc.gridx = 2; gbc.gridy = 1; formPanel.add(new JLabel("Beneficiario:"), gbc);
    gbc.gridx = 3; gbc.gridy = 1;
    txtBeneficiary = new JTextField("Empresa Ejemplo S.L.");
    formPanel.add(txtBeneficiary, gbc);

    // Fila 3: Importe y Concepto
    gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Importe (€):"), gbc);
    gbc.gridx = 1; gbc.gridy = 2;
    txtAmount = new JTextField("0.00");
    txtAmount.setFont(new Font("Consolas", Font.BOLD, 14));
    txtAmount.setForeground(Color.RED);
    formPanel.add(txtAmount, gbc);

    gbc.gridx = 2; gbc.gridy = 2; formPanel.add(new JLabel("Concepto:"), gbc);
    gbc.gridx = 3; gbc.gridy = 2;
    txtConcept = new JTextField("Pago de Servicios");
    formPanel.add(txtConcept, gbc);

    // Fila 4: Botón Gigante de Firmar (MEJORADO: Color Naranja/Oro llamativo)
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
    btnSignAndSend = new JButton("🔐 FIRMAR Y REALIZAR TRANSFERENCIA");

    // CAMBIO DE ESTILO VISUAL:
    btnSignAndSend.setBackground(new Color(255, 165, 0)); // Naranja Gold
    btnSignAndSend.setForeground(Color.BLACK);            // Texto Negro para contraste
    btnSignAndSend.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnSignAndSend.setPreferredSize(new Dimension(200, 50)); // Un poco más alto
    btnSignAndSend.setFocusPainted(false);
    btnSignAndSend.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Borde blanco

    btnSignAndSend.addActionListener(e -> attemptTransaction());

    formPanel.add(btnSignAndSend, gbc);

    add(formPanel, BorderLayout.NORTH);

    // ========================================================================
    // 2. PANELES DE LOS OBSERVADORES
    // ========================================================================
    JPanel mainGrid = new JPanel(new GridLayout(2, 2, 10, 10));
    mainGrid.setBackground(new Color(30, 30, 30));
    mainGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // --- PANEL FRAUDE ---
    JPanel fraudPanel = createPanel("IA DETECTOR DE FRAUDE (Security Scanner)");
    Box boxFraud = Box.createVerticalBox();
    lblCheckIP = createCheckLabel("1. Geolocalización Dispositivo...");
    lblCheckBlacklist = createCheckLabel("2. Check Listas Interpol / OFAC...");
    lblCheckAmount = createCheckLabel("3. Análisis de Patrón de Gasto...");
    lblFraudResult = new JLabel("ESPERANDO FIRMA");
    lblFraudResult.setFont(new Font("Consolas", Font.BOLD, 24));
    lblFraudResult.setForeground(Color.GRAY);
    lblFraudResult.setAlignmentX(Component.CENTER_ALIGNMENT);

    boxFraud.add(Box.createVerticalStrut(20));
    boxFraud.add(lblCheckIP); boxFraud.add(Box.createVerticalStrut(10));
    boxFraud.add(lblCheckBlacklist); boxFraud.add(Box.createVerticalStrut(10));
    boxFraud.add(lblCheckAmount); boxFraud.add(Box.createVerticalStrut(20));
    boxFraud.add(lblFraudResult);
    fraudPanel.add(boxFraud);
    mainGrid.add(fraudPanel);

    // --- PANEL LEDGER ---
    JPanel ledgerPanel = createPanel("LIBRO MAYOR (Blockchain Ledger)");
    ledgerArea = new JTextArea();
    ledgerArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    ledgerArea.setBackground(new Color(10, 10, 40));
    ledgerArea.setForeground(Color.CYAN);
    ledgerArea.setEditable(false);
    JButton btnDecrypt = new JButton("🔓 AUDITOR: DESENCRIPTAR REGISTRO");
    btnDecrypt.addActionListener(e -> unlockLedger());
    ledgerPanel.add(new JScrollPane(ledgerArea), BorderLayout.CENTER);
    ledgerPanel.add(btnDecrypt, BorderLayout.SOUTH);
    mainGrid.add(ledgerPanel);

    // --- PANEL NOTIFICACIONES ---
    JPanel notifPanel = createPanel("GATEWAY SMS / PUSH");
    JPanel phoneFrame = new JPanel(new BorderLayout());
    phoneFrame.setBackground(Color.BLACK);
    phoneFrame.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 12, true));
    smsArea = new JTextArea("\n  Sin notificaciones.\n  Red: 5G - Secure");
    smsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    smsArea.setBackground(new Color(220, 220, 220));
    smsArea.setForeground(Color.DARK_GRAY);
    smsArea.setEditable(false);
    smsArea.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
    phoneFrame.add(smsArea, BorderLayout.CENTER);
    notifPanel.add(phoneFrame);
    mainGrid.add(notifPanel);

    // --- PANEL AUDIT LOG ---
    JPanel auditPanel = createPanel("AUDITORÍA TÉCNICA (System Log)");
    auditLog = new JTextArea();
    auditLog.setFont(new Font("Consolas", Font.PLAIN, 11));
    auditLog.setBackground(Color.BLACK);
    auditLog.setForeground(Color.GREEN);
    auditPanel.add(new JScrollPane(auditLog));
    mainGrid.add(auditPanel);

    add(mainGrid, BorderLayout.CENTER);

    initSystem();
  }

  private void initSystem() {
    engine = new TransactionEngine();
    // Conectamos los observadores
    engine.attach(new FraudDetectorAI());
    engine.attach(new AuditLogger());
    engine.attach(new GeneralLedger());
    engine.attach(new NotificationService());
    engine.attach(this);
  }

  // --- LÓGICA DE LA APP ---
  private void attemptTransaction() {
    // VALIDACIÓN NUMÉRICA
    String amountStr = txtAmount.getText().replace(",", ".");
    double amount;
    try {
      amount = Double.parseDouble(amountStr);
      if (amount <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Por favor, introduzca un importe válido mayor que 0.", "Error", JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (txtIbanDest.getText().trim().isEmpty() || txtBeneficiary.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Faltan datos obligatorios.", "Error", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // VALIDACIÓN DE SALDO
    String selectedAccount = (String) comboAccounts.getSelectedItem();
    double currentBalance = extractBalance(selectedAccount);

    if (amount > currentBalance) {
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(this,
        "OPERACIÓN DENEGADA: Fondos Insuficientes.\n\nSaldo: " + currentBalance + " €\nSolicitado: " + amount + " €",
        "Error de Saldo", JOptionPane.ERROR_MESSAGE);
      logAudit("!!! INTENTO FALLIDO: FONDOS INSUFICIENTES (" + amount + "€) !!!");
      return;
    }

    // PIN DE SEGURIDAD
    JPasswordField pf = new JPasswordField();
    int opt = JOptionPane.showConfirmDialog(this, pf, "🔐 FIRMA REQUERIDA: Introduzca PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (opt == JOptionPane.OK_OPTION) {
      String pin = new String(pf.getPassword());
      if (pin.length() >= 4) {
        // LANZAMOS SIMULACIÓN
        runSimulation(amount, txtIbanDest.getText(), txtConcept.getText());
      } else {
        JOptionPane.showMessageDialog(this, "PIN Incorrecto.", "Error", JOptionPane.ERROR_MESSAGE);
        logAudit("FALLO DE AUTENTICACIÓN - PIN INCORRECTO");
      }
    }
  }

  private void runSimulation(double amount, String iban, String concept) {
    btnSignAndSend.setEnabled(false);
    resetVisuals();

    // --- NUEVO: ACTUALIZAR SALDO VISUALMENTE ---
    updateAccountBalanceInUI(amount);

    new Thread(() -> {
      try {
        logAudit(">>> INICIANDO PROTOCOLO SEPA...");
        logAudit("Solicitud: " + amount + "€ -> " + iban);

        // Paso 1: Detector de Fraude
        Thread.sleep(600);
        updateLabel(lblCheckIP, "✔ UBICACIÓN: MADRID (Dispositivo Conocido)", Color.GREEN);
        Thread.sleep(600);
        updateLabel(lblCheckBlacklist, "✔ BENEFICIARIO: " + txtBeneficiary.getText() + " (Limpio)", Color.GREEN);
        Thread.sleep(600);

        if (amount > 10000) {
          updateLabel(lblCheckAmount, "⚠ ALTO VALOR: Verificación Extendida...", Color.ORANGE);
          Thread.sleep(1500);
          updateLabel(lblCheckAmount, "✔ IMPORTE: AUTORIZADO (Nivel 2)", Color.GREEN);
        } else {
          updateLabel(lblCheckAmount, "✔ IMPORTE: NORMAL", Color.GREEN);
        }

        lblFraudResult.setText("✅ TRANSACCIÓN LIMPIA");
        lblFraudResult.setForeground(Color.GREEN);

        // Paso 2: Motor Real
        Thread.sleep(500);
        engine.executeTransfer(amount, "MiCuenta", iban);

      } catch (InterruptedException e) { e.printStackTrace(); }
      finally { btnSignAndSend.setEnabled(true); }
    }).start();
  }

  // --- NUEVO MÉTODO: ACTUALIZA EL COMBOBOX CON EL NUEVO DINERO ---
  private void updateAccountBalanceInUI(double amountDeducted) {
    int selectedIndex = comboAccounts.getSelectedIndex();
    String currentText = (String) comboAccounts.getSelectedItem();

    // Sacamos el saldo actual
    double currentBalance = extractBalance(currentText);
    double newBalance = currentBalance - amountDeducted;

    // Formateamos de nuevo a String bonito (ej: 24.500,00)
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator('.');
    symbols.setDecimalSeparator(',');
    DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

    // Reconstruimos el String: "ES21... (Saldo: " + nuevo + " €)"
    // Usamos una expresión regular simple para reemplazar lo que hay dentro de "Saldo: ... €"
    String newText = currentText.replaceAll("Saldo: .* €\\)", "Saldo: " + df.format(newBalance) + " €)");

    // Actualizamos la interfaz en el hilo de eventos
    SwingUtilities.invokeLater(() -> {
      // Truco para actualizar el ítem sin romper el listener
      comboAccounts.removeItemAt(selectedIndex);
      comboAccounts.insertItemAt(newText, selectedIndex);
      comboAccounts.setSelectedIndex(selectedIndex);
      logAudit(">>> CUENTA DEBITADA: Nuevo Saldo " + df.format(newBalance) + "€");
    });
  }

  // --- RESPUESTA DEL PATRÓN OBSERVADOR ---
  @Override
  public void onTransactionExecuted(String data) {
    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
    String concepto = txtConcept.getText();
    String importe = txtAmount.getText();

    logAudit("CORE NOTIFICA: Transacción completada (" + time + ")");
    logAudit("Persistiendo hash en clúster de almacenamiento...");

    String encrypted = Base64.getEncoder().encodeToString((data + " | " + concepto).getBytes());
    ledgerArea.append("[" + time + "] {SHA-256} " + encrypted.substring(0, 20) + "...\n");
    secretLedgerData.add("[" + time + "] " + data + " | Ref: " + concepto);

    smsArea.setText("\n 💬 SECUREBANK INFO \n\n Transferencia realizada.\n ------------------\n Importe: -" + importe + " €\n Concepto: " + concepto + "\n Dest: " + txtBeneficiary.getText() + "\n\n Saldo disponible actualizado.");
    smsArea.setBackground(new Color(220, 255, 220));
  }

  // --- MÉTODOS AUXILIARES ---
  private double extractBalance(String accountText) {
    try {
      int start = accountText.indexOf("Saldo: ") + 7;
      int end = accountText.indexOf(" €");
      String numberStr = accountText.substring(start, end);
      numberStr = numberStr.replace(".", "").replace(",", ".");
      return Double.parseDouble(numberStr);
    } catch (Exception e) {
      return 0.0;
    }
  }

  private void unlockLedger() {
    JPasswordField pf = new JPasswordField();
    int ok = JOptionPane.showConfirmDialog(this, pf, "Credenciales de Auditor (root)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    if (ok == JOptionPane.OK_OPTION && "root".equals(new String(pf.getPassword()))) {
      StringBuilder realContent = new StringBuilder("--- REGISTRO OFICIAL DESCIFRADO ---\n");
      for (String s : secretLedgerData) realContent.append(s).append("\n");
      JTextArea ta = new JTextArea(15, 50); ta.setText(realContent.toString());
      JOptionPane.showMessageDialog(this, new JScrollPane(ta), "LIBRO MAYOR DESBLOQUEADO", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(this, "ACCESO DENEGADO", "ALERTA", JOptionPane.ERROR_MESSAGE);
      logAudit("!!! ERROR DE ACCESO AL LEDGER !!!");
    }
  }

  private JPanel createPanel(String title) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(new Color(40, 40, 40));
    p.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(Color.GRAY), title,
      TitledBorder.LEFT, TitledBorder.TOP,
      new Font("SansSerif", Font.BOLD, 12), Color.WHITE));
    return p;
  }

  private JLabel createCheckLabel(String text) {
    JLabel l = new JLabel("⏳ " + text);
    l.setForeground(Color.LIGHT_GRAY);
    l.setAlignmentX(Component.CENTER_ALIGNMENT);
    return l;
  }

  private void updateLabel(JLabel label, String text, Color color) {
    label.setText(text); label.setForeground(color);
  }

  private void resetVisuals() {
    lblCheckIP.setText("⏳ 1. Geolocalización..."); lblCheckIP.setForeground(Color.LIGHT_GRAY);
    lblCheckBlacklist.setText("⏳ 2. Check Listas..."); lblCheckBlacklist.setForeground(Color.LIGHT_GRAY);
    lblCheckAmount.setText("⏳ 3. Análisis Patrón..."); lblCheckAmount.setForeground(Color.LIGHT_GRAY);
    lblFraudResult.setText("ANALIZANDO..."); lblFraudResult.setForeground(Color.YELLOW);
    smsArea.setText("\n  Procesando firma digital..."); smsArea.setBackground(new Color(255, 255, 200));
  }

  private void logAudit(String msg) {
    auditLog.append("> " + msg + "\n");
    auditLog.setCaretPosition(auditLog.getDocument().getLength());
  }

  public static void main(String[] args) {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
    SwingUtilities.invokeLater(() -> new BankCommander().setVisible(true));
  }
}