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

  // --- MÉTODOS CRYPTO (ADAPTADOS A TU CLASE CryptoHolding) ---

  public List<CryptoHolding> getCryptoPortfolio(Long userId) {
    return cryptoRepo.findByOwnerId(userId);
  }

  public String executeCryptoTrade(long userId, String iban, String symbol, String type, double amountEur, double currentPrice) {
    // 1. Buscamos la cuenta bancaria
    Optional<Account> optAcc = accountRepo.findById(iban);
    if (optAcc.isEmpty()) return "Cuenta bancaria no encontrada";
    Account acc = optAcc.get();

    // 2. Buscamos al USUARIO (Necesario para tu clase CryptoHolding)
    Optional<User> optUser = userRepo.findById(userId);
    if (optUser.isEmpty()) return "Usuario no encontrado";
    User user = optUser.get();

    // 3. Buscamos si ya tiene esa moneda, si no, creamos una vacía vinculada al User
    Optional<CryptoHolding> optHolding = cryptoRepo.findByOwnerIdAndSymbol(userId, symbol);
    CryptoHolding holding = optHolding.orElse(new CryptoHolding(symbol, 0.0, user));

    if ("BUY".equals(type)) {
      if (acc.getBalance() < amountEur) return "Fondos insuficientes en euros";

      double cryptoAmount = amountEur / currentPrice;

      acc.setBalance(acc.getBalance() - amountEur);
      holding.setAmount(holding.getAmount() + cryptoAmount);

      transactionRepo.save(new Transaction(iban, "CRYPTO-BROKER", amountEur, "Compra " + symbol, LocalDateTime.now()));

    } else if ("SELL".equals(type)) {
      double cryptoToSell = amountEur / currentPrice;

      if (holding.getAmount() < cryptoToSell) return "No tienes suficientes " + symbol;

      holding.setAmount(holding.getAmount() - cryptoToSell);
      acc.setBalance(acc.getBalance() + amountEur);

      transactionRepo.save(new Transaction("CRYPTO-BROKER", iban, amountEur, "Venta " + symbol, LocalDateTime.now()));
    } else {
      return "Operación no válida";
    }

    accountRepo.save(acc);
    cryptoRepo.save(holding); // Guardamos tu CryptoHolding

    return "OK Transaction";
  }
}