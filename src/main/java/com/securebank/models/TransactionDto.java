package com.securebank.models;

public class TransactionDto {
  // CAMBIO: Ahora se llaman igual que en el JSON del HTML
  private String sourceIban;
  private String destIban;
  private double amount;
  private String concept;

  public TransactionDto() {}

  public TransactionDto(String sourceIban, String destIban, double amount, String concept) {
    this.sourceIban = sourceIban;
    this.destIban = destIban;
    this.amount = amount;
    this.concept = concept;
  }

  // --- GETTERS Y SETTERS ACTUALIZADOS ---
  public String getSourceIban() { return sourceIban; }
  public void setSourceIban(String sourceIban) { this.sourceIban = sourceIban; }

  public String getDestIban() { return destIban; }
  public void setDestIban(String destIban) { this.destIban = destIban; }

  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }

  public String getConcept() { return concept; }
  public void setConcept(String concept) { this.concept = concept; }
}