package com.securebank.controllers;

import com.securebank.models.*;
import com.securebank.repositories.*;
import com.securebank.services.BankService;

// Imports estándar
import java.io.IOException;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Imports PDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

// Imports Spring
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

  // --- CATEGORIZACIÓN ---
  private static final Map<String, List<String>> CATEGORY_RULES = new HashMap<>();
  static {
    CATEGORY_RULES.put("Ropa 👕", Arrays.asList("zara", "h&m", "hym", "primark", "pull", "bear", "stradivarius", "bershka", "shein", "zalando", "nike", "adidas"));
    CATEGORY_RULES.put("Alimentación 🛒", Arrays.asList("mercadona", "lidl", "carrefour", "dia", "aldi", "eroski", "consum", "alcampo", "bonarea", "super", "fruteria", "panaderia"));
    CATEGORY_RULES.put("Ocio 🍔", Arrays.asList("restaurante", "bar", "cafe", "starbucks", "mcdonalds", "burger", "king", "kfc", "uber eats", "glovo", "just eat", "cine", "netflix", "spotify", "hbo", "steam", "playstation", "entrada"));
    CATEGORY_RULES.put("Hogar 🏠", Arrays.asList("luz", "agua", "gas", "iberdrola", "endesa", "naturgy", "internet", "movistar", "vodafone", "orange", "yoigo", "alquiler", "comunidad", "leroy", "ikea"));
    CATEGORY_RULES.put("Transporte 🚗", Arrays.asList("gasolin", "repsol", "cepsa", "bp", "galp", "uber", "cabify", "taxi", "renfe", "ave", "alsa", "metro", "emt", "autobus", "parking"));
    CATEGORY_RULES.put("Salud 💊", Arrays.asList("farmacia", "medico", "dentista", "sanitas", "adeslas", "hospital", "optica"));
  }

  // --- AUTH ---
  @PostMapping("/verify-pass")
  public ResponseEntity<?> verifyPass(@RequestBody Map<String, String> data) {
    Optional<User> u = userRepo.findById(Long.parseLong(data.get("userId")));
    return (u.isPresent() && u.get().getPassword().equals(data.get("password"))) ? ResponseEntity.ok("OK") : ResponseEntity.status(401).body("Error");
  }

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
    accRepo.save(new Account("ES" + (long)(Math.random() * 1e18) + "00", "Cuenta Corriente", 0.0, u));
    return ResponseEntity.ok("Registrado");
  }

  // --- OPERACIONES ---
  @GetMapping("/accounts/{userId}")
  public List<Account> getAccounts(@PathVariable Long userId) { return accRepo.findByOwnerId(userId); }

  @GetMapping("/beneficiary/{iban}")
  public ResponseEntity<String> getBeneficiary(@PathVariable String iban) {
    String name = bankService.getBeneficiaryName(iban);
    return name != null ? ResponseEntity.ok("Cliente: " + name) : ResponseEntity.ok("Externo");
  }

  @PostMapping("/transfer")
  public ResponseEntity<String> transfer(@RequestBody Map<String, Object> payload) {
    String res = bankService.executeTransfer((String) payload.get("sourceIban"), (String) payload.get("destIban"), Double.parseDouble(payload.get("amount").toString()), (String) payload.get("concept"));
    return "OK".equals(res) ? ResponseEntity.ok("OK") : ResponseEntity.badRequest().body(res);
  }

  @PostMapping("/deposit")
  public ResponseEntity<String> deposit(@RequestBody Map<String, Object> payload) {
    Account acc = accRepo.findById((String) payload.get("iban")).orElse(null);
    if (acc == null) return ResponseEntity.badRequest().body("Cuenta no encontrada");
    double amount = Double.parseDouble(payload.get("amount").toString());
    acc.setBalance(acc.getBalance() + amount);
    accRepo.save(acc);
    bankService.executeTransfer(acc.getIban(), acc.getIban(), amount, "INGRESO CAJERO");
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/history/{iban}")
  public ResponseEntity<List<Transaction>> getHistory(@PathVariable String iban) { return ResponseEntity.ok(bankService.getHistory(iban)); }

  // --- NUEVO: ENDPOINTS DE METAS DE AHORRO ---
  @GetMapping("/goals/{userId}")
  public List<SavingsGoal> getGoals(@PathVariable Long userId) {
    return bankService.getGoals(userId);
  }

  @PostMapping("/goals/create")
  public ResponseEntity<String> createGoal(@RequestBody Map<String, Object> data) {
    return ResponseEntity.ok(bankService.createGoal(Long.parseLong(data.get("userId").toString()), (String) data.get("name"), Double.parseDouble(data.get("target").toString())));
  }

  @PostMapping("/goals/operate")
  public ResponseEntity<String> operateGoal(@RequestBody Map<String, Object> data) {
    String res = bankService.processGoalOperation(Long.parseLong(data.get("goalId").toString()), (String) data.get("iban"), Double.parseDouble(data.get("amount").toString()), (String) data.get("type"));
    return "OK".equals(res) ? ResponseEntity.ok("OK") : ResponseEntity.badRequest().body(res);
  }

  // --- GRÁFICAS & PDF ---
  @GetMapping("/expenses-chart/{iban}")
  public Map<String, Double> getChartData(@PathVariable String iban) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> cats = new HashMap<>();
    for (Transaction tx : txs) if (tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban)) {
      String c = categorizeSmart(tx.getConcept());
      cats.put(c, cats.getOrDefault(c, 0.0) + tx.getAmount());
    }
    return cats;
  }

  @GetMapping("/monthly-chart/{iban}")
  public Map<String, Double> getMonthlyData(@PathVariable String iban) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> m = new TreeMap<>();
    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM");
    for (Transaction tx : txs) if (tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban) && tx.getDate() != null) {
      m.put(tx.getDate().format(f), m.getOrDefault(tx.getDate().format(f), 0.0) + tx.getAmount());
    }
    return m;
  }

  @GetMapping("/export-pdf/{txId}")
  public void exportPdf(@PathVariable Long txId, HttpServletResponse r) throws IOException {
    Optional<Transaction> o = txRepo.findById(txId);
    if (o.isPresent()) {
      Transaction t = o.get();
      r.setContentType("application/pdf");
      r.setHeader("Content-Disposition", "attachment; filename=Justificante_" + txId + ".pdf");
      Document d = new Document(PageSize.A4);
      PdfWriter.getInstance(d, r.getOutputStream());
      d.open();
      Font titleF = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(59, 130, 246));
      d.add(new Paragraph("SecureBank Ultimate", titleF));
      d.add(new Paragraph(" "));
      d.add(new Paragraph("JUSTIFICANTE DE OPERACIÓN"));
      d.add(new Paragraph("------------------------------------------------"));
      d.add(new Paragraph("Ref: " + t.getId()));
      d.add(new Paragraph("Fecha: " + t.getDate()));
      d.add(new Paragraph("Importe: " + t.getAmount() + " EUR"));
      d.add(new Paragraph("Concepto: " + t.getConcept()));
      d.close();
    }
  }

  // --- CRYPTO ---
  @GetMapping("/crypto/portfolio/{userId}")
  public ResponseEntity<?> getPortfolio(@PathVariable Long userId) { return ResponseEntity.ok(bankService.getCryptoPortfolio(userId)); }

  @PostMapping("/crypto/trade")
  public ResponseEntity<String> tradeCrypto(@RequestBody Map<String, Object> p) {
    return ResponseEntity.ok(bankService.executeCryptoTrade(Long.parseLong(p.get("userId").toString()), (String) p.get("iban"), (String) p.get("symbol"), (String) p.get("type"), Double.parseDouble(p.get("amountEur").toString()), Double.parseDouble(p.get("currentPrice").toString())));
  }

  private String categorizeSmart(String c) {
    if (c == null) return "Otros 📦";
    String n = Normalizer.normalize(c.toLowerCase().replace("&", "y"), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    for (Map.Entry<String, List<String>> e : CATEGORY_RULES.entrySet()) for (String k : e.getValue()) if (n.contains(k)) return e.getKey();
    return "Otros 📦";
  }
}