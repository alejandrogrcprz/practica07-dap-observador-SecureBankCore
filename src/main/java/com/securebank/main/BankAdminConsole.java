package com.securebank.main;

import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction; // <--- NUEVO IMPORT NECESARIO

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BankAdminConsole extends JFrame implements IBankObserver {
  private JTextArea auditLog;
  private DefaultTableModel tableModel;
  private JLabel lblFraudResult;

  public BankAdminConsole() {
    setTitle("SECUREBANK OPS CENTER");
    setSize(1000, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Tabla de historial
    // Hemos añadido columnas para que coincidan mejor con los datos de la Transacción
    String[] cols = {"HORA", "TIPO", "ORIGEN -> DESTINO", "CONCEPTO", "IMPORTE", "ESTADO"};
    tableModel = new DefaultTableModel(cols, 0);
    JTable table = new JTable(tableModel);

    // Logs de sistema
    auditLog = new JTextArea(10, 50);
    auditLog.setBackground(Color.BLACK);
    auditLog.setForeground(Color.GREEN);
    auditLog.setFont(new Font("Monospaced", Font.PLAIN, 12));

    lblFraudResult = new JLabel("SISTEMA ACTIVO - ESPERANDO TRÁFICO", SwingConstants.CENTER);
    lblFraudResult.setFont(new Font("Monospaced", Font.BOLD, 20));

    add(new JScrollPane(table), BorderLayout.CENTER);
    add(new JScrollPane(auditLog), BorderLayout.SOUTH);
    add(lblFraudResult, BorderLayout.NORTH);
  }

  public void logToSystem(String msg) {
    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
    auditLog.append("[" + time + "] " + msg + "\n");
    // Auto-scroll hacia abajo
    auditLog.setCaretPosition(auditLog.getDocument().getLength());
  }

  public void animateFraudCheck(String code) {
    lblFraudResult.setText("CHECKING: " + code);
    lblFraudResult.setForeground(code.equals("OK") ? Color.GREEN : Color.RED);
  }

  // --- AQUÍ ESTÁ EL CAMBIO CLAVE PARA EL OBSERVADOR ---
  @Override
  public void onTransactionAttempt(Transaction tx) {
    // Obtenemos la hora actual
    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

    // Formateamos los detalles: Quién envía a quién
    String route = tx.getSourceIban() + " -> " + tx.getDestIban();

    // Añadimos la fila a la tabla visualmente desglosando el objeto Transaction
    tableModel.addRow(new String[]{
      time,
      "TRANSFER",
      route,
      tx.getConcept(),
      String.format("%.2f €", tx.getAmount()),
      "AUDITADO"
    });

    // También lo escribimos en el log negro de abajo
    logToSystem("Nueva transacción detectada: " + tx.getAmount() + "€ (" + tx.getConcept() + ")");
  }

  // Mantenemos este método para que funcione tu GeneralLedger (BlockChain)
  public void addLedgerEntry(String data, String hash) {
    logToSystem("LEDGER: Bloque minado. Hash: " + hash.substring(0, 16) + "...");
  }
}