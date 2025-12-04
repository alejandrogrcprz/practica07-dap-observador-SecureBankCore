package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.interfaces.IBankObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MobilePhoneSimulator extends JFrame implements IBankObserver {

  // --- ARQUITECTURA ---
  private TransactionEngine engine;
  private BankRepository repository;

  // --- UI MÓVIL ---
  private JPanel screenPanel;
  private CardLayout osLayout; // Para cambiar entre Apps
  private JLabel lblStatusBarTime;

  // --- APPS INTERNAS ---
  private JPanel homeScreen;
  private JPanel smsApp;
  private JPanel bankAppLogin;
  private JPanel bankAppDashboard;
  private JPanel bankAppTransfer;
  private JPanel bankAppNotifications;

  // --- DATOS ---
  private DefaultListModel<String> smsListModel;
  private DefaultListModel<String> bankNotifListModel;

  // --- COMPONENTES DE TRANSFERENCIA ---
  private JComboBox<String> comboAccounts;
  private JTextField txtIban, txtBeneficiary, txtAmount, txtConcept;

  public MobilePhoneSimulator(TransactionEngine engine, BankRepository repository) {
    this.engine = engine;
    this.repository = repository;

    // 1. DISEÑO FÍSICO DEL TELÉFONO
    setTitle("Smartphone Cliente");
    setSize(380, 750);
    setLocation(100, 100);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);

    // Carcasa del móvil (Negra)
    JPanel bezel = new JPanel(new BorderLayout());
    bezel.setBackground(Color.BLACK);
    bezel.setBorder(new EmptyBorder(15, 10, 15, 10)); // Bordes del teléfono

    // 2. BARRA DE ESTADO SUPERIOR (Hora, Batería)
    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setBackground(Color.BLACK);
    statusBar.setBorder(new EmptyBorder(0, 5, 5, 5));
    lblStatusBarTime = new JLabel("12:00");
    lblStatusBarTime.setForeground(Color.WHITE);
    lblStatusBarTime.setFont(new Font("Segoe UI", Font.BOLD, 12));
    JLabel lblBat = new JLabel("🔋 100%  📶 5G");
    lblBat.setForeground(Color.WHITE);
    lblBat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    statusBar.add(lblStatusBarTime, BorderLayout.WEST);
    statusBar.add(lblBat, BorderLayout.EAST);
    bezel.add(statusBar, BorderLayout.NORTH);

    // 3. PANTALLA (Donde ocurren las Apps)
    osLayout = new CardLayout();
    screenPanel = new JPanel(osLayout);

    // Inicializamos las "Apps"
    initHomeScreen();
    initSmsApp();
    initBankApp(); // Incluye Login, Dash, Transfer y Notifs

    screenPanel.add(homeScreen, "HOME");
    screenPanel.add(smsApp, "SMS");
    screenPanel.add(bankAppLogin, "BANK_LOGIN");
    screenPanel.add(bankAppDashboard, "BANK_DASH");
    screenPanel.add(bankAppTransfer, "BANK_TRANS");
    screenPanel.add(bankAppNotifications, "BANK_NOTIF");

    bezel.add(screenPanel, BorderLayout.CENTER);

    // 4. BOTÓN HOME FÍSICO
    JButton btnHome = new JButton("⏺"); // Círculo
    btnHome.setBackground(new Color(30,30,30));
    btnHome.setForeground(Color.WHITE);
    btnHome.setFocusPainted(false);
    btnHome.setBorderPainted(false);
    btnHome.setFont(new Font("SansSerif", Font.BOLD, 20));
    btnHome.addActionListener(e -> osLayout.show(screenPanel, "HOME"));
    bezel.add(btnHome, BorderLayout.SOUTH);

    setContentPane(bezel);

    // Reloj en tiempo real
    new Timer(1000, e -> lblStatusBarTime.setText(new SimpleDateFormat("HH:mm").format(new Date()))).start();
  }

  // ========================================================================
  // SISTEMA OPERATIVO (PANTALLA DE INICIO)
  // ========================================================================
  private void initHomeScreen() {
    homeScreen = new JPanel(new GridLayout(4, 3, 15, 15));
    homeScreen.setBackground(new Color(40, 40, 40)); // Fondo oscuro moderno
    homeScreen.setBorder(new EmptyBorder(40, 20, 40, 20));

    // App Mensajes
    JButton btnSms = createAppIcon("💬", "Mensajes", new Color(100, 200, 100));
    btnSms.addActionListener(e -> osLayout.show(screenPanel, "SMS"));

    // App Banco
    JButton btnBank = createAppIcon("🏦", "SecureBank", new Color(0, 102, 204));
    btnBank.addActionListener(e -> osLayout.show(screenPanel, "BANK_LOGIN"));

    // Apps de relleno para realismo
    homeScreen.add(btnSms);
    homeScreen.add(btnBank);
    homeScreen.add(createAppIcon("📷", "Cámara", Color.GRAY));
    homeScreen.add(createAppIcon("📧", "Mail", Color.BLUE));
    homeScreen.add(createAppIcon("🎵", "Música", Color.RED));
    homeScreen.add(createAppIcon("⚙", "Ajustes", Color.DARK_GRAY));
  }

  private JButton createAppIcon(String emoji, String name, Color bg) {
    JButton b = new JButton("<html><center><font size='6'>" + emoji + "</font><br><font size='3'>" + name + "</font></center></html>");
    b.setBackground(bg);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    return b;
  }

  // ========================================================================
  // APP MENSAJERÍA (SMS)
  // ========================================================================
  private void initSmsApp() {
    smsApp = new JPanel(new BorderLayout());
    smsApp.setBackground(Color.WHITE);

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    header.setBackground(new Color(240,240,240));
    header.add(new JLabel("💬 Mensajes"));

    smsListModel = new DefaultListModel<>();
    JList<String> smsList = new JList<>(smsListModel);
    smsList.setCellRenderer(new BubbleRenderer(new Color(220, 255, 220))); // Burbujas verdes

    smsApp.add(header, BorderLayout.NORTH);
    smsApp.add(new JScrollPane(smsList), BorderLayout.CENTER);
  }

  // ========================================================================
  // APP SECUREBANK (LOGIN + DASHBOARD)
  // ========================================================================
  private void initBankApp() {
    // --- PANTALLA 1: LOGIN (PIN) ---
    bankAppLogin = new JPanel(new GridBagLayout());
    bankAppLogin.setBackground(new Color(0, 51, 102)); // Azul Corporativo

    JLabel logo = new JLabel("<html><center><h1>🔒</h1><h2>SecureBank</h2></center></html>");
    logo.setForeground(Color.WHITE);

    JPasswordField txtPin = new JPasswordField(6);
    txtPin.setHorizontalAlignment(JTextField.CENTER);
    txtPin.setFont(new Font("Arial", Font.BOLD, 24));

    JButton btnLogin = new JButton("ENTRAR");
    btnLogin.setBackground(Color.ORANGE);
    btnLogin.addActionListener(e -> {
      if (new String(txtPin.getPassword()).equals("1234")) {
        txtPin.setText("");
        osLayout.show(screenPanel, "BANK_DASH"); // Ir al Dashboard
      } else {
        JOptionPane.showMessageDialog(this, "PIN Incorrecto (Prueba 1234)");
      }
    });

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx=0; gbc.gridy=0; bankAppLogin.add(logo, gbc);
    gbc.gridy=1; gbc.insets=new Insets(20,0,10,0); bankAppLogin.add(txtPin, gbc);
    gbc.gridy=2; bankAppLogin.add(btnLogin, gbc);

    // --- PANTALLA 2: DASHBOARD PRINCIPAL ---
    bankAppDashboard = new JPanel(new BorderLayout());
    bankAppDashboard.setBackground(new Color(245, 250, 255));

    JPanel dashHeader = new JPanel(new BorderLayout());
    dashHeader.setBackground(new Color(0, 102, 204));
    dashHeader.setBorder(new EmptyBorder(15,15,15,15));
    JLabel lblUser = new JLabel("Hola, Alejandro 👋");
    lblUser.setForeground(Color.WHITE);
    lblUser.setFont(new Font("Segoe UI", Font.BOLD, 18));
    dashHeader.add(lblUser, BorderLayout.CENTER);

    JPanel dashMenu = new JPanel(new GridLayout(2, 1, 10, 10));
    dashMenu.setBorder(new EmptyBorder(20, 20, 20, 20));
    dashMenu.setOpaque(false);

    JButton btnNewTransfer = new JButton("💸 Nueva Transferencia");
    styleBankButton(btnNewTransfer);
    btnNewTransfer.addActionListener(e -> osLayout.show(screenPanel, "BANK_TRANS"));

    JButton btnMyNotifs = new JButton("🔔 Mis Notificaciones");
    styleBankButton(btnMyNotifs);
    btnMyNotifs.addActionListener(e -> osLayout.show(screenPanel, "BANK_NOTIF"));

    dashMenu.add(btnNewTransfer);
    dashMenu.add(btnMyNotifs);

    bankAppDashboard.add(dashHeader, BorderLayout.NORTH);
    bankAppDashboard.add(dashMenu, BorderLayout.CENTER);

    // Inicializar sub-pantallas
    initTransferScreen();
    initNotificationScreen();
  }

  private void styleBankButton(JButton b) {
    b.setBackground(Color.WHITE);
    b.setFont(new Font("Segoe UI", Font.BOLD, 14));
    b.setForeground(new Color(0, 102, 204));
    b.setFocusPainted(false);
  }

  // --- SUB-PANTALLA: FORMULARIO TRANSFERENCIA ---
  private void initTransferScreen() {
    bankAppTransfer = new JPanel(new BorderLayout());
    bankAppTransfer.setBackground(Color.WHITE);

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnBack = new JButton("←");
    btnBack.addActionListener(e -> osLayout.show(screenPanel, "BANK_DASH"));
    header.add(btnBack);
    header.add(new JLabel("Nueva Transferencia"));

    JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
    form.setBorder(new EmptyBorder(10, 20, 10, 20));

    comboAccounts = new JComboBox<>(repository.getAccountsArray());
    txtIban = new JTextField("ES88 2038 5555...");
    txtBeneficiary = new JTextField("Empresa S.L.");
    txtAmount = new JTextField("0.00");
    txtConcept = new JTextField("Pago");

    form.add(new JLabel("Origen:")); form.add(comboAccounts);
    form.add(new JLabel("Destino:")); form.add(txtIban);
    form.add(new JLabel("Beneficiario:")); form.add(txtBeneficiary);
    form.add(new JLabel("Importe (€):")); form.add(txtAmount);
    form.add(new JLabel("Concepto:")); form.add(txtConcept);

    JButton btnSend = new JButton("CONFIRMAR");
    btnSend.setBackground(new Color(0, 153, 76));
    btnSend.setForeground(Color.WHITE);
    btnSend.addActionListener(e -> doTransfer());

    bankAppTransfer.add(header, BorderLayout.NORTH);
    bankAppTransfer.add(form, BorderLayout.CENTER);
    bankAppTransfer.add(btnSend, BorderLayout.SOUTH);
  }

  // --- SUB-PANTALLA: BUZÓN DE NOTIFICACIONES ---
  private void initNotificationScreen() {
    bankAppNotifications = new JPanel(new BorderLayout());

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnBack = new JButton("←");
    btnBack.addActionListener(e -> osLayout.show(screenPanel, "BANK_DASH"));
    header.add(btnBack);
    header.add(new JLabel("Buzón Seguro"));

    bankNotifListModel = new DefaultListModel<>();
    JList<String> list = new JList<>(bankNotifListModel);
    list.setCellRenderer(new BubbleRenderer(new Color(200, 230, 255))); // Burbujas azules

    bankAppNotifications.add(header, BorderLayout.NORTH);
    bankAppNotifications.add(new JScrollPane(list), BorderLayout.CENTER);
  }

  // ========================================================================
  // LÓGICA DE NEGOCIO
  // ========================================================================
  private void doTransfer() {
    try {
      double amount = Double.parseDouble(txtAmount.getText().replace(",", "."));
      // Llamamos al Motor (que está en el backend)
      new Thread(() -> {
        try {
          Thread.sleep(500);
          engine.executeTransfer(amount, "MiCuenta", txtIban.getText());
          // Al terminar, volvemos al dashboard
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Operación Procesada");
            osLayout.show(screenPanel, "BANK_DASH");
          });
        } catch (Exception e) {}
      }).start();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error en los datos");
    }
  }

  // --- RECEPCIÓN DE EVENTOS (PATRÓN OBSERVER) ---
  @Override
  public void onTransactionExecuted(String data) {
    // 1. Llega SMS a la App de Mensajes
    String sms = "SMS: Pago de " + txtAmount.getText() + "€ realizado.";
    SwingUtilities.invokeLater(() -> smsListModel.addElement(sms));

    // 2. Llega Notificación Push a la App del Banco
    String notif = "BANK: Transferencia enviada a " + txtBeneficiary.getText();
    SwingUtilities.invokeLater(() -> bankNotifListModel.addElement(notif));

    // 3. Vibración/Aviso (Cambiar color barra estado momentáneamente)
    SwingUtilities.invokeLater(() -> lblStatusBarTime.setForeground(Color.GREEN));
  }

  // Renderizador simple para listas
  class BubbleRenderer extends DefaultListCellRenderer {
    Color bg;
    public BubbleRenderer(Color c) { this.bg = c; }
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      l.setBackground(bg);
      l.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      return l;
    }
  }
}