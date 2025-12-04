package com.securebank.main;

import java.io.File;
import java.io.PrintWriter; // Importante para escribir
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BankRepository {

  // Guardamos la info cruda: "DNI#CLIENTE#IBAN#TIPO#SALDO"
  private List<String> rawData = new ArrayList<>();
  private List<String> blackListTerms = new ArrayList<>();
  private List<String> csvBlockedConcepts = new ArrayList<>();

  public BankRepository() {
    loadDataFromCore();
    loadBlockedConceptsCsv();
  }

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
      // Datos de emergencia por si no hay fichero
      rawData.add("00000000X#Usuario Demo#ES00-DEMO#Cuenta Emergencia#1000.00");
    }
    loadSecurityLists();
  }

  // --- NUEVO: MÉTODO PARA GUARDAR CAMBIOS EN EL DISCO DURO ---
  private void saveChangesToDisk() {
    try (PrintWriter pw = new PrintWriter(new File("bank_accounts.csv"))) {
      // 1. Escribimos la cabecera
      pw.println("DNI,CLIENTE,IBAN,TIPO_CUENTA,SALDO");

      // 2. Volcamos la memoria al archivo
      for (String row : rawData) {
        // Convertimos nuestro formato interno (#) al formato CSV (,)
        // IMPORTANTE: Reemplazamos el punto decimal por punto (Java estándar)
        String csvRow = row.replace("#", ",");
        pw.println(csvRow);
      }
      // System.out.println(">> REPOSITORIO: Cambios guardados en CSV.");
    } catch (Exception e) {
      System.err.println("ERROR CRÍTICO: No se pudo guardar en el CSV.");
      e.printStackTrace();
    }
  }

  private void loadSecurityLists() {
    String[] threats = {"TERRORISMO", "ARMAS", "LAVADO", "BITCOIN", "DARKNET", "DROGAS", "OFAC", "FRAUDE"};
    for (String t : threats) blackListTerms.add(t);
  }

  // --- MÉTODOS PÚBLICOS ---

  public String[] getAccountsArray() {
    return new String[]{"Sistema protegido. Inicie sesión con DNI."};
  }

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

        String display = iban + " | " + tipo + " - " + nombre + " (Saldo: " + saldoF + " €)";
        userAccounts.add(display);
      }
    }
    return userAccounts;
  }

  // --- MÉTODO TRANSACCIONAL CON PERSISTENCIA ---
  public boolean processTransfer(String sourceIban, String destIban, double amount) {
    boolean sourceFound = false;
    boolean dataChanged = false;

    // Recorremos la memoria para hacer los cálculos
    for (int i = 0; i < rawData.size(); i++) {
      String row = rawData.get(i);
      String[] parts = row.split("#"); // 0:DNI, 1:Nombre, 2:IBAN, 3:Tipo, 4:Saldo

      String currentIban = parts[2];
      double currentSaldo = Double.parseDouble(parts[4]);
      boolean modified = false;

      // 1. Restar al Origen
      if (currentIban.equals(sourceIban)) {
        currentSaldo -= amount;
        modified = true;
        sourceFound = true;
      }
      // 2. Sumar al Destino
      else if (currentIban.equals(destIban)) {
        currentSaldo += amount;
        modified = true;
      }

      if (modified) {
        String newRow = parts[0] + "#" + parts[1] + "#" + parts[2] + "#" + parts[3] + "#" + currentSaldo;
        rawData.set(i, newRow); // Actualizamos memoria RAM
        dataChanged = true;
      }
    }

    // SI HUBO CAMBIOS, GUARDAMOS EN EL DISCO DURO
    if (dataChanged) {
      saveChangesToDisk();
    }

    return sourceFound;
  }

  // --- NUEVO: BUSCAR NOMBRE POR IBAN (Auto-completado) ---
  public String getBeneficiaryNameByIBAN(String targetIban) {
    for (String row : rawData) {
      String[] parts = row.split("#"); // 0:DNI, 1:Nombre, 2:IBAN...
      String ibanDb = parts[2].trim();

      // Si el IBAN coincide (quitando espacios por si acaso)
      if (ibanDb.replace(" ", "").equalsIgnoreCase(targetIban.replace(" ", ""))) {
        return parts[1]; // Devolvemos el NOMBRE (Cliente)
      }
    }
    return null; // No encontrado (puede ser de otro banco)
  }

  // --- CARGAR CONCEPTOS PROHIBIDOS DESDE CSV ---
  private void loadBlockedConceptsCsv() {
    File file = new File("blocked_concepts.csv");
    if (file.exists()) {
      try (Scanner scanner = new Scanner(file)) {
        if (scanner.hasNextLine()) scanner.nextLine(); // Saltar cabecera

        while (scanner.hasNextLine()) {
          String line = scanner.nextLine().trim();
          if (!line.isEmpty()) {
            csvBlockedConcepts.add(line.toUpperCase());
          }
        }
        System.out.println(">> SEGURIDAD: Cargados " + csvBlockedConcepts.size() + " conceptos prohibidos del CSV.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("AVISO: No se encuentra blocked_concepts.csv");
    }
  }

  // Método público para que el móvil pida la lista
  public List<String> getCsvBlockedConcepts() {
    return csvBlockedConcepts;
  }

  public List<String> getBlackList() { return blackListTerms; }
}