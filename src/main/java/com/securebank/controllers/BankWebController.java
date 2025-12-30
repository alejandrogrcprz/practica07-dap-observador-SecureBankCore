package com.securebank.controllers;

import com.securebank.models.Account;
import com.securebank.models.Transaction;
import com.securebank.models.User;
import com.securebank.repositories.AccountRepository;
import com.securebank.repositories.TransactionRepository;
import com.securebank.repositories.UserRepository;
import com.securebank.services.BankService;

// --- IMPORTS DE JAVA ESTÁNDAR ---
import java.io.IOException;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List; // <--- ¡ESTA LÍNEA ES LA CLAVE! Desempata el conflicto.

// --- IMPORTS PARA EL PDF (OpenPDF) ---
// Importamos solo lo necesario para no chocar con 'List'
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

// --- IMPORTS DE SPRING ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class BankWebController {

  @Autowired private UserRepository userRepo;
  @Autowired private AccountRepository accRepo;
  @Autowired private BankService bankService;
  @Autowired private TransactionRepository txRepo;

  // --- MOTOR DE CATEGORIZACIÓN ---
  private static final Map<String, List<String>> CATEGORY_RULES = new HashMap<>();
  static {
    CATEGORY_RULES.put("Ropa 👕", Arrays.asList("zara", "h&m", "hym", "primark", "pull", "bear", "stradivarius", "bershka", "shein", "zalando", "nike", "adidas", "decathlon", "cortefiel", "mango", "uniqlo"));
    CATEGORY_RULES.put("Alimentación 🛒", Arrays.asList("mercadona", "lidl", "carrefour", "dia", "aldi", "eroski", "consum", "alcampo", "bonarea", "super", "fruteria", "panaderia"));
    CATEGORY_RULES.put("Ocio y Comida 🍔", Arrays.asList("restaurante", "bar", "cafe", "starbucks", "mcdonalds", "burger", "king", "kfc", "uber eats", "glovo", "just eat", "cine", "netflix", "spotify", "hbo", "steam", "playstation", "entrada"));
    CATEGORY_RULES.put("Hogar y Fijos 🏠", Arrays.asList("luz", "agua", "gas", "iberdrola", "endesa", "naturgy", "internet", "movistar", "vodafone", "orange", "yoigo", "alquiler", "comunidad", "leroy", "ikea"));
    CATEGORY_RULES.put("Transporte 🚗", Arrays.asList("gasolin", "repsol", "cepsa", "bp", "galp", "uber", "cabify", "taxi", "renfe", "ave", "alsa", "metro", "emt", "autobus", "parking"));
    CATEGORY_RULES.put("Salud 💊", Arrays.asList("farmacia", "medico", "dentista", "sanitas", "adeslas", "hospital", "optica"));
  }

  // --- VERIFICADOR DE SEGURIDAD ---
  @PostMapping("/verify-pass")
  public ResponseEntity<?> verifyPass(@RequestBody Map<String, String> data) {
    Optional<User> u = userRepo.findById(Long.parseLong(data.get("userId")));
    if (u.isPresent() && u.get().getPassword().equals(data.get("password"))) {
      return ResponseEntity.ok("OK");
    }
    return ResponseEntity.status(401).body("Contraseña incorrecta");
  }

  // --- LOGIN & REGISTER ---
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
    Optional<User> u = userRepo.findByDni(data.get("dni"));
    if (u.isPresent() && u.get().getPassword().equals(data.get("password"))) return ResponseEntity.ok(u.get());
    return ResponseEntity.status(401).body("Credenciales inválidas");
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
    if(userRepo.findByDni(data.get("dni")).isPresent()) return ResponseEntity.badRequest().body("DNI duplicado");
    User u = new User(data.get("dni"), data.get("firstName"), data.get("lastName"), data.get("password"));
    userRepo.save(u);
    String randomIban = "ES" + (long)(Math.random() * 1000000000000000000L) + "0000";
    accRepo.save(new Account(randomIban, "Cuenta Corriente", 0.0, u));
    return ResponseEntity.ok("Registrado");
  }

  // --- OPERACIONES ---
  @GetMapping("/accounts/{userId}")
  public List<Account> getAccounts(@PathVariable Long userId) { return accRepo.findByOwnerId(userId); }

  @GetMapping("/beneficiary/{iban}")
  public ResponseEntity<String> getBeneficiary(@PathVariable String iban) {
    if (!iban.matches("^[A-Z]{2}\\d{22}$")) return ResponseEntity.badRequest().body("Formato incorrecto");
    String name = bankService.getBeneficiaryName(iban);
    return name != null ? ResponseEntity.ok("Cliente: " + name) : ResponseEntity.ok("Banco Externo / Internacional");
  }

  @PostMapping("/transfer")
  public ResponseEntity<String> transfer(@RequestBody Map<String, Object> payload) {
    String dest = (String) payload.get("destIban");
    if (!dest.matches("^[A-Z]{2}\\d{22}$")) return ResponseEntity.badRequest().body("IBAN destino inválido");
    String res = bankService.executeTransfer((String) payload.get("sourceIban"), dest, Double.parseDouble(payload.get("amount").toString()), (String) payload.get("concept"));
    if ("OK".equals(res)) return ResponseEntity.ok("Transferencia realizada");
    return ResponseEntity.badRequest().body(res);
  }

  @PostMapping("/deposit")
  public ResponseEntity<String> deposit(@RequestBody Map<String, Object> payload) {
    String iban = (String) payload.get("iban");
    double amount = Double.parseDouble(payload.get("amount").toString());
    if (amount <= 0) return ResponseEntity.badRequest().body("El importe debe ser positivo");
    Account acc = accRepo.findById(iban).orElse(null);
    if (acc == null) return ResponseEntity.badRequest().body("Cuenta no encontrada");
    acc.setBalance(acc.getBalance() + amount);
    accRepo.save(acc);
    bankService.executeTransfer(iban, iban, amount, "INGRESO EN CAJERO");
    return ResponseEntity.ok("Saldo actualizado");
  }

  @GetMapping("/history/{iban}")
  public ResponseEntity<List<Transaction>> getHistory(@PathVariable String iban) {
    return ResponseEntity.ok(bankService.getHistory(iban));
  }

  // --- GRÁFICAS INTELIGENTES ---
  @GetMapping("/expenses-chart/{iban}")
  public Map<String, Double> getChartData(@PathVariable String iban) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> categories = new HashMap<>();
    for (Transaction tx : txs) {
      if (tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban)) {
        String category = categorizeSmart(tx.getConcept());
        categories.put(category, categories.getOrDefault(category, 0.0) + tx.getAmount());
      }
    }
    return categories;
  }

  @GetMapping("/monthly-chart/{iban}")
  public Map<String, Double> getMonthlyData(@PathVariable String iban) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> monthly = new TreeMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
    for (Transaction tx : txs) {
      if (tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban)) {
        String monthKey = "";
        if (tx.getDate() != null) monthKey = tx.getDate().format(formatter);
        else continue;
        monthly.put(monthKey, monthly.getOrDefault(monthKey, 0.0) + tx.getAmount());
      }
    }
    return monthly;
  }

  private String categorizeSmart(String concept) {
    if (concept == null || concept.trim().isEmpty()) return "Otros 📦";
    String normalized = cleanText(concept);
    for (Map.Entry<String, List<String>> entry : CATEGORY_RULES.entrySet()) {
      for (String keyword : entry.getValue()) {
        if (normalized.contains(keyword)) return entry.getKey();
      }
    }
    if (normalized.contains("bizum") || normalized.contains("transf")) return "Transferencias 💸";
    return "Otros 📦";
  }

  private String cleanText(String input) {
    String lower = input.toLowerCase().replace("&", "y");
    return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  // --- CRYPTO ---
  @GetMapping("/crypto/portfolio/{userId}")
  public ResponseEntity<?> getPortfolio(@PathVariable Long userId) {
    return ResponseEntity.ok(bankService.getCryptoPortfolio(userId));
  }
  @PostMapping("/crypto/trade")
  public ResponseEntity<String> tradeCrypto(@RequestBody Map<String, Object> payload) {
    String result = bankService.executeCryptoTrade(Long.parseLong(payload.get("userId").toString()), (String) payload.get("iban"), (String) payload.get("symbol"), (String) payload.get("type"), Double.parseDouble(payload.get("amountEur").toString()), Double.parseDouble(payload.get("currentPrice").toString()));
    if (result.startsWith("OK")) return ResponseEntity.ok(result);
    return ResponseEntity.badRequest().body(result);
  }

  // --- GENERADOR DE PDF (OPENPDF) ---
  @GetMapping("/export-pdf/{txId}")
  public void exportPdf(@PathVariable Long txId, HttpServletResponse response) throws IOException {
    Optional<Transaction> optTx = txRepo.findById(txId);

    if (optTx.isPresent()) {
      Transaction tx = optTx.get();

      response.setContentType("application/pdf");
      String headerKey = "Content-Disposition";
      String headerValue = "attachment; filename=Justificante_" + txId + ".pdf";
      response.setHeader(headerKey, headerValue);

      Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, response.getOutputStream());

      document.open();

      Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(59, 130, 246));
      Font fontText = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
      Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);

      Paragraph title = new Paragraph("SecureBank Ultimate", fontTitle);
      title.setAlignment(Paragraph.ALIGN_CENTER);
      document.add(title);

      document.add(new Paragraph(" "));
      document.add(new Paragraph("JUSTIFICANTE DE OPERACIÓN", fontLabel));
      document.add(new Paragraph("------------------------------------------------------------"));

      document.add(new Paragraph("Referencia: REF-" + tx.getId(), fontText));
      document.add(new Paragraph("Fecha: " + tx.getDate().toString(), fontText));
      document.add(new Paragraph(" ", fontText));
      document.add(new Paragraph("Origen: " + tx.getSourceIban(), fontText));
      document.add(new Paragraph("Destino: " + tx.getDestIban(), fontText));
      document.add(new Paragraph("Concepto: " + tx.getConcept(), fontText));
      document.add(new Paragraph(" ", fontText));

      Paragraph amountP = new Paragraph("IMPORTE: " + tx.getAmount() + " €", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK));
      amountP.setAlignment(Paragraph.ALIGN_RIGHT);
      document.add(amountP);

      document.add(new Paragraph(" "));
      document.add(new Paragraph("Este documento ha sido generado electrónicamente por SecureBank.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY)));

      document.close();
    }
  }
}