package com.securebank.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "accounts")
public class Account {

  @Id
  @Column(length = 30)
  private String iban;

  private String alias;
  private double balance;

  // NUEVO CAMPO: Para saber si está congelada
  private boolean isFrozen = false;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_dni")
  @JsonIgnoreProperties("password")
  private User owner;

  public Account() {}

  public Account(String iban, String alias, double balance, User owner) {
    this.iban = iban;
    this.alias = alias;
    this.balance = balance;
    this.owner = owner;
    this.isFrozen = false;
  }

  // Getters y Setters
  public String getIban() { return iban; }
  public void setIban(String iban) { this.iban = iban; }
  public String getAlias() { return alias; }
  public void setAlias(String alias) { this.alias = alias; }
  public double getBalance() { return balance; }
  public void setBalance(double balance) { this.balance = balance; }
  public User getOwner() { return owner; }
  public void setOwner(User owner) { this.owner = owner; }

  // Getter/Setter nuevo
  public boolean isFrozen() { return isFrozen; }
  public void setFrozen(boolean frozen) { isFrozen = frozen; }
}