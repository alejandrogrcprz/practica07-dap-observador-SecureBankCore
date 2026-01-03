package com.securebank.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class SavingsGoal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;          // Nombre (ej: "Viaje Japón")
  private double targetAmount;  // Objetivo (ej: 3000)
  private double currentAmount; // Ahorrado (ej: 500)

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User owner;

  public SavingsGoal() {}

  public SavingsGoal(String name, double targetAmount, User owner) {
    this.name = name;
    this.targetAmount = targetAmount;
    this.currentAmount = 0.0;
    this.owner = owner;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public double getTargetAmount() { return targetAmount; }
  public double getCurrentAmount() { return currentAmount; }
  public User getOwner() { return owner; }

  public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
}