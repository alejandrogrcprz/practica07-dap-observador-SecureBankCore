package com.securebank.main;

import java.util.List;

public interface IBankRepository {
  // Métodos de lectura
  String[] getAccountsArray(); // Para compatibilidad
  List<String> findAccountsByDNI(String dni);
  List<String> getBlackList();
  List<String> getCsvBlockedConcepts();
  String getBeneficiaryNameByIBAN(String targetIban);

  // Métodos de escritura (Transaccionales)
  boolean processTransfer(String sourceIban, String destIban, double amount);
}