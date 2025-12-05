package com.securebank.main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// AHORA IMPLEMENTAMOS LA INTERFAZ (OCP)
public class BankRepository implements IBankRepository {

  // Listas en memoria
  private List<String> rawData = new ArrayList<>(); // Datos de cuentas
  private List<String> blackListTerms = new ArrayList<>(); // Amenazas técnicas
  private List<String> csvBlockedConcepts = new ArrayList<>(); // Conceptos prohibidos (CSV)

  public BankRepository() {
    loadDataFromCore();         // Carga cuentas y saldos
    loadSecurityLists();        // Carga amenazas técnicas hardcoded
    loadBlockedConceptsCsv();   // Carga conceptos prohibidos del archivo externo
  }

  // --- 1. CARGA DE CUENTAS (bank_accounts.csv) ---
  private void loadDataFromCore() {
    File csvFile = new File("bank_accounts.csv");
    if (csvFile.exists()) {
      try (Scanner scanner = new Scanner(csvFile)) {
        if (scanner.hasNextLine()) scanner.nextLine(); // Saltar cabecera

        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          // CSV: DNI,CLIENTE,IBAN,TIPO_CUENTA,SALDO
          String[] parts = line.split(",");

          if (parts.length >= 5) {
            String dni = parts[0].trim();
            String cliente = parts[1].replace("\"", "").trim();
            String iban = parts[2].trim();
            String tipo = parts[3].trim();
            String saldoStr = parts[4].replace("$", "").trim();

            // Guardamos en memoria con separador interno '#'
            rawData.add(dni + "#" + cliente + "#" + iban + "#" + tipo + "#" + saldoStr);
          }
        }
        System.out.println(">> REPOSITORIO: " + rawData.size() + " cuentas cargadas.");
      } catch (Exception e) { e.printStackTrace(); }
    } else {
      System.err.println("ERROR: No se encuentra bank_accounts.csv");
      rawData.add("00000000X#Usuario Demo#ES00-DEMO#Cuenta Emergencia#1000.00");
    }
  }

  // --- 2. CARGA DE CONCEPTOS PROHIBIDOS (blocked_concepts.csv) ---
  private void loadBlockedConceptsCsv() {
    File file = new File("blocked_concepts.csv");
    if (file.exists()) {
      try (Scanner scanner = new Scanner(file)) {
        if (scanner.hasNextLine()) scanner.nextLine();
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine().trim();
          if (!line.isEmpty()) csvBlockedConcepts.add(line.toUpperCase());
        }
        System.out.println(">> SEGURIDAD: Cargados " + csvBlockedConcepts.size() + " conceptos prohibidos.");
      } catch (Exception e) { e.printStackTrace(); }
    } else {
      System.err.println("AVISO: No se encuentra blocked_concepts.csv");
    }
  }

  // --- 3. LISTAS DE SEGURIDAD TÉCNICA (Hardcoded) ---
  private void loadSecurityLists() {
    String[] threats = {"SQL", "INJECTION", "XSS", "SCRIPT", "DROP TABLE", "ALERT(", "<SCRIPT>"};
    for (String t : threats) blackListTerms.add(t);
  }

  // --- 4. PERSISTENCIA (Guardar cambios en disco) ---
  private void saveChangesToDisk() {
    try (PrintWriter pw = new PrintWriter(new File("bank_accounts.csv"))) {
      pw.println("DNI,CLIENTE,IBAN,TIPO_CUENTA,SALDO");
      for (String row : rawData) {
        String csvRow = row.replace("#", ",");
        pw.println(csvRow);
      }
    } catch (Exception e) {
      System.err.println("ERROR CRÍTICO: No se pudo guardar en el CSV.");
      e.printStackTrace();
    }
  }

  // ========================================================================
  // IMPLEMENTACIÓN DE LA INTERFAZ IBankRepository (MÉTODOS PÚBLICOS)
  // ========================================================================

  @Override
  public String[] getAccountsArray() {
    return new String[]{"Sistema protegido. Inicie sesión con DNI."};
  }

  @Override
  public List<String> findAccountsByDNI(String dniInput) {
    List<String> userAccounts = new ArrayList<>();
    for (String row : rawData) {
      String[] parts = row.split("#");
      String dniDb = parts[0];
      if (dniDb.equalsIgnoreCase(dniInput)) {
        String nombre = parts[1];
        String iban = parts[2];
        String tipo = parts[3];
        double saldo = Double.parseDouble(parts[4]);
        String saldoF = String.format("%,.2f", saldo);
        userAccounts.add(iban + " | " + tipo + " - " + nombre + " (Saldo: " + saldoF + " €)");
      }
    }
    return userAccounts;
  }

  @Override
  public String getBeneficiaryNameByIBAN(String targetIban) {
    for (String row : rawData) {
      String[] parts = row.split("#");
      String ibanDb = parts[2].trim();
      if (ibanDb.replace(" ", "").equalsIgnoreCase(targetIban.replace(" ", ""))) {
        return parts[1]; // Retorna nombre del cliente
      }
    }
    return null;
  }

  @Override
  public List<String> getBlackList() {
    return blackListTerms;
  }

  @Override
  public List<String> getCsvBlockedConcepts() {
    return csvBlockedConcepts;
  }

  @Override
  public boolean processTransfer(String sourceIban, String destIban, double amount) {
    boolean sourceFound = false;
    boolean dataChanged = false;

    for (int i = 0; i < rawData.size(); i++) {
      String row = rawData.get(i);
      String[] parts = row.split("#"); // 0:DNI, 1:Nombre, 2:IBAN, 3:Tipo, 4:Saldo

      String currentIban = parts[2];
      double currentSaldo = Double.parseDouble(parts[4]);
      boolean modified = false;

      // Restar al origen
      if (currentIban.equals(sourceIban)) {
        currentSaldo -= amount;
        modified = true;
        sourceFound = true;
      }
      // Sumar al destino
      else if (currentIban.equals(destIban)) {
        currentSaldo += amount;
        modified = true;
      }

      if (modified) {
        String newRow = parts[0] + "#" + parts[1] + "#" + parts[2] + "#" + parts[3] + "#" + currentSaldo;
        rawData.set(i, newRow);
        dataChanged = true;
      }
    }

    if (dataChanged) saveChangesToDisk(); // Persistencia

    return sourceFound;
  }
}