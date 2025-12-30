package com.securebank.main;

import java.util.List;

public interface IBankRepository {
  List<String> findAccountsByDNI(String dni);
  List<String> getBlackList();
  List<String> getCsvBlockedConcepts();
  String getBeneficiaryNameByIBAN(String targetIban);
  boolean processTransfer(String sourceIban, String destIban, double amount);
  String[] getAccountsArray();
}