package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.interfaces.IBankObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.List;

public class MobilePhoneSimulator extends JFrame implements IBankObserver {

  private TransactionEngine engine;
  private BankRepository repository;

  // CAPAS VISUALES
  private JLayeredPane layeredPane;
  private JPanel mainOSPanel;
  private JPanel notificationBanner;
  private JLabel lblNotifTitle, lblNotifText;
  private Timer notifTimer;

  // ESTADO
  private JLabel lblStatusBarTime;
  private JLabel lblNotifIcon;

  // APPS
  private CardLayout osLayout;
  private JPanel homeScreen, smsApp, bankAppLogin, bankAppDashboard, bankAppTransfer, bankAppNotifications, receiptScreen, downloadsApp;

  // DATOS
  private DefaultListModel<NotificationData> smsListModel;
  private DefaultListModel<NotificationData> bankNotifListModel;
  private DefaultListModel<String> downloadsListModel;

  // TRANSFERENCIA
  private JComboBox<String> comboAccounts;
  private JTextField txtIban, txtBeneficiary, txtAmount, txtConcept;

  // VARIABLES TEMPORALES
  private String lastAmount = "0.00";
  private String lastBeneficiary = "";

  public MobilePhoneSimulator(TransactionEngine engine, BankRepository repository) {
    this.engine = engine;
    this.repository = repository;

    setTitle("Smartphone Cliente");
    setSize(380, 750);
    setLocation(100, 100);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);

    // CARCASA
    JPanel bezel = new JPanel(new BorderLayout());
    bezel.setBackground(Color.BLACK);
    bezel.setBorder(new EmptyBorder(10, 10, 15, 10));

    // 1. BARRA DE ESTADO
    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setBackground(Color.BLACK);
    statusBar.setBorder(new EmptyBorder(0, 5, 5, 5));

    JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    leftStatus.setBackground(Color.BLACK);
    lblStatusBarTime = new JLabel("12:00");
    lblStatusBarTime.setForeground(Color.WHITE);
    lblStatusBarTime.setFont(new Font("Segoe UI", Font.BOLD, 12));

    lblNotifIcon = new JLabel("🔔");
    lblNotifIcon.setForeground(Color.YELLOW);
    lblNotifIcon.setVisible(false);

    leftStatus.add(lblStatusBarTime);
    leftStatus.add(lblNotifIcon);

    // PARTE DERECHA: Batería Emoji Verde
    JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    rightStatus.setBackground(Color.BLACK);

    JLabel lblNetwork = new JLabel("5G ");
    lblNetwork.setForeground(Color.WHITE);
    lblNetwork.setFont(new Font("Segoe UI", Font.BOLD, 12));

    JLabel lblBattery = new JLabel("100% ");
    lblBattery.setForeground(Color.WHITE);
    lblBattery.setFont(new Font("Segoe UI", Font.BOLD, 12));

    // ICONO BATERÍA VERDE
    JLabel lblBatIcon = new JLabel("🔋");
    lblBatIcon.setForeground(Color.GREEN);
    lblBatIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

    rightStatus.add(lblNetwork);
    rightStatus.add(lblBattery);
    rightStatus.add(lblBatIcon);

    statusBar.add(leftStatus, BorderLayout.WEST);
    statusBar.add(rightStatus, BorderLayout.EAST);
    bezel.add(statusBar, BorderLayout.NORTH);

    // 2. PANTALLA PRINCIPAL
    layeredPane = new JLayeredPane();
    layeredPane.setLayout(null);
    bezel.add(layeredPane, BorderLayout.CENTER);

    mainOSPanel = new JPanel();
    osLayout = new CardLayout();
    mainOSPanel.setLayout(osLayout);
    mainOSPanel.setBounds(0, 0, 345, 630);

    initApps();

    layeredPane.add(mainOSPanel, Integer.valueOf(0));

    createNotificationBanner();
    layeredPane.add(notificationBanner, Integer.valueOf(1));

    // 3. BOTÓN HOME
    JButton btnHome = new JButton("⏺");
    btnHome.setBackground(new Color(20, 20, 20));
    btnHome.setForeground(Color.WHITE);
    btnHome.setFocusPainted(false);
    btnHome.setBorderPainted(false);
    btnHome.setFont(new Font("SansSerif", Font.BOLD, 20));
    btnHome.addActionListener(e -> osLayout.show(mainOSPanel, "HOME"));
    bezel.add(btnHome, BorderLayout.SOUTH);

