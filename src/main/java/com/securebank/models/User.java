package com.securebank.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String dni;
  private String firstName;
  private String lastName;
  private String password; // En producción, esto iría hasheado

  // Constructor vacío requerido por JPA
  public User() {}

  public User(String dni, String firstName, String lastName, String password) {
    this.dni = dni;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
  }

  // Getters
  public Long getId() { return id; }
  public String getDni() { return dni; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getPassword() { return password; }
}