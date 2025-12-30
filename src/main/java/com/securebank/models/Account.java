package com.securebank.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Account {
  @Id
  private String iban;
  private String alias;
  private double balance;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User owner;

  public Account() {}

  public Account(String iban, String alias, double balance, User owner) {
    this.iban = iban;
    this.alias = alias;
    this.balance = balance;
    this.owner = owner;
  }

  public String getIban() { return iban; }
  public void setIban(String iban) { this.iban = iban; }
  public String getAlias() { return alias; }
  public void setAlias(String alias) { this.alias = alias; }
  public double getBalance() { return balance; }
  public void setBalance(double balance) { this.balance = balance; }
  public User getOwner() { return owner; }
  public void setOwner(User owner) { this.owner = owner; }
}