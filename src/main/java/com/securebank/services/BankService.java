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
  @Autowired private SavingsGoalRepository goalRepo; // <--- NUEVO

  @Autowired(required = false)
  private List<IBankObserver> observers;

  // --- MÉTODOS BANCARIOS ---
  public String getBeneficiaryName(String iban) {
    return accountRepo.findById(iban)
      .map(acc -> acc.getOwner().getFirstName() + " " + acc.getOwner().getLastName())
      .orElse(null);
  }

  public List<Transaction> getHistory(String iban) {
    return transactionRepo.findBySourceIbanOrDestIbanOrderByDateDesc(iban, iban);
  }

  public String executeTransfer(String sourceIban, String destIban, double amount, String concept) {
    if (amount <= 0) return "Importe debe ser positivo";
    Optional<Account> optSource = accountRepo.findById(sourceIban);
    Optional<Account> optDest = accountRepo.findById(destIban);

    if (optSource.isEmpty()) return "Cuenta origen no existe";
    Account source = optSource.get();

    if (source.getBalance() < amount) return "Fondos insuficientes";

    Transaction tx = new Transaction(sourceIban, destIban, amount, concept, LocalDateTime.now());

    if (observers != null) {
      for (IBankObserver obs : observers) {
        try { obs.onTransactionAttempt(tx); }
        catch (SecurityException e) { return e.getMessage(); }
      }
    }

    source.setBalance(source.getBalance() - amount);
    accountRepo.save(source);

    if (optDest.isPresent()) {
      Account dest = optDest.get();
      dest.setBalance(dest.getBalance() + amount);
      accountRepo.save(dest);
    }

    transactionRepo.save(tx);
    return "OK";
  }

  // --- CRIPTOMONEDAS ---
  public List<CryptoHolding> getCryptoPortfolio(Long userId) {
    return cryptoRepo.findByOwnerId(userId);
  }

  public String executeCryptoTrade(long userId, String iban, String symbol, String type, double amountEur, double currentPrice) {
    Optional<Account> optAcc = accountRepo.findById(iban);
    if (optAcc.isEmpty()) return "Cuenta no encontrada";
    Account acc = optAcc.get();
    Optional<User> optUser = userRepo.findById(userId);
    if (optUser.isEmpty()) return "Usuario no encontrado";
    User user = optUser.get();

    Optional<CryptoHolding> optHolding = cryptoRepo.findByOwnerIdAndSymbol(userId, symbol);
    CryptoHolding holding = optHolding.orElse(new CryptoHolding(symbol, 0.0, user));

    if ("BUY".equals(type)) {
      if (acc.getBalance() < amountEur) return "Fondos insuficientes en euros";
      acc.setBalance(acc.getBalance() - amountEur);
      holding.setAmount(holding.getAmount() + (amountEur / currentPrice));
      transactionRepo.save(new Transaction(iban, "CRYPTO-BROKER", amountEur, "Compra " + symbol, LocalDateTime.now()));
    } else {
      double cryptoToSell = amountEur / currentPrice;
      if (holding.getAmount() < cryptoToSell) return "No tienes suficientes " + symbol;
      holding.setAmount(holding.getAmount() - cryptoToSell);
      acc.setBalance(acc.getBalance() + amountEur);
      transactionRepo.save(new Transaction("CRYPTO-BROKER", iban, amountEur, "Venta " + symbol, LocalDateTime.now()));
    }
    accountRepo.save(acc);
    cryptoRepo.save(holding);
    return "OK";
  }

  // --- NUEVO: GESTIÓN DE METAS DE AHORRO ---
  public List<SavingsGoal> getGoals(Long userId) {
    return goalRepo.findByOwnerId(userId);
  }

  public String createGoal(Long userId, String name, double target) {
    Optional<User> u = userRepo.findById(userId);
    if(u.isPresent()) {
      goalRepo.save(new SavingsGoal(name, target, u.get()));
      return "OK";
    }
    return "Usuario no encontrado";
  }

  public String processGoalOperation(Long goalId, String iban, double amount, String type) {
    Optional<SavingsGoal> optGoal = goalRepo.findById(goalId);
    Optional<Account> optAcc = accountRepo.findById(iban);

    if(optGoal.isEmpty() || optAcc.isEmpty()) return "Datos inválidos";
    SavingsGoal goal = optGoal.get();
    Account acc = optAcc.get();

    if("DEPOSIT".equals(type)) {
      // Mover dinero: Cuenta -> Hucha
      if(acc.getBalance() < amount) return "Saldo insuficiente en cuenta";
      acc.setBalance(acc.getBalance() - amount);
      goal.setCurrentAmount(goal.getCurrentAmount() + amount);

      // Creamos transacción interna para que salga en el historial
      transactionRepo.save(new Transaction(iban, "HUCHA-" + goal.getId(), amount, "Ahorro: " + goal.getName(), LocalDateTime.now()));

    } else if ("WITHDRAW".equals(type)) {
      // Mover dinero: Hucha -> Cuenta
      if(goal.getCurrentAmount() < amount) return "Saldo insuficiente en la meta";
      goal.setCurrentAmount(goal.getCurrentAmount() - amount);
      acc.setBalance(acc.getBalance() + amount);

      transactionRepo.save(new Transaction("HUCHA-" + goal.getId(), iban, amount, "Retiro: " + goal.getName(), LocalDateTime.now()));
    }

    accountRepo.save(acc);
    goalRepo.save(goal);
    return "OK";
  }
}