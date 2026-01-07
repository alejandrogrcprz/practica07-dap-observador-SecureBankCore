package com.securebank.repositories;

import com.securebank.models.CryptoHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoRepository extends JpaRepository<CryptoHolding, Long> {
  // Buscamos por el DNI del propietario (User.dni)
  List<CryptoHolding> findByOwnerDni(String dni);

  // Buscamos por DNI y Símbolo
  Optional<CryptoHolding> findByOwnerDniAndSymbol(String dni, String symbol);
}