package com.securebank.observers;

import com.securebank.fraud.*; // Importamos las reglas del paquete nuevo
import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectorAI implements IBankObserver {

  private final FraudCheck fraudChain;

  public FraudDetectorAI() {
    // CONFIGURACIÓN DE LA CADENA DE SEGURIDAD
    // Aquí definimos el orden de las comprobaciones.
    // Es recomendable poner las más rápidas o críticas primero.

    this.fraudChain = new LimitCheck(); // 1. Primero valida el límite (rápido)

    this.fraudChain
      .linkWith(new BlacklistCheck()) // 2. Busca palabras prohibidas
      .linkWith(new VelocityCheck())  // 3. Comprueba la velocidad (necesita memoria)
      .linkWith(new GeoCheck());      // 4. Comprueba el país de destino
  }

  @Override
  public void onTransactionAttempt(Transaction tx) throws SecurityException {
    // El detector ya no "piensa", solo pasa la transacción al primer guardia.
    // Si alguien en la cadena encuentra un problema, lanzará la excepción.
    fraudChain.check(tx);
  }
}