package com.securebank.repositories;

import com.securebank.models.CryptoHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CryptoRepository extends JpaRepository<CryptoHolding, Long> {
  // Spring es listo: "findByOwnerId" busca dentro del objeto 'owner' su campo 'id'
  List<CryptoHolding> findByOwnerId(Long userId);

  Optional<CryptoHolding> findByOwnerIdAndSymbol(Long userId, String symbol);
}