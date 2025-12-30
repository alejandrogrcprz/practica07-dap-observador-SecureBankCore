package com.securebank.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sourceIban;
  private String destIban;
  private double amount;
  private String concept;
  private LocalDateTime date;

  public Transaction() {}

  public Transaction(String sourceIban, String destIban, double amount, String concept, LocalDateTime date) {
    this.sourceIban = sourceIban;
    this.destIban = destIban;
    this.amount = amount;
    this.concept = concept;
    this.date = date;
  }

  public Long getId() { return id; }
  public String getSourceIban() { return sourceIban; }
  public String getDestIban() { return destIban; }
  public double getAmount() { return amount; }
  public String getConcept() { return concept; }
  public LocalDateTime getDate() { return date; }

  public void setSourceIban(String sourceIban) { this.sourceIban = sourceIban; }
  public void setDestIban(String destIban) { this.destIban = destIban; }
  public void setAmount(double amount) { this.amount = amount; }
  public void setConcept(String concept) { this.concept = concept; }
  public void setDate(LocalDateTime date) { this.date = date; }
}