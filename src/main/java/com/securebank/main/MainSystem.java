package com.securebank.main;

import com.securebank.models.*;
import com.securebank.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Random;

@SpringBootApplication(scanBasePackages = "com.securebank")
@EnableJpaRepositories(basePackages = "com.securebank.repositories")
@EntityScan(basePackages = "com.securebank.models")
public class MainSystem {

  public static void main(String[] args) {
    // CAMBIO RADICAL: Usamos una base de datos nueva 'bankdb_v2' y simplificamos la URL
    System.setProperty("spring.datasource.url", "jdbc:h2:file:./data/bankdb_v2;AUTO_SERVER=TRUE");

    // Arrancamos la aplicación
    new SpringApplicationBuilder(MainSystem.class)
      .headless(false)
      .run(args);
  }

  // --- GENERADOR DE DATOS DE PRUEBA ---
  @Bean
  public CommandLineRunner dataSeeder(UserRepository userRepo, AccountRepository accRepo) {
    return args -> {
      if (userRepo.count() > 0) {
        System.out.println(">> LA BASE DE DATOS YA TIENE DATOS. OMITIENDO SEED.");
        return;
      }

      System.out.println(">> GENERANDO POBLACIÓN DE PRUEBA...");
      String[] nombres = {"Antonio", "Carmen", "Manuel", "Josefa", "Francisco", "Ana", "David", "Maria", "Javier", "Isabel"};
      String[] apellidos = {"Garcia", "Gonzalez", "Rodriguez", "Fernandez", "Lopez", "Martinez", "Sanchez", "Perez", "Gomez", "Martin"};
      Random rand = new Random();

      // 1. Crear 20 Usuarios Random
      for (int i = 0; i < 20; i++) {
        String nombre = nombres[rand.nextInt(nombres.length)];
        String apellido1 = apellidos[rand.nextInt(apellidos.length)];
        String apellido2 = apellidos[rand.nextInt(apellidos.length)];
        String dni = (rand.nextInt(89999999) + 10000000) + getRandomLetter();

        User u = new User(dni, nombre, apellido1 + " " + apellido2, "1234");
        // Aseguramos que los usuarios random no sean premium por defecto (para probar comisiones)
        u.setPremium(false);
        userRepo.save(u);

        // 2. Crear entre 1 y 3 cuentas por usuario
        int numCuentas = rand.nextInt(3) + 1;
        for (int j = 0; j < numCuentas; j++) {
          String iban = generateEspIban(rand);
          double saldo = 1000 + (rand.nextDouble() * 50000);
          accRepo.save(new Account(iban, "Cuenta " + (j==0?"Principal":"Ahorro"), saldo, u));
        }
      }

      // Usuario Admin Fijo para ti
      User admin = new User("11111111A", "Admin", "Supremo", "admin");
      admin.setPremium(true); // El admin es Premium (0 comisiones)
      userRepo.save(admin);
      accRepo.save(new Account("ES0000000000000000000001", "Bóveda Central", 1000000.0, admin));

      System.out.println(">> BASE DE DATOS INICIALIZADA CON EXITO.");
    };
  }

  // Métodos auxiliares
  private String getRandomLetter() {
    String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
    return String.valueOf(letters.charAt(new Random().nextInt(letters.length())));
  }

  private String generateEspIban(Random r) {
    StringBuilder sb = new StringBuilder("ES");
    for(int i=0; i<22; i++) sb.append(r.nextInt(10));
    return sb.toString();
  }
}