    setContentPane(bezel);
    new Timer(1000, e -> lblStatusBarTime.setText(new SimpleDateFormat("HH:mm").format(new Date()))).start();
  }

  private void initApps() {
    initHomeScreen();
    initSmsApp();
    initBankApp();
    initReceiptScreen();
    initDownloadsApp();

    mainOSPanel.add(homeScreen, "HOME");
    mainOSPanel.add(smsApp, "SMS");
    mainOSPanel.add(downloadsApp, "DOWNLOADS");
    mainOSPanel.add(bankAppLogin, "BANK_LOGIN");
    mainOSPanel.add(bankAppDashboard, "BANK_DASH");
    mainOSPanel.add(bankAppTransfer, "BANK_TRANS");
    mainOSPanel.add(bankAppNotifications, "BANK_NOTIF");
    mainOSPanel.add(receiptScreen, "RECEIPT_VIEW");
  }

  // --- NOTIFICACIONES FLOTANTES ---
  private void createNotificationBanner() {
    notificationBanner = new JPanel(new BorderLayout());
    notificationBanner.setBackground(new Color(40, 40, 40));
    notificationBanner.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    notificationBanner.setBounds(10, 10, 325, 60);
    notificationBanner.setVisible(false);

    JLabel icon = new JLabel(" 💬 ");
    icon.setForeground(Color.WHITE);
    icon.setFont(new Font("SansSerif", Font.PLAIN, 24));
    notificationBanner.add(icon, BorderLayout.WEST);

    JPanel txtPanel = new JPanel(new GridLayout(2, 1));
    txtPanel.setOpaque(false);
    lblNotifTitle = new JLabel("Title");
    lblNotifTitle.setForeground(Color.CYAN);
    lblNotifTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
    lblNotifText = new JLabel("Content");
    lblNotifText.setForeground(Color.WHITE);
    lblNotifText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    txtPanel.add(lblNotifTitle);
    txtPanel.add(lblNotifText);
    notificationBanner.add(txtPanel, BorderLayout.CENTER);

    notificationBanner.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        notificationBanner.setVisible(false);
        notifTimer.stop();
        if (lblNotifTitle.getText().contains("SecureBank")) osLayout.show(mainOSPanel, "BANK_NOTIF");
        else if (lblNotifTitle.getText().contains("Descarga")) osLayout.show(mainOSPanel, "DOWNLOADS");
        else osLayout.show(mainOSPanel, "SMS");
      }
    });
  }

  public void showHeadsUp(String title, String msg, Color color) {
    lblNotifTitle.setText(title);
    lblNotifText.setText(msg);
    notificationBanner.setBorder(BorderFactory.createLineBorder(color, 2));
    notificationBanner.setVisible(true);
    if (notifTimer != null && notifTimer.isRunning()) notifTimer.stop();
    notifTimer = new Timer(5000, e -> { notificationBanner.setVisible(false); lblNotifIcon.setVisible(true); });
    notifTimer.setRepeats(false);
    notifTimer.start();
  }

  // --- HOME SCREEN ---
  private void initHomeScreen() {
    homeScreen = new JPanel(new GridLayout(4, 3, 10, 20));
    homeScreen.setBackground(new Color(30, 30, 30));
    homeScreen.setBorder(new EmptyBorder(50, 15, 50, 15));

    JButton btnSms = createAppIcon("✉", "SMS", new Color(100, 200, 100));
    btnSms.addActionListener(e -> { osLayout.show(mainOSPanel, "SMS"); lblNotifIcon.setVisible(false); });

    JButton btnBank = createAppIcon("$", "Bank", new Color(0, 102, 204));
    btnBank.addActionListener(e -> { osLayout.show(mainOSPanel, "BANK_LOGIN"); lblNotifIcon.setVisible(false); });

    JButton btnFiles = createAppIcon("☰", "Docs", new Color(255, 165, 0));
    btnFiles.addActionListener(e -> osLayout.show(mainOSPanel, "DOWNLOADS"));

    homeScreen.add(btnSms);
    homeScreen.add(btnBank);
    homeScreen.add(btnFiles);

    homeScreen.add(createAppIcon("◉", "Cam", Color.GRAY));
    homeScreen.add(createAppIcon("@", "Mail", Color.BLUE));
    homeScreen.add(createAppIcon("♫", "Music", Color.RED));
  }

  private JButton createAppIcon(String symbol, String name, Color bg) {
    String html = "<html><center><span style='font-size:24px; font-weight:bold;'>" + symbol + "</span><br><span style='font-size:9px; font-family:sans-serif; font-weight:bold;'><nobr>" + name + "</nobr></span></center></html>";
    JButton b = new JButton(html);
    b.setBackground(bg);
    b.setForeground(Color.WHITE);
    b.setFocusPainted(false);
    b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    b.setMargin(new Insets(0, 0, 0, 0));
    return b;
  }

  // --- CLASE DATOS ---
  private class NotificationData {
    String title, preview, amount, beneficiary, date, id;
    public NotificationData(String title, String preview, String amount, String beneficiary) {
      this.title = title; this.preview = preview; this.amount = amount; this.beneficiary = beneficiary;
      this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
      this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Override public String toString() { return preview; }
  }

  // --- DETALLE DE NOTIFICACIONES ---
  private void showDetailedNotification(String windowTitle, NotificationData data) {
    String detailMsg = "DETALLE DE OPERACIÓN\n------------------------\nFecha: " + data.date + "\nID Ref: #" + data.id + "\n\nEstado: COMPLETADO ✅\nMensaje:\n\"" + data.preview + "\"\n\nImporte: " + data.amount + " €\nDestinatario: " + data.beneficiary + "\n\nSecureBank Mobile v3.0";
    showWhiteDialog(detailMsg, windowTitle, JOptionPane.INFORMATION_MESSAGE);
  }

  // --- UTILS DIÁLOGOS BLANCOS ---
  private void showWhiteDialog(String message, String title, int type) {
    Object oldBg = UIManager.get("OptionPane.background");
    Object oldMsgFg = UIManager.get("OptionPane.messageForeground");
    Object oldPnlBg = UIManager.get("Panel.background");
    UIManager.put("OptionPane.background", new ColorUIResource(Color.WHITE));
    UIManager.put("Panel.background", new ColorUIResource(Color.WHITE));
    UIManager.put("OptionPane.messageForeground", new ColorUIResource(Color.BLACK));
    JOptionPane.showMessageDialog(this, message, title, type);
    UIManager.put("OptionPane.background", oldBg);
    UIManager.put("Panel.background", oldPnlBg);
    UIManager.put("OptionPane.messageForeground", oldMsgFg);
  }

  private int showWhiteConfirmDialog(String message, String title) {
    Object oldBg = UIManager.get("OptionPane.background");
    Object oldMsgFg = UIManager.get("OptionPane.messageForeground");
    Object oldPnlBg = UIManager.get("Panel.background");
    UIManager.put("OptionPane.background", new ColorUIResource(Color.WHITE));
    UIManager.put("Panel.background", new ColorUIResource(Color.WHITE));
    UIManager.put("OptionPane.messageForeground", new ColorUIResource(Color.BLACK));
    int result = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
    UIManager.put("OptionPane.background", oldBg);
    UIManager.put("Panel.background", oldPnlBg);
    UIManager.put("OptionPane.messageForeground", oldMsgFg);
    return result;
  }

  // --- APP DESCARGAS ---
  private void initDownloadsApp() {
    downloadsApp = new JPanel(new BorderLayout());
    JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT));
    h.setBackground(Color.ORANGE);
    JButton b = new JButton("←");
    b.addActionListener(e -> osLayout.show(mainOSPanel, "HOME"));
    h.add(b);
    h.add(new JLabel("Gestor de Archivos"));
    downloadsListModel = new DefaultListModel<>();
    JList<String> list = new JList<>(downloadsListModel);
    list.setCellRenderer(new BubbleRenderer(new Color(255, 240, 200)));
    list.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          String selected = list.getSelectedValue();
          if (selected == null) return;
          String filename = selected.replace("📄 ", "").trim();
          File file = new File("recibos_movil/" + filename);
          if (file.exists()) {
            try { showHtmlContent(Files.readString(file.toPath()), "DOWNLOADS"); } catch (IOException ex) {}
          } else { showWhiteDialog("Archivo no encontrado.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
      }
    });
    downloadsApp.add(h, BorderLayout.NORTH);
    downloadsApp.add(new JScrollPane(list), BorderLayout.CENTER);
  }

  // --- APP SMS ---
  private void initSmsApp() {
    smsApp = new JPanel(new BorderLayout()); smsApp.setBackground(Color.WHITE);
    JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT)); h.setBackground(new Color(240, 240, 240));
    JButton b = new JButton("←"); b.addActionListener(e -> osLayout.show(mainOSPanel, "HOME")); h.add(b); h.add(new JLabel("Mensajes"));
    smsListModel = new DefaultListModel<>();
    JList<NotificationData> list = new JList<>(smsListModel); list.setCellRenderer(new BubbleRenderer(new Color(220, 255, 220)));
    list.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2 && list.getSelectedValue() != null) showDetailedNotification("Detalle SMS", list.getSelectedValue()); }
    });
    smsApp.add(h, BorderLayout.NORTH); smsApp.add(new JScrollPane(list), BorderLayout.CENTER);
  }

  // --- APP BANCO ---
  private void initBankApp() {
    bankAppLogin = new JPanel(new GridBagLayout());
    bankAppLogin.setBackground(new Color(0, 51, 102));

    JPasswordField txtPin = new JPasswordField(6) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !isFocusOwner()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.GRAY);
          g2.setFont(getFont().deriveFont(Font.ITALIC));
          FontMetrics fm = g2.getFontMetrics();
          int x = (getWidth() - fm.stringWidth("PIN")) / 2;
          int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
          g2.drawString("PIN", x, y);
          g2.dispose();
        }
      }
    };
    txtPin.setHorizontalAlignment(JTextField.CENTER);
    txtPin.setFont(new Font("Arial", Font.BOLD, 24));
    txtPin.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) { repaint(); }
      public void focusLost(java.awt.event.FocusEvent evt) { repaint(); }
    });

    JButton btnLogin = new JButton("ENTRAR");
    btnLogin.setBackground(Color.ORANGE);
    btnLogin.setPreferredSize(new Dimension(150, 40));
    btnLogin.addActionListener(e -> {
      if (new String(txtPin.getPassword()).equals("1234")) { txtPin.setText(""); osLayout.show(mainOSPanel, "BANK_DASH"); }
      else showWhiteDialog("PIN Incorrecto (Prueba 1234)", "Error", JOptionPane.ERROR_MESSAGE);
    });

    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(10, 10, 10, 10);
    g.gridx = 0; g.gridy = 0;

    JLabel lblTitle = new JLabel("SecureBank");
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
    lblTitle.setForeground(Color.WHITE);
    bankAppLogin.add(lblTitle, g);

    g.gridy = 1;
    bankAppLogin.add(Box.createVerticalStrut(30), g);

    g.gridy = 2;
    bankAppLogin.add(txtPin, g);

    g.gridy = 3;
    bankAppLogin.add(btnLogin, g);

    bankAppDashboard = new JPanel(new BorderLayout());
    bankAppDashboard.setBackground(Color.WHITE);
    JPanel head = new JPanel();
    head.setBackground(new Color(0, 102, 204));
    head.setBorder(new EmptyBorder(10, 10, 10, 10));
    head.add(new JLabel("<html><h2 style='color:white'>Hola, Cliente</h2></html>"));
    JPanel menu = new JPanel(new GridLayout(2, 1, 10, 10));
    menu.setBorder(new EmptyBorder(20, 20, 20, 20));
    menu.setOpaque(false);
    JButton b1 = new JButton("Transferir Dinero"); styleBtn(b1);
    b1.addActionListener(e -> osLayout.show(mainOSPanel, "BANK_TRANS"));
    JButton b2 = new JButton("Mis Notificaciones"); styleBtn(b2);
    b2.addActionListener(e -> osLayout.show(mainOSPanel, "BANK_NOTIF"));
    menu.add(b1); menu.add(b2);
    bankAppDashboard.add(head, BorderLayout.NORTH); bankAppDashboard.add(menu, BorderLayout.CENTER);

    initTransferScreen();
    initNotificationScreen();
  }

  private void styleBtn(JButton b) {
    b.setBackground(Color.WHITE);
    b.setFont(new Font("Segoe UI", Font.BOLD, 14));
    b.setForeground(new Color(0, 102, 204));
    b.setFocusPainted(false);
  }

  private void initTransferScreen() {
    bankAppTransfer = new JPanel(new BorderLayout());
    bankAppTransfer.setBackground(Color.WHITE);

    JPanel h = new JPanel(new BorderLayout());
    h.setBackground(new Color(0, 102, 204));
    h.setBorder(new EmptyBorder(15, 10, 15, 10));

    JButton btnBack = new JButton(" ← ");
    btnBack.setForeground(Color.WHITE);
    btnBack.setBackground(new Color(0, 80, 180));
    btnBack.setFocusPainted(false);
    btnBack.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    btnBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnBack.addActionListener(e -> osLayout.show(mainOSPanel, "BANK_DASH"));

    JLabel lblTitle = new JLabel("Nueva Transferencia", SwingConstants.CENTER);
    lblTitle.setForeground(Color.WHITE);
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

    h.add(btnBack, BorderLayout.WEST);
    h.add(lblTitle, BorderLayout.CENTER);
    h.add(new JLabel("    "), BorderLayout.EAST);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(Color.WHITE);
    formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 0, 5, 0);
    gbc.weightx = 1.0;

    comboAccounts = new JComboBox<>(repository.getAccountsArray());
    comboAccounts.setRenderer(new AccountRenderer());

    // --- AQUÍ APLICAMOS EL PLACEHOLDER EN LUGAR DEL TEXTO FIJO ---
    txtIban = createPlaceholderField("Ej: ES88 2038...");
    txtBeneficiary = createPlaceholderField("Ej: Empresa S.L.");
    txtAmount = createPlaceholderField("0.00");
    txtConcept = createPlaceholderField("Ej: Pago servicios");

    addFormField(formPanel, "CUENTA ORIGEN", comboAccounts, gbc);
    addFormField(formPanel, "DESTINATARIO (IBAN)", txtIban, gbc);
    addFormField(formPanel, "BENEFICIARIO", txtBeneficiary, gbc);
    addFormField(formPanel, "IMPORTE (€)", txtAmount, gbc);
    addFormField(formPanel, "CONCEPTO", txtConcept, gbc);

    gbc.insets = new Insets(25, 0, 0, 0);

    JButton btnSend = new JButton("CONFIRMAR OPERACIÓN");
    btnSend.setBackground(new Color(0, 153, 76));
    btnSend.setForeground(Color.WHITE);
    btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnSend.setFocusPainted(false);
    btnSend.setPreferredSize(new Dimension(0, 45));
    btnSend.addActionListener(e -> doTransfer());

    formPanel.add(btnSend, gbc);
    gbc.weighty = 1.0;
    formPanel.add(Box.createVerticalGlue(), gbc);

    bankAppTransfer.add(h, BorderLayout.NORTH);
    JScrollPane scroll = new JScrollPane(formPanel);
    scroll.setBorder(null);
    bankAppTransfer.add(scroll, BorderLayout.CENTER);
  }

  // MÉTODO PARA CREAR CAMPOS CON TEXTO DE FONDO (PLACEHOLDER)
  private JTextField createPlaceholderField(String placeholder) {
    JTextField field = new JTextField() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.GRAY);
          g2.setFont(getFont().deriveFont(Font.ITALIC));
          int padding = 10;
          int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
          g2.drawString(placeholder, padding, y);
          g2.dispose();
        }
      }
    };
    // Listeners para repintar al entrar/salir
    field.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) { repaint(); }
      public void focusLost(java.awt.event.FocusEvent evt) { repaint(); }
    });
    return field;
  }

  private void addFormField(JPanel p, String label, JComponent field, GridBagConstraints gbc) {
    JLabel l = new JLabel(label);
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    l.setFont(new Font("Segoe UI", Font.BOLD, 11));
    l.setForeground(Color.GRAY);

    if (field instanceof JTextField) {
      field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      field.setPreferredSize(new Dimension(0, 35));
      ((JTextField)field).setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    } else if (field instanceof JComboBox) {
      field.setPreferredSize(new Dimension(0, 50));
    }

    gbc.insets = new Insets(10, 0, 2, 0);
    p.add(l, gbc);
    gbc.insets = new Insets(0, 0, 0, 0);
    p.add(field, gbc);
  }

  // --- NOTIFICACIONES BANCO ---
  private void initNotificationScreen() {
    bankAppNotifications = new JPanel(new BorderLayout());
    JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton b = new JButton("←");
    b.addActionListener(e -> osLayout.show(mainOSPanel, "BANK_DASH"));
    h.add(b); h.add(new JLabel("Buzón Seguro"));

    bankNotifListModel = new DefaultListModel<>();
    JList<NotificationData> list = new JList<>(bankNotifListModel);
    list.setCellRenderer(new BubbleRenderer(new Color(200, 230, 255)));

    list.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
          showDetailedNotification("Notificación Bancaria", list.getSelectedValue());
        }
      }
    });

    bankAppNotifications.add(h, BorderLayout.NORTH);
    bankAppNotifications.add(new JScrollPane(list), BorderLayout.CENTER);
  }

  private void doTransfer() {
    try {
      String amountStr = txtAmount.getText().replace(",", ".");
      if (amountStr.isEmpty()) throw new NumberFormatException();
      double amount = Double.parseDouble(amountStr);

      String accountText = (String) comboAccounts.getSelectedItem();
      double balance = extractBalance(accountText);
      String ben = txtBeneficiary.getText().toUpperCase();
      String concept = txtConcept.getText().toUpperCase();

      String sourceIban = accountText.split("\\|")[0].trim();

      if (amount > balance) {
        showWhiteDialog("No dispone de saldo suficiente para esta operación.\n\nSaldo: " + balance + " €", "Fondos Insuficientes", JOptionPane.ERROR_MESSAGE);
        engine.reportIncident("FALLO_SALDO", "Intento fallido por saldo insuficiente. Cliente intentó enviar " + amount + "€");
        return;
      }

      if (amount <= 0) {
        showWhiteDialog("El importe debe ser mayor que 0.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      List<String> blackList = repository.getBlackList();
      for (String term : blackList) {
        if (ben.contains(term) || concept.contains(term)) {
          showWhiteDialog("Operación rechazada por políticas de seguridad.\nCódigo: R-SEC-09", "Bloqueo de Seguridad", JOptionPane.ERROR_MESSAGE);
          engine.reportIncident("BLOQUEADO", "Intento de fraude detectado en App Móvil. Palabra clave: " + term);
          return;
        }
      }

      lastAmount = txtAmount.getText();
      lastBeneficiary = txtBeneficiary.getText();
      new Thread(() -> {
        try {
          Thread.sleep(500);
          engine.executeTransfer(amount, sourceIban, txtIban.getText() + "|" + txtBeneficiary.getText() + "|" + txtConcept.getText());
          SwingUtilities.invokeLater(() -> {
            osLayout.show(mainOSPanel, "BANK_DASH");
            updateAccountBalanceInUI(amount);
          });
        } catch (Exception e) {}
      }).start();

    } catch (NumberFormatException e) { showWhiteDialog("Por favor, introduzca un importe válido.", "Error de Datos", JOptionPane.WARNING_MESSAGE); }
  }

  @Override
  public void onTransactionExecuted(String data) {
    if (data.contains("BLOQUEADO") || data.contains("FALLO")) return;
    String amount = txtAmount.getText();
    String ben = txtBeneficiary.getText();
    String con = txtConcept.getText();
    SwingUtilities.invokeLater(() -> {
      NotificationData sms = new NotificationData("Pago Tarjeta", "Pago de " + amount + "€ realizado.", amount, ben);
      NotificationData bank = new NotificationData("Transferencia", "Envío a " + ben + " completado.", amount, ben);
      smsListModel.addElement(sms);
      bankNotifListModel.addElement(bank);
      showHeadsUp("SecureBank", "Transferencia Exitosa", new Color(0, 102, 204));
      showWhiteDialog("✅ OPERACIÓN REALIZADA CON ÉXITO\n\nSe ha enviado el dinero correctamente.", "Operación Confirmada", JOptionPane.INFORMATION_MESSAGE);
      int opt = showWhiteConfirmDialog("¿Desea descargar el justificante oficial?", "Justificante");
      if (opt == JOptionPane.YES_OPTION) generateAndShowReceipt(ben, amount, con);
    });
  }

  private void initReceiptScreen() {
    receiptScreen = new JPanel(new BorderLayout());
    JPanel h = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnSave = new JButton("💾 GUARDAR"); btnSave.setBackground(new Color(0, 100, 200)); btnSave.setForeground(Color.WHITE);
    JButton btnClose = new JButton("❌ CERRAR"); btnClose.setBackground(Color.RED); btnClose.setForeground(Color.WHITE);
    h.add(btnSave); h.add(btnClose);
    JEditorPane htmlPane = new JEditorPane(); htmlPane.setContentType("text/html"); htmlPane.setEditable(false);
    btnClose.addActionListener(e -> osLayout.show(mainOSPanel, "BANK_DASH"));
    btnSave.addActionListener(e -> {
      String name = "Justificante_" + System.currentTimeMillis() + ".html";
      saveFileToDownloads(name, htmlPane.getText());
      downloadsListModel.addElement("📄 " + name);
      showHeadsUp("Descarga Completa", "Guardado en Archivos", Color.ORANGE);
      osLayout.show(mainOSPanel, "DOWNLOADS");
    });
    receiptScreen.add(h, BorderLayout.NORTH); receiptScreen.add(new JScrollPane(htmlPane), BorderLayout.CENTER);
    receiptScreen.putClientProperty("htmlPane", htmlPane); receiptScreen.putClientProperty("btnClose", btnClose);
  }

  private void saveFileToDownloads(String name, String content) {
    try { File dir = new File("recibos_movil"); if (!dir.exists()) dir.mkdir(); Files.writeString(new File(dir, name).toPath(), content); } catch (IOException e) { e.printStackTrace(); }
  }

  private void showHtmlContent(String htmlContent, String returnScreen) {
    JEditorPane pane = (JEditorPane) receiptScreen.getClientProperty("htmlPane");
    JButton btnClose = (JButton) receiptScreen.getClientProperty("btnClose");
    for (var l : btnClose.getActionListeners()) btnClose.removeActionListener(l);
    btnClose.addActionListener(e -> osLayout.show(mainOSPanel, returnScreen));
    pane.setText(htmlContent);
    osLayout.show(mainOSPanel, "RECEIPT_VIEW");
  }

  private void generateAndShowReceipt(String ben, String amt, String con) {
    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    // El hash lo calculamos pero ya no lo mostramos en la pantalla del móvil
    // String hash = "SHA-" + (time + amt).hashCode();

    // HTML LIMPIO (Sin firma técnica)
    String html = "<html><body style='font-family:Arial; padding:15px;'>" +
      "<h2 style='color:#0066cc; text-align:center;'>Operación Exitosa</h2>" +
      "<hr>" +
      "<p><strong>Fecha:</strong> " + time + "</p>" +
      "<p><strong>Beneficiario:</strong> " + ben + "</p>" +
      "<p><strong>Concepto:</strong> " + con + "</p>" +
      "<h1 style='color:#d32f2f; text-align:center; margin-top:20px;'>-" + amt + " €</h1>" +
      "<hr>" +
      "<p style='text-align:center; color:gray; font-size:10px;'>Comprobante válido generado por SecureBank App</p>" +
      "</body></html>";

    showHtmlContent(html, "BANK_DASH");
  }
  private double extractBalance(String t) { try { return Double.parseDouble(t.substring(t.indexOf("Saldo: ") + 7, t.indexOf(" €")).replace(".", "").replace(",", ".")); } catch (Exception e) { return 0; } }
  private void updateAccountBalanceInUI(double amountDeducted) {
    int index = comboAccounts.getSelectedIndex(); String text = (String) comboAccounts.getSelectedItem();
    double newBal = extractBalance(text) - amountDeducted;
    DecimalFormatSymbols sym = new DecimalFormatSymbols(); sym.setGroupingSeparator('.'); sym.setDecimalSeparator(',');
    DecimalFormat df = new DecimalFormat("#,##0.00", sym);
    String newText = text.replaceAll("Saldo: .* €\\)", "Saldo: " + df.format(newBal) + " €)");
    comboAccounts.removeItemAt(index); comboAccounts.insertItemAt(newText, index); comboAccounts.setSelectedIndex(index);
  }
  class BubbleRenderer extends DefaultListCellRenderer {
    Color bg; public BubbleRenderer(Color c) { this.bg = c; }
    public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
      JLabel lbl = (JLabel) super.getListCellRendererComponent(l,v,i,s,f); lbl.setBackground(bg); lbl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); return lbl;
    }
  }
  class AccountRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value != null) {
        try {
          String text = value.toString();
          String[] parts = text.split("\\|");
          if (parts.length >= 2) {
            String iban = parts[0].trim();
            String info = parts[1].trim();
            String html = "<html><b>" + info + "</b><br><font size='2' color='gray'>" + iban + "</font></html>";
            label.setText(html);
          }
        } catch (Exception e) {}
      }
      return label;
    }
  }
}