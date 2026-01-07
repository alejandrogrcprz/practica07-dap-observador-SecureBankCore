package com.securebank.repositories;

import com.securebank.models.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
  // Buscamos por el DNI del propietario
  List<SavingsGoal> findByOwnerDni(String dni);
}