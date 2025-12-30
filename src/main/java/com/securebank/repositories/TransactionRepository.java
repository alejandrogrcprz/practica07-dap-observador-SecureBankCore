package com.securebank.repositories;
import com.securebank.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  // Buscar transacciones donde la cuenta sea origen O destino
  List<Transaction> findBySourceIbanOrDestIbanOrderByDateDesc(String source, String dest);
}