package com.securebank.controllers;

import com.securebank.models.*;
import com.securebank.repositories.*;
import com.securebank.services.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BankWebController {

  @Autowired private UserRepository userRepo;
  @Autowired private AccountRepository accountRepo;
  @Autowired private TransactionRepository transactionRepo;
  @Autowired private BankService bankService;

  // --- LOGIN & REGISTRO ---
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
    Optional<User> u = userRepo.findById(data.get("dni"));
    if (u.isPresent() && u.get().getPassword().equals(data.get("password"))) return ResponseEntity.ok(u.get());
    return ResponseEntity.status(401).body("Credenciales incorrectas");
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
    if (userRepo.existsById(data.get("dni"))) return ResponseEntity.badRequest().body("Usuario existe");
    User u = new User(data.get("dni"), data.get("firstName"), data.get("lastName"), data.get("password"));
    userRepo.save(u);
    accountRepo.save(new Account(data.get("iban"), "Cuenta Principal", 0.0, u));
    return ResponseEntity.ok(Map.of("status", "OK"));
  }

  // --- DATOS USUARIO (Recarga todo: hucha, premium, redondeo) ---
  @GetMapping("/users/{dni}")
  public ResponseEntity<?> getUser(@PathVariable String dni) {
    return userRepo.findById(dni).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  // --- GESTIÓN PREMIUM (COBRAR 9.99€) ---
  @PostMapping("/users/{dni}/premium")
  public ResponseEntity<?> setPremium(@PathVariable String dni, @RequestBody Map<String, Boolean> data) {
    Optional<User> uOpt = userRepo.findById(dni);
    if (uOpt.isEmpty()) return ResponseEntity.notFound().build();
    User user = uOpt.get();
    boolean wantPremium = data.get("isPremium");

    // Si quiere ser Premium y no lo es, COBRAR
    if (wantPremium && !user.isPremium()) {
      // Buscamos su cuenta principal para cobrarle
      List<Account> accounts = accountRepo.findByOwnerDni(dni);
      if (accounts.isEmpty()) return ResponseEntity.badRequest().body("No tienes cuenta para pagar");
      Account acc = accounts.get(0); // Cobramos de la primera

      if (acc.getBalance() < 9.99) return ResponseEntity.badRequest().body("Saldo insuficiente para Premium (9.99€)");

      acc.setBalance(acc.getBalance() - 9.99);
      accountRepo.save(acc);
      transactionRepo.save(new Transaction("SUSCRIPCION", acc.getIban(), 9.99, "Cuota Mensual SecureBank Premium", LocalDateTime.now()));
    }

    user.setPremium(wantPremium);
    userRepo.save(user);
    return ResponseEntity.ok("OK");
  }

  // --- ACTIVAR/DESACTIVAR REDONDEO ---
  @PostMapping("/users/{dni}/roundup")
  public ResponseEntity<?> toggleRoundUp(@PathVariable String dni, @RequestBody Map<String, Boolean> data) {
    userRepo.findById(dni).ifPresent(u -> {
      u.setRoundUpActive(data.get("active"));
      userRepo.save(u);
    });
    return ResponseEntity.ok("OK");
  }

  // --- HUCHA: AÑADIR (PERSISTENTE) ---
  @PostMapping("/savings/add")
  public ResponseEntity<?> addToSavings(@RequestBody Map<String, Object> data) {
    try {
      String iban = (String)data.get("iban");
      Double amount = Double.valueOf(data.get("amount").toString());

      // Validar cuenta
      Account acc = accountRepo.findById(iban).orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
      if(acc.isFrozen()) return ResponseEntity.badRequest().body("Cuenta congelada");
      if(acc.getBalance() < amount) return ResponseEntity.badRequest().body("Saldo insuficiente");

      // Operación
      acc.setBalance(acc.getBalance() - amount);
      accountRepo.save(acc);

      // Guardar en Usuario
      User u = acc.getOwner();
      u.setSavingsBalance(u.getSavingsBalance() + amount);
      userRepo.save(u);

      transactionRepo.save(new Transaction(iban, "HUCHA", amount, "Ahorro Hucha", LocalDateTime.now()));
      return ResponseEntity.ok("Guardado");
    } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
  }

  // --- HUCHA: ROMPER (DEVOLVER) ---
  @PostMapping("/savings/break")
  public ResponseEntity<?> breakSavings(@RequestBody Map<String, Object> data) {
    try {
      String iban = (String)data.get("iban");
      Account acc = accountRepo.findById(iban).orElseThrow();
      User u = acc.getOwner();

      double total = u.getSavingsBalance();
      if(total <= 0) return ResponseEntity.badRequest().body("La hucha está vacía");

      // Devolver dinero
      acc.setBalance(acc.getBalance() + total);
      u.setSavingsBalance(0.0);

      accountRepo.save(acc);
      userRepo.save(u);

      transactionRepo.save(new Transaction("HUCHA-ROTA", iban, total, "Recuperado de Hucha", LocalDateTime.now()));
      return ResponseEntity.ok("Hucha rota");
    } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
  }

  // --- CANJEAR CASHBACK ---
  @PostMapping("/users/{dni}/redeem-cashback")
  public ResponseEntity<?> redeemCashback(@PathVariable String dni, @RequestBody Map<String, String> data) {
    User user = userRepo.findById(dni).orElseThrow();
    double amount = user.getAccumulatedCashback();
    if (amount <= 0) return ResponseEntity.badRequest().body("Sin cashback");

    Account acc = accountRepo.findById(data.get("iban")).orElseThrow();
    acc.setBalance(acc.getBalance() + amount);
    accountRepo.save(acc);

    user.setAccumulatedCashback(0.0);
    userRepo.save(user);

    transactionRepo.save(new Transaction("REWARDS", acc.getIban(), amount, "Canje Cashback", LocalDateTime.now()));
    return ResponseEntity.ok("OK");
  }

  // --- RESTO DE ENDPOINTS (Info, Transfer, Crypto, Graficas) ---
  @GetMapping("/accounts/{dni}")
  public List<Account> getAccounts(@PathVariable String dni) { return accountRepo.findByOwnerDni(dni); }

  @GetMapping("/history/{iban}")
  public List<Transaction> getHistory(@PathVariable String iban) { return bankService.getHistory(iban); }

  @PostMapping("/transfer")
  public ResponseEntity<?> transfer(@RequestBody TransactionDto dto) {
    try { bankService.processTransaction(dto); return ResponseEntity.ok("Transferencia OK"); }
    catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> deposit(@RequestBody Map<String, Object> data) {
    Account acc = accountRepo.findById((String)data.get("iban")).orElseThrow();
    acc.setBalance(acc.getBalance() + Double.valueOf(data.get("amount").toString()));
    accountRepo.save(acc);
    transactionRepo.save(new Transaction("INGRESO", acc.getIban(), Double.valueOf(data.get("amount").toString()), "Ingreso Efectivo", LocalDateTime.now()));
    return ResponseEntity.ok(Map.of("status", "OK"));
  }

  @PostMapping("/accounts/{iban}/toggle-freeze")
  public ResponseEntity<?> toggleFreeze(@PathVariable String iban) {
    Account acc = accountRepo.findById(iban).orElseThrow();
    acc.setFrozen(!acc.isFrozen());
    accountRepo.save(acc);
    return ResponseEntity.ok(Map.of("frozen", acc.isFrozen()));
  }

  @GetMapping("/crypto/portfolio/{dni}")
  public List<CryptoHolding> getPortfolio(@PathVariable String dni) { return bankService.getCryptoPortfolio(dni); }

  @PostMapping("/crypto/trade")
  public ResponseEntity<?> cryptoTrade(@RequestBody Map<String, Object> data) {
    String res = bankService.executeCryptoTrade((String)data.get("userId"), (String)data.get("iban"), (String)data.get("symbol"), (String)data.get("type"), Double.valueOf(data.get("amountEur").toString()), Double.valueOf(data.get("currentPrice").toString()));
    return "OK".equals(res) ? ResponseEntity.ok("OK") : ResponseEntity.badRequest().body(res);
  }

  @GetMapping("/expenses-chart/{iban}")
  public Map<String, Double> getExpensesChart(@PathVariable String iban) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> chartData = new HashMap<>();
    for (Transaction tx : txs) {
      if (tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban)) {
        String c = tx.getConcept().toLowerCase();
        String cat = "Otros";
        if (c.contains("netflix") || c.contains("spotify") || c.contains("cine")) cat = "Ocio";
        else if (c.contains("mercadona") || c.contains("carrefour") || c.contains("restaurante")) cat = "Alimentación";
        else if (c.contains("gasolinera") || c.contains("uber") || c.contains("metro")) cat = "Transporte";
        else if (c.contains("crypto") || c.contains("binance")) cat = "Inversión";
        else if (c.contains("hucha") || c.contains("ahorro")) continue;
        chartData.put(cat, chartData.getOrDefault(cat, 0.0) + tx.getAmount());
      }
    }
    if(chartData.isEmpty()) chartData.put("Sin Gastos", 1.0);
    return chartData;
  }

  @GetMapping("/monthly-chart/{iban}")
  public Map<String, Double> getMonthlyChart(@PathVariable String iban, @RequestParam(defaultValue = "2026") String year) {
    List<Transaction> txs = bankService.getHistory(iban);
    Map<String, Double> monthlyData = new TreeMap<>();
    for (Transaction tx : txs) {
      String dateStr = tx.getDate().toString();
      if (dateStr.startsWith(year) && tx.getSourceIban().equals(iban) && !tx.getDestIban().equals(iban) && !tx.getConcept().contains("HUCHA")) {
        String mk = dateStr.substring(0, 7);
        monthlyData.put(mk, monthlyData.getOrDefault(mk, 0.0) + tx.getAmount());
      }
    }
    return monthlyData;
  }
}