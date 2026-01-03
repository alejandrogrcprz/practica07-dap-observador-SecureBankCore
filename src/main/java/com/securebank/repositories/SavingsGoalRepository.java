package com.securebank.repositories;

import com.securebank.models.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
  List<SavingsGoal> findByOwnerId(Long userId);
}