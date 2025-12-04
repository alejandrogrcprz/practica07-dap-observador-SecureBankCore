package com.securebank.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Esta clase simula la conexión con la Base de Datos del Banco
public class BankRepository {

  private static final String CONFIG_FILE = "securebank_data.config";

  // Listas en memoria (Cache)
  private List<String> activeAccounts = new ArrayList<>();
  private List<String> blackListTerms = new ArrayList<>();

  public BankRepository() {
    loadDataFromCore();
  }

  private void loadDataFromCore() {
    File file = new File(CONFIG_FILE);
    if (!file.exists()) {
      System.err.println("ERROR CRÍTICO: No se encuentra la configuración del Core Bancario.");
      // Datos de emergencia por si falla el archivo
      activeAccounts.add("ES00-0000-0000-00 | Cuenta DEMO (Saldo: 0.00 €)");
      blackListTerms.add("CRYPTO");
      return;
    }

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.startsWith("#") || line.isEmpty()) continue; // Ignorar comentarios

        String[] parts = line.split("\\|");

        if (line.startsWith("ACCOUNT")) {
          // Formateamos bonito para el ComboBox: "IBAN | Alias (Saldo: X €)"
          String iban = parts[1];
          String alias = parts[2];
          String saldo = parts[3];
          // Formateamos el saldo para que se vea bien (punto miles, coma decimal)
          double saldoD = Double.parseDouble(saldo);
          String saldoF = String.format("%,.2f", saldoD);

          activeAccounts.add(iban + " | " + alias + " (Saldo: " + saldoF + " €)");
        }
        else if (line.startsWith("BLACKLIST")) {
          blackListTerms.add(parts[1].toUpperCase());
        }
      }
      System.out.println(">> CONEXIÓN CORE BANCARIO: OK. Datos cargados.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Métodos públicos para que la App pida datos (API interna)
  public String[] getAccountsArray() {
    return activeAccounts.toArray(new String[0]);
  }

  public List<String> getBlackList() {
    return blackListTerms;
  }

  // Método para recargar en caliente (Simulación de actualización de BBDD)
  public void refreshData() {
    activeAccounts.clear();
    blackListTerms.clear();
    loadDataFromCore();
  }
}