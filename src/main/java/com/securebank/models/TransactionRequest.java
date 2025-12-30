package com.securebank.models;

/**
 * Patrón DTO: Objeto para transportar los datos de la solicitud
 * sin exponer la lógica del negocio.
 */
public class TransactionRequest {
  public String sourceIban;
  public String destIban;
  public double amount;
  public String currency; // "EUR", "BTC", "ETH"
  public String beneficiary;
  public String concept;
}