package com.securebank.repositories;

import com.securebank.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

  // ESTE ES EL MÉTODO QUE FALTABA:
  // Busca todas las cuentas que pertenezcan a un usuario con este DNI
  List<Account> findByOwnerDni(String dni);
}