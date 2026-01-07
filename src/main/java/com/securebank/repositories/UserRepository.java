package com.securebank.repositories;

import com.securebank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// CAMBIO CLAVE: Cambiamos <User, Long> por <User, String>
// porque el DNI es el identificador y es un Texto.
@Repository
public interface UserRepository extends JpaRepository<User, String> {
  // Si tuvieras métodos extra, irían aquí
}