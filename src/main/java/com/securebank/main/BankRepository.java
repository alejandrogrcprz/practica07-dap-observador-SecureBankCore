package com.securebank.main;

import java.io.*;
import java.util.*;

public class BankRepository implements IBankRepository {
  private List<String> rawData = new ArrayList<>();
  private List<String> csvBlockedConcepts = new ArrayList<>();

  public BankRepository() {
    loadDataFromCore();
    loadBlockedConceptsCsv();
  }

  private void loadDataFromCore() {
    File csvFile = new File("src/main/resources/bank_accounts.csv");
    if (!csvFile.exists()) csvFile = new File("bank_accounts.csv"); // Fallback a raíz

    try (Scanner scanner = new Scanner(csvFile)) {
      if (scanner.hasNextLine()) scanner.nextLine();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        if (parts.length >= 5) {
          rawData.add(parts[0].trim() + "#" + parts[1].trim() + "#" + parts[2].trim() + "#" + parts[3].trim() + "#" + parts[4].trim());
        }
      }
    } catch (Exception e) { System.err.println("Error cargando cuentas: " + e.getMessage()); }
  }

  private void loadBlockedConceptsCsv() {
    File file = new File("src/main/resources/blocked_concepts.csv");
    if (!file.exists()) file = new File("blocked_concepts.csv");

    try (Scanner scanner = new Scanner(file)) {
      if (scanner.hasNextLine()) scanner.nextLine();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (!line.isEmpty()) csvBlockedConcepts.add(line.toUpperCase());
      }
    } catch (Exception e) { System.err.println("Error cargando conceptos: " + e.getMessage()); }
  }

  @Override
  public List<String> findAccountsByDNI(String dniInput) {
    List<String> userAccounts = new ArrayList<>();
    for (String row : rawData) {
      String[] parts = row.split("#");
      if (parts[0].equalsIgnoreCase(dniInput)) {
        userAccounts.add(parts[2] + " | " + parts[3] + " - " + parts[1] + " (Saldo: " + parts[4] + " €)");
      }
    }
    return userAccounts;
  }

  @Override public List<String> getBlackList() { return Arrays.asList("SQL", "XSS", "SCRIPT"); }
  @Override public List<String> getCsvBlockedConcepts() { return csvBlockedConcepts; }
  @Override public String getBeneficiaryNameByIBAN(String iban) {
    for (String row : rawData) {
      String[] parts = row.split("#");
      if (parts[2].replace(" ", "").equalsIgnoreCase(iban.replace(" ", ""))) return parts[1];
    }
    return null;
  }

  @Override
  public boolean processTransfer(String src, String dst, double amt) {
    // Lógica de persistencia simplificada para el ejemplo
    System.out.println("REPOSITORIO: Actualizando saldos en disco...");
    return true;
  }

  @Override public String[] getAccountsArray() { return new String[0]; }
}