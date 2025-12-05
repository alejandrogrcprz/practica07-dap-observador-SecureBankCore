package com.securebank.main;

import com.securebank.core.TransactionEngine;
import com.securebank.interfaces.IBankObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MobilePhoneSimulator extends JFrame implements IBankObserver {

  private TransactionEngine engine;
  private IBankRepository repository;

  // SESIÓN Y DATOS
  private List<String> myIbans = new ArrayList<>();
  private boolean isBankLoggedIn = false;
  private long lastTransferTimestamp = 0; // Para el Velocity Check

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

  // LISTAS DATOS
  private DefaultListModel<NotificationData> smsListModel;
  private DefaultListModel<NotificationData> bankNotifListModel;
  private DefaultListModel<String> downloadsListModel;

  // COMPONENTES FORMULARIOS
  private JComboBox<String> comboAccounts;
  private JTextField txtIban, txtBeneficiary, txtAmount, txtConcept;
  private JTextField txtUserLogin;

  public MobilePhoneSimulator(TransactionEngine engine, IBankRepository repository) {
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

    // BARRA DE ESTADO
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

    JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    rightStatus.setBackground(Color.BLACK);
    JLabel lblNetwork = new JLabel("5G ");
    lblNetwork.setForeground(Color.WHITE);
    lblNetwork.setFont(new Font("Segoe UI", Font.BOLD, 12));
    JLabel lblBattery = new JLabel("100% ");
    lblBattery.setForeground(Color.WHITE);
    lblBattery.setFont(new Font("Segoe UI", Font.BOLD, 12));
    JLabel lblBatIcon = new JLabel("🔋");
    lblBatIcon.setForeground(Color.GREEN);
    lblBatIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

    rightStatus.add(lblNetwork);
    rightStatus.add(lblBattery);
    rightStatus.add(lblBatIcon);

    statusBar.add(leftStatus, BorderLayout.WEST);
    statusBar.add(rightStatus, BorderLayout.EAST);
    bezel.add(statusBar, BorderLayout.NORTH);

    // PANTALLA PRINCIPAL
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

    // BOTÓN HOME
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
        if(notifTimer!=null) notifTimer.stop();
        if (lblNotifTitle.getText().contains("Recibido") || lblNotifTitle.getText().contains("Enviado"))
          osLayout.show(mainOSPanel, "BANK_NOTIF");
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
    btnBank.addActionListener(e -> {
      lblNotifIcon.setVisible(false);
      if (isBankLoggedIn) osLayout.show(mainOSPanel, "BANK_DASH");
      else osLayout.show(mainOSPanel, "BANK_LOGIN");
    });

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
    String typeTitle, sourceName, concept, sourceIban, destIban;
    boolean canDownloadReceipt;

    public NotificationData(String title, String msg, String amt, String ben, boolean canReceipt) {
      this.title = title;
      this.preview = msg;
      this.amount = amt;
      this.beneficiary = ben;
      this.canDownloadReceipt = canReceipt;
      this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
      this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Método para inyectar los datos del recibo
    public void setReceiptDetails(String type, String from, String to, String amt, String con, String iban1, String iban2) {
      this.typeTitle = type;
      this.sourceName = from;
      this.beneficiary = to;
      this.amount = amt;
      this.concept = con;
      this.sourceIban = iban1;
      this.destIban = iban2;
    }

    @Override public String toString() { return preview; }
  }

  // --- DETALLE DE NOTIFICACIÓN ---
  private void showDetailedNotification(String windowTitle, NotificationData data) {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBackground(Color.WHITE);

    JTextArea txt = new JTextArea();
    txt.setText("DETALLE DE OPERACIÓN\n------------------------\n" +
      "Fecha: " + data.date + "\nID Ref: #" + data.id + "\n\n" +
      "Mensaje:\n\"" + data.preview + "\"\n\n" +
      "Importe: " + data.amount + " €\n");
    txt.setEditable(false);
    txt.setBackground(Color.WHITE);
    txt.setFont(new Font("Consolas", Font.PLAIN, 12));

    p.add(txt, BorderLayout.CENTER);

    if (data.canDownloadReceipt) {
      JButton btnDown = new JButton("📥 DESCARGAR JUSTIFICANTE");
      btnDown.setBackground(new Color(0, 102, 204));
      btnDown.setForeground(Color.WHITE);
      btnDown.setFocusPainted(false);

      btnDown.addActionListener(ev -> {
        Window w = SwingUtilities.getWindowAncestor(btnDown);
        if (w != null) w.dispose();

        generateAndShowReceipt(data.typeTitle, data.sourceName, data.beneficiary, data.amount, data.concept, data.sourceIban, data.destIban);
      });
      p.add(btnDown, BorderLayout.SOUTH);
    }

    styleDialogs();
    JOptionPane.showMessageDialog(this, p, windowTitle, JOptionPane.PLAIN_MESSAGE);
  }

  private void showWhiteDialog(String message, String title, int type) {
    styleDialogs();
    JOptionPane.showMessageDialog(this, message, title, type);
  }

  private int showWhiteConfirmDialog(String message, String title) {
    styleDialogs();
    return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
  }

  private void styleDialogs() {
    UIManager.put("OptionPane.background", new ColorUIResource(Color.WHITE));
    UIManager.put("Panel.background", new ColorUIResource(Color.WHITE));
    UIManager.put("OptionPane.messageForeground", new ColorUIResource(Color.BLACK));
  }

  // --- APP ARCHIVOS ---
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

    txtUserLogin = new JTextField(15) {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.GRAY);
          g2.setFont(getFont().deriveFont(Font.ITALIC));
          int x = (getWidth() - g2.getFontMetrics().stringWidth("DNI / NIE")) / 2;
          int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
          g2.drawString("DNI / NIE", x, y);
          g2.dispose();
        }
      }
    };
    txtUserLogin.setHorizontalAlignment(JTextField.CENTER);
    txtUserLogin.setFont(new Font("Arial", Font.PLAIN, 16));
    txtUserLogin.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) { repaint(); }
      public void focusLost(java.awt.event.FocusEvent evt) { repaint(); }
    });

    JPasswordField txtPin = new JPasswordField(6) {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !isFocusOwner()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.GRAY);
          g2.setFont(getFont().deriveFont(Font.ITALIC));
          int x = (getWidth() - g2.getFontMetrics().stringWidth("PIN")) / 2;
          int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
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
      String dni = txtUserLogin.getText().trim();
      String pin = new String(txtPin.getPassword());

      if (!pin.equals("1234")) { showWhiteDialog("PIN Incorrecto (Prueba 1234)", "Error", JOptionPane.ERROR_MESSAGE); return; }
      if (dni.isEmpty()) { showWhiteDialog("Introduzca su DNI.", "Error", JOptionPane.WARNING_MESSAGE); return; }

      List<String> userAccounts = repository.findAccountsByDNI(dni);

      if (userAccounts.isEmpty()) {
        showWhiteDialog("DNI no encontrado o sin cuentas.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
      } else {
        comboAccounts.removeAllItems();
        myIbans.clear();
        for(String acc : userAccounts) {
          comboAccounts.addItem(acc);
          try { myIbans.add(acc.split("\\|")[0].trim()); } catch(Exception ex) {}
        }
        isBankLoggedIn = true;
        txtUserLogin.setText("");
        txtPin.setText("");
        osLayout.show(mainOSPanel, "BANK_DASH");
      }
    });

    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(10, 10, 10, 10);
    g.gridx = 0; g.gridy = 0;
    JLabel lblTitle = new JLabel("SecureBank");
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
    lblTitle.setForeground(Color.WHITE);
    bankAppLogin.add(lblTitle, g);
    g.gridy = 1; bankAppLogin.add(Box.createVerticalStrut(20), g);
    g.gridy = 2; bankAppLogin.add(txtUserLogin, g);
    g.gridy = 3; bankAppLogin.add(txtPin, g);
    g.gridy = 4; bankAppLogin.add(btnLogin, g);

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

    JButton b3 = new JButton("Cerrar Sesión");
    b3.setBackground(new Color(200, 200, 200));
    b3.setForeground(Color.BLACK);
    b3.setFocusPainted(false);
    b3.addActionListener(e -> {
      isBankLoggedIn = false;
      myIbans.clear();
      osLayout.show(mainOSPanel, "BANK_LOGIN");
    });

    menu.add(b1); menu.add(b2);
    bankAppDashboard.add(head, BorderLayout.NORTH);
    bankAppDashboard.add(menu, BorderLayout.CENTER);
    bankAppDashboard.add(b3, BorderLayout.SOUTH);

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

    comboAccounts = new JComboBox<>();
    comboAccounts.setRenderer(new AccountRenderer());

    txtIban = createPlaceholderField("Ej: ES88 2038...");
    txtBeneficiary = createPlaceholderField("Ej: Empresa S.L.");
    txtAmount = createPlaceholderField("0.00");
    txtConcept = createPlaceholderField("Ej: Pago servicios");

    txtIban.addFocusListener(new java.awt.event.FocusAdapter() {
      @Override
      public void focusLost(java.awt.event.FocusEvent evt) {
        String typedIban = txtIban.getText().trim();
        if (!typedIban.isEmpty()) {
          String foundName = repository.getBeneficiaryNameByIBAN(typedIban);
          if (foundName != null) {
            txtBeneficiary.setText(foundName);
            txtBeneficiary.setBackground(new Color(230, 255, 230));
          } else {
            txtBeneficiary.setBackground(Color.WHITE);
          }
        }
      }
    });

    addFormField(formPanel, "CUENTA ORIGEN (*)", comboAccounts, gbc);
    addFormField(formPanel, "DESTINATARIO (IBAN) (*)", txtIban, gbc);
    addFormField(formPanel, "BENEFICIARIO (*)", txtBeneficiary, gbc);
    addFormField(formPanel, "IMPORTE (€) (*)", txtAmount, gbc);
    addFormField(formPanel, "CONCEPTO (*)", txtConcept, gbc);

    JLabel lblObligatory = new JLabel("(*) Campos obligatorios");
    lblObligatory.setForeground(new Color(200, 50, 50));
    lblObligatory.setFont(new Font("Segoe UI", Font.ITALIC, 10));
    gbc.insets = new Insets(5, 0, 15, 0);
    formPanel.add(lblObligatory, gbc);

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

  private JTextField createPlaceholderField(String placeholder) {
    JTextField field = new JTextField() {
      @Override protected void paintComponent(Graphics g) {
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
      ((JTextField)field).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
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

    // Doble clic abre detalle con botón descargar
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
    // 1. VALIDACIONES LOCALES (FORMULARIO) - Esto es instantáneo
    if (comboAccounts.getSelectedItem() == null || txtIban.getText().trim().isEmpty() ||
      txtBeneficiary.getText().trim().isEmpty() || txtAmount.getText().trim().isEmpty() ||
      txtConcept.getText().trim().isEmpty()) {
      showWhiteDialog("Todos los campos marcados con (*) son obligatorios.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try {
      String amountStr = txtAmount.getText().replace(",", ".");
      double amount = Double.parseDouble(amountStr);
      if (amount <= 0) { showWhiteDialog("Importe inválido.", "Error", JOptionPane.ERROR_MESSAGE); return; }

      String accountText = (String) comboAccounts.getSelectedItem();
      String sourceIban = accountText.split("\\|")[0].trim();
      String destIban = txtIban.getText().trim().toUpperCase();

      // Extracción segura del nombre
      String tempName = "Usuario";
      try {
        int dashIndex = accountText.indexOf("- ");
        int parenIndex = accountText.indexOf(" (");
        if (dashIndex != -1 && parenIndex != -1) tempName = accountText.substring(dashIndex + 2, parenIndex).trim();
      } catch (Exception e) {}
      String sourceName = tempName;

      double balance = extractBalance(accountText);
      String ben = txtBeneficiary.getText().toUpperCase();
      String concept = txtConcept.getText().toUpperCase();

      if (amount > balance) {
        showWhiteDialog("No dispone de saldo suficiente.", "Fondos Insuficientes", JOptionPane.ERROR_MESSAGE);
        engine.reportIncident("FALLO_SALDO", "Saldo insuficiente (" + amount + "€)");
        return;
      }

      // =======================================================================
      //   PROCESO ASÍNCRONO DE SEGURIDAD (SIMULACIÓN SERVIDOR)
      // =======================================================================
      // Lanzamos un hilo para no congelar la pantalla mientras "pensamos"
      new Thread(() -> {

        // Simular tiempo de conexión con el servidor (Feedback visual)
        SwingUtilities.invokeLater(() -> {
          // Opcional: Podrías cambiar el cursor o deshabilitar el botón aquí
          System.out.println(">> MÓVIL: Conectando con servidor de seguridad...");
        });

        try { Thread.sleep(800); } catch (Exception e) {} // Pequeña espera realista

        // --- REGLA 0: LISTA NEGRA (CSV) ---
        List<String> badWords = repository.getCsvBlockedConcepts();
        for (String badWord : badWords) {
          if (concept.contains(badWord) || ben.contains(badWord)) {
            // 1. AVISAMOS AL DASHBOARD (Para que empiece la animación YA)
            engine.reportIncident("BLOQUEADO", "STEP4_BLACKLIST|Concepto Prohibido: " + badWord);

            // 2. ESPERAMOS UN POCO (Para que se vea la animación en el dashboard)
            try { Thread.sleep(1000); } catch (Exception e) {}

            // 3. MOSTRAMOS EL BLOQUEO AL USUARIO
            SwingUtilities.invokeLater(() ->
              showWhiteDialog("Operación rechazada por política de contenidos.\nConcepto no permitido.", "Bloqueo de Seguridad", JOptionPane.ERROR_MESSAGE)
            );
            return; // Cortamos ejecución
          }
        }

        // --- REGLA 1: VELOCITY CHECK ---
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastTransferTimestamp) < 10000 && lastTransferTimestamp != 0) {
          engine.reportIncident("BLOQUEADO", "STEP1_VELOCITY|Alta Velocidad detectada (<10s)");
          try { Thread.sleep(1000); } catch (Exception e) {}

          SwingUtilities.invokeLater(() ->
            showWhiteDialog("Operación bloqueada temporalmente.\nDemasiadas peticiones seguidas.", "Velocity Check", JOptionPane.ERROR_MESSAGE)
          );
          return;
        }

        // --- REGLA 2: PATRÓN NUMÉRICO ---
        if (amount >= 1000 && (amount % 500 == 0)) {
          engine.reportIncident("BLOQUEADO", "STEP2_PATTERN|Patrón Numérico Sospechoso (" + amount + "€)");
          try { Thread.sleep(1000); } catch (Exception e) {}

          SwingUtilities.invokeLater(() ->
            showWhiteDialog("Esta operación requiere justificación de fondos.\nPatrón de importe inusual.", "Revisión Manual", JOptionPane.WARNING_MESSAGE)
          );
          return;
        }

        // --- REGLA 3: FUGA CAPITALES ---
        if (!destIban.startsWith("ES") && amount > 3000) {
          engine.reportIncident("BLOQUEADO", "STEP3_GEO|Fuga Capitales (IBAN " + destIban.substring(0,2) + ")");
          try { Thread.sleep(1000); } catch (Exception e) {}

          SwingUtilities.invokeLater(() ->
            showWhiteDialog("Transferencia internacional bloqueada.\nDestino fuera de zona permitida.", "Bloqueo Geográfico", JOptionPane.ERROR_MESSAGE)
          );
          return;
        }

        // --- REGLA 4: SEGURIDAD TÉCNICA ---
        if (concept.matches(".*[<>\\{\\}\\[\\];].*")) {
          engine.reportIncident("BLOQUEADO", "STEP4_BLACKLIST|Intento de inyección técnica");
          SwingUtilities.invokeLater(() ->
            showWhiteDialog("Caracteres inválidos detectados.", "Error Técnico", JOptionPane.ERROR_MESSAGE)
          );
          return;
        }

        // =======================================================================
        //   SI LLEGA AQUÍ -> TRANSACCIÓN EXITOSA (OK)
        // =======================================================================

        lastTransferTimestamp = System.currentTimeMillis();
        repository.processTransfer(sourceIban, destIban, amount);

        // Avisamos al motor del ÉXITO
        engine.executeTransfer(amount, sourceIban, destIban, ben, concept, sourceName);

        SwingUtilities.invokeLater(() -> osLayout.show(mainOSPanel, "BANK_DASH"));

      } ).start(); // FIN DEL HILO

    } catch (NumberFormatException e) {
      showWhiteDialog("Importe inválido.", "Error", JOptionPane.WARNING_MESSAGE);
    }
  }

  // --- CEREBRO OBSERVADOR ---
  @Override
  public void onTransactionExecuted(String data) {
    if (!data.startsWith("TX##")) return;
    String[] parts = data.split("##");
    if(parts.length < 7) return;

    String sourceIban = parts[1];
    String destIban = parts[2];
    String amountStr = parts[3];
    String benName = parts[4];
    String concept = parts[5];
    String sourceName = parts[6];

    boolean isMineOutgoing = myIbans.contains(sourceIban);
    boolean isMineIncoming = myIbans.contains(destIban);

    if (!isMineOutgoing && !isMineIncoming) return;

    SwingUtilities.invokeLater(() -> {
      String title, msg, amountSign;
      Color color;
      double amountVal = Double.parseDouble(amountStr);

      if (isMineOutgoing) {
        // EMISOR: PREGUNTA INMEDIATA
        title = "Pago Realizado";
        msg = "Has enviado " + amountVal + "€ a " + benName;
        amountSign = "-" + amountVal;
        color = new Color(200, 50, 50);
        updateAccountBalanceInUI(amountVal, true);
        showHeadsUp(title, msg, color);

        Timer t = new Timer(500, e -> {
          int opt = showWhiteConfirmDialog("Transferencia realizada.\n¿Desea descargar el justificante?", "Justificante");
          if (opt == JOptionPane.YES_OPTION) generateAndShowReceipt("ENVÍO DE DINERO", sourceName, benName, amountStr, concept, sourceIban, destIban);
        });
        t.setRepeats(false); t.start();

      } else {
        // RECEPTOR: SOLO NOTIFICACIÓN (DESCARGA EN DETALLE)
        title = "Dinero Recibido";
        msg = "Has recibido " + amountVal + "€ de " + sourceName;
        amountSign = "+" + amountVal;
        color = new Color(50, 150, 50);
        updateAccountBalanceInUI(amountVal, false);

        // Solo Banner, sin interrupción
        showHeadsUp(title, msg, color);
      }

      // CREAR NOTIFICACIÓN CON DATOS DE RECIBO
      NotificationData notif = new NotificationData(title, msg, amountSign, benName, true);
      if (isMineOutgoing) notif.setReceiptDetails("ENVÍO DE DINERO", sourceName, benName, amountStr, concept, sourceIban, destIban);
      else notif.setReceiptDetails("RECEPCIÓN DE DINERO", sourceName, "TÚ (" + benName + ")", amountStr, concept, sourceIban, destIban);

      bankNotifListModel.add(0, notif);
      smsListModel.add(0, notif);
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

  private void generateAndShowReceipt(String typeTitle, String from, String to, String amt, String con, String ibanFrom, String ibanTo) {
    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    // COLORES DINÁMICOS
    String colorHex = "#333333";
    String sign = "";
    if (typeTitle.toUpperCase().contains("ENVÍO")) { colorHex = "#d32f2f"; sign = "-"; }
    else if (typeTitle.toUpperCase().contains("RECEPCIÓN")) { colorHex = "#2e7d32"; sign = "+"; }

    String html = "<html><body style='font-family:SansSerif; padding:20px; background-color:#f9f9f9;'>" +
      "<div style='border:1px solid #ccc; padding:15px; background-color:white; box-shadow: 0px 4px 10px rgba(0,0,0,0.1);'>" +
      "<h2 style='color:#003366; text-align:center; border-bottom:2px solid #003366; padding-bottom:10px;'>" + typeTitle + "</h2>" +
      "<p style='text-align:right; font-size:10px; color:gray;'>Ref: " + UUID.randomUUID().toString().substring(0,12).toUpperCase() + "</p>" +
      "<table style='width:100%; margin-top:20px; border-collapse: collapse;'>" +
      "<tr><td style='padding:5px;'><strong>Fecha:</strong></td><td>" + time + "</td></tr>" +
      "<tr><td style='padding:5px; padding-top:10px;'><strong>Ordenante:</strong></td><td style='padding-top:10px;'>" + from + "</td></tr>" +
      "<tr><td></td><td><font size='2' color='gray'>" + ibanFrom + "</font></td></tr>" +
      "<tr><td style='padding:5px; padding-top:10px;'><strong>Beneficiario:</strong></td><td style='padding-top:10px;'>" + to + "</td></tr>" +
      "<tr><td></td><td><font size='2' color='gray'>" + ibanTo + "</font></td></tr>" +
      "<tr><td style='padding:5px; padding-top:10px;'><strong>Concepto:</strong></td><td style='padding-top:10px;'><i>" + con + "</i></td></tr>" +
      "</table>" +
      "<div style='text-align:center; margin-top:30px; background-color:#f0f0f0; padding:15px; border-radius:5px;'>" +
      "<h1 style='margin:0; color:" + colorHex + "; font-size: 32px;'>" + sign + " " + amt + " €</h1>" +
      "</div>" +
      "<p style='text-align:center; color:#2e7d32; font-weight:bold; margin-top:20px;'>✔ OPERACIÓN COMPLETADA</p>" +
      "<hr style='border: 0; border-top: 1px solid #eee; margin-top: 20px;'>" +
      "<p style='text-align:center; color:#aaa; font-size:9px;'>SecureBank Digital Receipt • Validez legal según normativa vigente.</p>" +
      "</div></body></html>";
    showHtmlContent(html, "BANK_DASH");
  }

  private double extractBalance(String t) {
    try { return Double.parseDouble(t.substring(t.indexOf("Saldo: ") + 7, t.indexOf(" €")).replace(".", "").replace(",", ".")); } catch (Exception e) { return 0; }
  }

  private void updateAccountBalanceInUI(double amount, boolean subtract) {
    if(comboAccounts.getSelectedItem() == null) return;
    int index = comboAccounts.getSelectedIndex();
    String text = (String) comboAccounts.getSelectedItem();
    double currentBal = extractBalance(text);
    double newBal = subtract ? (currentBal - amount) : (currentBal + amount);
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
    @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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