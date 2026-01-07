package com.securebank.services;

import com.securebank.models.*;
import com.securebank.repositories.*;
import com.securebank.interfaces.IBankObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BankService {

  @Autowired private AccountRepository accountRepo;
  @Autowired private TransactionRepository transactionRepo;
  @Autowired private UserRepository userRepo;
  @Autowired private CryptoRepository cryptoRepo;
  @Autowired(required = false) private List<IBankObserver> observers;

  // --- INFO ---
  public List<Transaction> getHistory(String iban) {
    return transactionRepo.findBySourceIbanOrDestIbanOrderByDateDesc(iban, iban);
  }

  // --- TRANSFERENCIAS CON BLOQUEO ---
  // ... dentro de BankService.java ...

  public void processTransaction(TransactionDto tx) {
    // 1. Validaciones previas (Origen, Congelación, Saldo...)
    Optional<Account> optSource = accountRepo.findById(tx.getSourceIban());
    if (optSource.isEmpty()) throw new RuntimeException("Cuenta origen no existe");
    Account source = optSource.get();

    if (source.isFrozen()) throw new RuntimeException("TARJETA CONGELADA. Desbloquéala para operar.");
    if (source.getBalance() < tx.getAmount()) throw new RuntimeException("Saldo insuficiente");

    // 2. Fraude Check
    Transaction transaction = new Transaction(tx.getSourceIban(), tx.getDestIban(), tx.getAmount(), tx.getConcept(), LocalDateTime.now());
    try { if (observers != null) for (IBankObserver obs : observers) obs.onTransactionAttempt(transaction); }
    catch (Exception e) { throw new RuntimeException(e.getMessage()); }

    // 3. EJECUTAR TRANSFERENCIA (Restar dinero)
    source.setBalance(source.getBalance() - tx.getAmount());

    // --- LOGICA CASHBACK (NUEVO) ---
    // Si el dueño es Premium, le devolvemos el 1%
    User owner = source.getOwner();
    if (owner.isPremium()) {
      double cashback = tx.getAmount() * 0.01; // 1%
      owner.setAccumulatedCashback(owner.getAccumulatedCashback() + cashback);
      userRepo.save(owner); // Guardamos el usuario con su nuevo saldo de regalo
    }
    // -------------------------------

    accountRepo.save(source);

    // 4. Sumar al destino (si es interno)
    Optional<Account> optDest = accountRepo.findById(tx.getDestIban());
    if (optDest.isPresent()) {
      Account dest = optDest.get();
      dest.setBalance(dest.getBalance() + tx.getAmount());
      accountRepo.save(dest);
    }
    transactionRepo.save(transaction);
  }

  // --- HUCHA Y REDONDEO (NUEVO) ---
  public void processSavings(String iban, double amount) {
    Optional<Account> optAcc = accountRepo.findById(iban);
    if (optAcc.isPresent()) {
      Account acc = optAcc.get();
      if(acc.isFrozen()) throw new RuntimeException("Cuenta congelada");
      if(acc.getBalance() < amount) throw new RuntimeException("Saldo insuficiente para ahorro");

      // Restamos de la cuenta principal
      acc.setBalance(acc.getBalance() - amount);
      accountRepo.save(acc);

      // Guardamos movimiento especial "HUCHA"
      Transaction t = new Transaction(iban, "HUCHA-AHORRO", amount, "Aportación Hucha/Redondeo", LocalDateTime.now());
      transactionRepo.save(t);
    }
  }

  // --- CRIPTO ---
  public List<CryptoHolding> getCryptoPortfolio(String userId) {
    return cryptoRepo.findByOwnerDni(userId);
  }

  public String executeCryptoTrade(String userId, String iban, String symbol, String type, double amountEur, double currentPrice) {
    Optional<Account> accOpt = accountRepo.findById(iban);
    if(accOpt.isEmpty()) return "Cuenta inválida";
    Account acc = accOpt.get();
    if(acc.isFrozen()) return "Cuenta congelada";

    Optional<User> uOpt = userRepo.findById(userId);
    if(uOpt.isEmpty()) return "Usuario inválido";

    // Buscar o crear holding
    CryptoHolding holding = cryptoRepo.findByOwnerDniAndSymbol(userId, symbol)
      .orElse(new CryptoHolding(symbol, 0.0, uOpt.get()));

    if("BUY".equals(type)) {
      if(acc.getBalance() < amountEur) return "Sin saldo";
      acc.setBalance(acc.getBalance() - amountEur);

      // Comisión simple
      double fee = uOpt.get().isPremium() ? 0 : 0.015;
      double net = amountEur * (1 - fee);

      holding.setAmount(holding.getAmount() + (net / currentPrice));
      transactionRepo.save(new Transaction(iban, "CRYPTO", amountEur, "Compra " + symbol, LocalDateTime.now()));
    } else {
      double cryptoNeeded = amountEur / currentPrice;
      if(holding.getAmount() < cryptoNeeded) return "Faltan criptos";

      holding.setAmount(holding.getAmount() - cryptoNeeded);
      acc.setBalance(acc.getBalance() + amountEur);
      transactionRepo.save(new Transaction("CRYPTO", iban, amountEur, "Venta " + symbol, LocalDateTime.now()));
    }

    accountRepo.save(acc);
    cryptoRepo.save(holding); // GUARDAMOS EN BD
    return "OK";
  }
}