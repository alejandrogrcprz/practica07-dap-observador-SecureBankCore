package com.securebank.main;

import com.securebank.interfaces.IBankObserver;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankAdminConsole extends JFrame implements IBankObserver {

  // --- COMPONENTES VISUALES ---
  private JTextArea ledgerArea, auditLog;
  // Etiquetas para los 4 pasos del escáner
  private JLabel lblStep1, lblStep2, lblStep3, lblStep4, lblFraudResult;
  private JProgressBar progressCPU, progressRAM;

  // --- DATOS ---
  private List<String> secretLedgerData = new ArrayList<>();
  private JTable historyTable;
  private DefaultTableModel tableModel;
  private JLabel lblKpiVolume, lblKpiFraud, lblKpiCount;

  private final String CSV_FILE = "bank_history.csv";

  public BankAdminConsole() {
    setTitle("SECUREBANK OPS CENTER - INFRAESTRUCTURA CRÍTICA");
    setSize(1200, 800);
    setLocation(500, 100);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setupDarkTheme();

    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

    tabs.addTab("  SALA DE MÁQUINAS (LIVE)  ", createLiveMonitor());
    tabs.addTab("  DATOS FINANCIEROS  ", createDataPanel());

    add(tabs);

    loadHistoryFromDisk();
    updateKPIs();

    startServerSimulation();
  }

  private void setupDarkTheme() {
    UIManager.put("Panel.background", new Color(30, 30, 30));
    UIManager.put("Label.foreground", Color.WHITE);
    UIManager.put("Table.background", new Color(40, 40, 40));
    UIManager.put("Table.foreground", Color.WHITE);
    UIManager.put("TableHeader.background", new Color(20, 20, 20));
    UIManager.put("TableHeader.foreground", Color.ORANGE);
    getContentPane().setBackground(new Color(30, 30, 30));
  }

  // --- PESTAÑA 1: MONITORIZACIÓN (REDISEÑADA) ---
  private JPanel createLiveMonitor() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    statusPanel.setBackground(new Color(20, 20, 20));
    // Iconos ASCII limpios en lugar de emojis fallidos
    JLabel lblStatus = new JLabel("● CLUSTER STATUS: HEALTHY | ● REGION: EU-WEST-1 | ● PKI: ACTIVE");
    lblStatus.setForeground(Color.GREEN);
    lblStatus.setFont(new Font("Consolas", Font.BOLD, 12));
    statusPanel.add(lblStatus);
    panel.add(statusPanel, BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
    grid.setBackground(new Color(30, 30, 30));
    grid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // 1. FRAUD DETECTOR AI (NUEVO DISEÑO SECUENCIAL)
    JPanel pFraud = createPanel("FRAUD DETECTOR AI"); // Nombre cambiado
    Box box = Box.createVerticalBox();

    // Los 4 pasos del escáner
    lblStep1 = createLabel("• Velocity Check (Rate Limit)");
    lblStep2 = createLabel("• Patrón Numérico (Heurística)");
    lblStep3 = createLabel("• Riesgo Geográfico (IBAN)");
    lblStep4 = createLabel("• Lista Negra (Contenido)");

    lblFraudResult = new JLabel("[ IDLE ]");
    lblFraudResult.setForeground(Color.DARK_GRAY);
    lblFraudResult.setAlignmentX(Component.CENTER_ALIGNMENT);
    lblFraudResult.setFont(new Font("Consolas", Font.BOLD, 24));

    box.add(Box.createVerticalStrut(20));
    box.add(lblStep1); box.add(lblStep2); box.add(lblStep3); box.add(lblStep4);
    box.add(Box.createVerticalStrut(20)); box.add(lblFraudResult);
    pFraud.add(box); grid.add(pFraud);

    // 2. LEDGER (Sin cambios importantes)
    JPanel pLedger = createPanel("BLOCKCHAIN LEDGER (IMMUTABLE)");
    ledgerArea = new JTextArea();
    ledgerArea.setBackground(new Color(15, 15, 30));
    ledgerArea.setForeground(new Color(100, 255, 100));
    ledgerArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
    ledgerArea.setEditable(false);
    JButton btnUnlock = new JButton("[ INSPECCIONAR BLOQUE (ROOT) ]"); // Texto limpio
    btnUnlock.setBackground(new Color(0, 100, 200));
    btnUnlock.setForeground(Color.WHITE);
    btnUnlock.setFocusPainted(false);
    btnUnlock.addActionListener(e -> unlockLedger());
    pLedger.add(new JScrollPane(ledgerArea), BorderLayout.CENTER);
    pLedger.add(btnUnlock, BorderLayout.SOUTH);
    grid.add(pLedger);

    // 3. LOGS DEL SISTEMA
    JPanel pAudit = createPanel("SYSTEM EVENT LOG");
    auditLog = new JTextArea();
    auditLog.setBackground(Color.BLACK);
    auditLog.setForeground(Color.LIGHT_GRAY);
    auditLog.setFont(new Font("Consolas", Font.PLAIN, 11));
    pAudit.add(new JScrollPane(auditLog)); grid.add(pAudit);

    // 4. SERVER HEALTH
    JPanel pServer = createPanel("INFRASTRUCTURE HEALTH");
    Box serverBox = Box.createVerticalBox();
    progressCPU = new JProgressBar(0, 100); progressCPU.setStringPainted(true); progressCPU.setForeground(new Color(255, 100, 100)); progressCPU.setBackground(Color.DARK_GRAY);
    progressRAM = new JProgressBar(0, 100); progressRAM.setStringPainted(true); progressRAM.setForeground(new Color(100, 150, 255)); progressRAM.setBackground(Color.DARK_GRAY);
    serverBox.add(new JLabel("NETWORK TRAFFIC (TPS):")); serverBox.add(progressCPU);
    serverBox.add(Box.createVerticalStrut(15));
    serverBox.add(new JLabel("DB LATENCY (ms):")); serverBox.add(progressRAM);
    JTextArea stats = new JTextArea("\n Active Threads: 42\n Garbage Collector: Idle\n DB Connections: 8/100");
    stats.setBackground(new Color(30,30,30)); stats.setForeground(Color.GRAY); stats.setEditable(false);
    serverBox.add(stats);
    pServer.add(serverBox);
    grid.add(pServer);

    panel.add(grid, BorderLayout.CENTER);
    return panel;
  }

  // --- PESTAÑA 2: HISTORIAL (Sin cambios) ---
  private JPanel createDataPanel() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(new Color(30, 30, 30));
    p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
    kpiPanel.setOpaque(false);
    kpiPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    lblKpiVolume = createKpiCard("VOLUMEN TOTAL", "0.00 €", new Color(0, 100, 200));
    lblKpiFraud = createKpiCard("ALERTAS", "0", new Color(200, 50, 50));
    lblKpiCount = createKpiCard("OK", "0", new Color(50, 150, 50));
    kpiPanel.add(lblKpiVolume); kpiPanel.add(lblKpiFraud); kpiPanel.add(lblKpiCount);
    p.add(kpiPanel, BorderLayout.NORTH);

    String[] cols = {"FECHA", "TIPO", "ORIGEN -> DESTINO", "CONCEPTO", "IMPORTE", "ESTADO"};
    tableModel = new DefaultTableModel(cols, 0);
    historyTable = new JTable(tableModel);
    historyTable.setRowHeight(30);
    historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

    historyTable.getColumnModel().getColumn(0).setPreferredWidth(120);
    historyTable.getColumnModel().getColumn(2).setPreferredWidth(300);

    historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = (String) table.getValueAt(row, 5);
        if ("BLOQUEADO".equals(status)) {
          c.setForeground(Color.RED); c.setBackground(new Color(50, 10, 10));
        } else {
          c.setForeground(Color.WHITE);
          if (!isSelected) c.setBackground(row % 2 == 0 ? new Color(45, 45, 45) : new Color(55, 55, 55));
        }
        if (column == 4) c.setForeground("BLOQUEADO".equals(status) ? Color.RED : Color.ORANGE);
        if (isSelected) { c.setBackground(Color.ORANGE); c.setForeground(Color.BLACK); }
        return c;
      }
    });

    p.add(new JScrollPane(historyTable), BorderLayout.CENTER);
    return p;
  }

  // ========================================================================
  // MÉTODOS PÚBLICOS PARA LOS OBSERVADORES
  // ========================================================================

  public void logToSystem(String message) {
    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
    if (auditLog != null) {
      // Icono ASCII limpio para el log
      auditLog.append("> [" + time + "] " + message.replace("🚨", "[ALERTA]").replace("NOTIFICACIÓN:", "[NOTIF]") + "\n");
      auditLog.setCaretPosition(auditLog.getDocument().getLength());
    }
    spikeCpu();
  }

  public void addLedgerEntry(String rawData, String hash) {
    String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    if (ledgerArea != null) {
      ledgerArea.append("[" + time + "] BLOCK MINED: " + hash.substring(0, 16) + "...\n");
    }
    String json = "{\n  \"timestamp\": \"" + time + "\",\n  \"hash\": \"" + hash + "\",\n  \"data\": \"" + rawData + "\"\n}";
    secretLedgerData.add(json);
  }

  // --- NUEVA ANIMACIÓN SECUENCIAL DEL ESCÁNER ---
  public void animateFraudCheck(String resultCode) {
    new Thread(() -> {
      try {
        // ESTADO INICIAL: ESCANEANDO
        if (lblFraudResult != null) { lblFraudResult.setText("[ SCANNING... ]"); lblFraudResult.setForeground(Color.CYAN); }
        spikeCpu();

        // Limpiamos los pasos anteriores
        resetLabels(false);

        // SECUENCIA DE CHEQUEO (Cada paso tarda 400ms)
        // Usamos iconos ASCII seguros (✔ y ❌)

        // PASO 1: VELOCITY
        Thread.sleep(400);
        if (resultCode.contains("STEP1")) { updateLabel(lblStep1, "❌ Velocity Check (Rate Limit)", Color.RED); failSequence(); return; }
        else updateLabel(lblStep1, "✔ Velocity Check (Rate Limit)", Color.GREEN);

        // PASO 2: PATRÓN
        Thread.sleep(400);
        if (resultCode.contains("STEP2")) { updateLabel(lblStep2, "❌ Patrón Numérico (Heurística)", Color.RED); failSequence(); return; }
        else updateLabel(lblStep2, "✔ Patrón Numérico (Heurística)", Color.GREEN);

        // PASO 3: GEO
        Thread.sleep(400);
        if (resultCode.contains("STEP3")) { updateLabel(lblStep3, "❌ Riesgo Geográfico (IBAN)", Color.RED); failSequence(); return; }
        else updateLabel(lblStep3, "✔ Riesgo Geográfico (IBAN)", Color.GREEN);

        // PASO 4: BLACKLIST
        Thread.sleep(400);
        if (resultCode.contains("STEP4")) { updateLabel(lblStep4, "❌ Lista Negra (Contenido)", Color.RED); failSequence(); return; }
        else updateLabel(lblStep4, "✔ Lista Negra (Contenido)", Color.GREEN);

        // SI LLEGA AQUÍ, TODO ES CORRECTO (OK)
        Thread.sleep(200);
        if (lblFraudResult != null) { lblFraudResult.setText("[ SECURE ]"); lblFraudResult.setForeground(Color.GREEN); }

        // Resetear después de 3 segundos
        Thread.sleep(3000); resetLabels(true);

      } catch (Exception e) {}
    }).start();
  }

  // Método auxiliar para cuando la secuencia falla
  private void failSequence() throws InterruptedException {
    if (lblFraudResult != null) { lblFraudResult.setText("[ BLOCKED ]"); lblFraudResult.setForeground(Color.RED); }
    Thread.sleep(3000); resetLabels(true);
  }

  // --- RECEPCIÓN EVENTOS (TABLA GENERAL) ---
  @Override
  public void onTransactionExecuted(String data) {
    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    boolean isFraud = data.contains("BLOQUEADO");
    boolean isError = data.contains("FALLO");

    String tipo = "TRANSFER"; String estado = "OK"; String importe = "---"; String detalle = data; String concepto = "";

    if (isFraud) {
      tipo = "ALERTA"; estado = "BLOQUEADO"; detalle = "Intento de fraude detectado";
      try {
        String[] parts = data.split("\\|");
        if (parts.length >= 3) detalle = parts[2].trim(); // Usar el detalle del fraude si existe
      } catch (Exception e) {}
    } else if (isError) {
      tipo = "ERROR"; estado = "DENEGADO"; detalle = "Fallo operativo (Saldo)";
    } else {
      try {
        String[] parts = data.split("\\|");
        if (parts.length >= 4) { detalle = parts[1].trim(); concepto = parts[3].trim(); }
      } catch (Exception e) {}
    }

    try { Pattern p = Pattern.compile("(\\d+[.,]?\\d*)\\s?€"); Matcher m = p.matcher(data); if (m.find()) importe = m.group(1) + " €"; } catch (Exception e) {}

    String[] rowData = {time, tipo, detalle, concepto, importe, estado};
    tableModel.addRow(rowData);
    saveToCSV(rowData);
    updateKPIs();
  }

  // --- SIMULACIÓN SERVIDOR ---
  private void startServerSimulation() {
    Timer t = new Timer(1000, e -> {
      int cpu = new Random().nextInt(10) + 5; int ram = new Random().nextInt(5) + 40;
      if(progressCPU.getValue() > 20) cpu = progressCPU.getValue() - 5;
      progressCPU.setValue(cpu); progressRAM.setValue(ram);
    });
    t.start();
  }

  private void spikeCpu() { progressCPU.setValue(new Random().nextInt(40) + 50); }

  // --- UTILS ---
  private void updateKPIs() {
    double totalVol = 0; int fraudCount = 0; int okCount = 0;
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      String status = (String) tableModel.getValueAt(i, 5);
      String amountStr = (String) tableModel.getValueAt(i, 4);
      if ("BLOQUEADO".equals(status)) fraudCount++;
      else if ("OK".equals(status)) {
        okCount++;
        if (!"---".equals(amountStr)) { try { totalVol += Double.parseDouble(amountStr.replace("€", "").trim().replace(",", ".")); } catch (Exception e) {} }
      }
    }
    DecimalFormat df = new DecimalFormat("#,##0.00");
    if(lblKpiVolume!=null) lblKpiVolume.setText("<html><center><span style='font-size:10px; color:#ccc'>VOLUMEN</span><br><span style='font-size:20px; font-weight:bold'>" + df.format(totalVol) + " €</span></center></html>");
    if(lblKpiFraud!=null) lblKpiFraud.setText("<html><center><span style='font-size:10px; color:#ccc'>ALERTAS</span><br><span style='font-size:20px; font-weight:bold'>" + fraudCount + "</span></center></html>");
    if(lblKpiCount!=null) lblKpiCount.setText("<html><center><span style='font-size:10px; color:#ccc'>EXITOSAS</span><br><span style='font-size:20px; font-weight:bold'>" + okCount + "</span></center></html>");
  }

  private void saveToCSV(String[] rowData) {
    try (FileWriter fw = new FileWriter(CSV_FILE, true); BufferedWriter bw = new BufferedWriter(fw)) {
      bw.write(String.join(";", rowData)); bw.newLine();
    } catch (IOException e) {}
  }

  private void loadHistoryFromDisk() {
    File file = new File(CSV_FILE); if (!file.exists()) return;
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (!line.isEmpty()) { String[] data = line.split(";"); if (data.length == 6) tableModel.addRow(data); }
      }
    } catch (Exception e) {}
  }

  private void unlockLedger() {
    JPasswordField pf = new JPasswordField();
    int ok = JOptionPane.showConfirmDialog(this, pf, "Acceso Root (Pass: root):", JOptionPane.OK_CANCEL_OPTION);
    if (ok == JOptionPane.OK_OPTION && "root".equals(new String(pf.getPassword()))) {
      StringBuilder sb = new StringBuilder(); for (String row : secretLedgerData) sb.append(row).append("\n");
      JTextArea textArea = new JTextArea(20, 60); textArea.setText(sb.toString());
      JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "LEDGER JSON DATA", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void resetLabels(boolean resetResult) {
    // Usamos un punto ASCII limpio (•) en lugar de gris
    updateLabel(lblStep1, "• Velocity Check (Rate Limit)", Color.GRAY);
    updateLabel(lblStep2, "• Patrón Numérico (Heurística)", Color.GRAY);
    updateLabel(lblStep3, "• Riesgo Geográfico (IBAN)", Color.GRAY);
    updateLabel(lblStep4, "• Lista Negra (Contenido)", Color.GRAY);
    if(resetResult && lblFraudResult!=null) { lblFraudResult.setText("[ IDLE ]"); lblFraudResult.setForeground(Color.DARK_GRAY); }
  }

  // Métodos auxiliares para crear componentes con estilos limpios
  private JPanel createPanel(String t) { JPanel p = new JPanel(new BorderLayout()); p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), t, 0,0,new Font("Segoe UI",1,12),Color.WHITE)); p.setBackground(new Color(20,20,20)); return p; }
  private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setForeground(Color.GRAY); l.setAlignmentX(Component.CENTER_ALIGNMENT); l.setFont(new Font("Segoe UI", Font.PLAIN, 14)); return l; }
  private void updateLabel(JLabel l, String t, Color c) { if(l!=null) { l.setText(t); l.setForeground(c); } }
  private JLabel createKpiCard(String t, String v, Color c) { JLabel l = new JLabel("<html><center><span style='font-size:10px; color:#ccc'>" + t + "</span><br><span style='font-size:20px; font-weight:bold'>" + v + "</span></center></html>", SwingConstants.CENTER); l.setOpaque(true); l.setBackground(c); l.setForeground(Color.WHITE); l.setBorder(BorderFactory.createLineBorder(Color.WHITE)); return l; }
}