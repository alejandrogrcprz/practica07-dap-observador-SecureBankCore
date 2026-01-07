package com.securebank.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

  @Id
  @Column(length = 20)
  private String dni;

  private String firstName;
  private String lastName;
  private String password;

  private boolean isPremium;
  private double accumulatedCashback;

  // --- NUEVOS CAMPOS DE PERSISTENCIA ---
  private double savingsBalance; // El dinero de la hucha
  private boolean roundUpActive; // Si el redondeo está activado

  public User() {}

  public User(String dni, String firstName, String lastName, String password) {
    this.dni = dni;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.isPremium = false;
    this.accumulatedCashback = 0.0;
    this.savingsBalance = 0.0; // Empieza vacía
    this.roundUpActive = false; // Empieza desactivado
  }

  // Getters y Setters normales...
  public String getDni() { return dni; }
  public void setDni(String dni) { this.dni = dni; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public boolean isPremium() { return isPremium; }
  public void setPremium(boolean premium) { isPremium = premium; }
  public double getAccumulatedCashback() { return accumulatedCashback; }
  public void setAccumulatedCashback(double accumulatedCashback) { this.accumulatedCashback = accumulatedCashback; }

  // Nuevos Getters y Setters
  public double getSavingsBalance() { return savingsBalance; }
  public void setSavingsBalance(double savingsBalance) { this.savingsBalance = savingsBalance; }
  public boolean isRoundUpActive() { return roundUpActive; }
  public void setRoundUpActive(boolean roundUpActive) { this.roundUpActive = roundUpActive; }
}