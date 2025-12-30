package com.securebank.services;

import com.securebank.interfaces.IBankObserver;
import com.securebank.models.Transaction;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class FraudDetectorAI implements IBankObserver {

  private Map<String, Long> lastTime = new HashMap<>();
  private Map<String, Double> lastAmount = new HashMap<>();

  // --- DICCIONARIO AVANZADO DE COMPLIANCE ---
  private static final List<String> BLACKLIST_WORDS = new ArrayList<>();

  static {
    // DROGAS Y SUSTANCIAS
    BLACKLIST_WORDS.addAll(Arrays.asList("DROGA", "COCA", "HEROINA", "FENTA", "PASTILLA", "GRAMO", "MERCANCIA", "HIERBA", "CRISTAL", "LSD", "MDMA", "NARCO"));
    // ARMAS Y VIOLENCIA
    BLACKLIST_WORDS.addAll(Arrays.asList("ARMA", "PISTOLA", "BALA", "MUNICION", "CALIBRE", "ESCOPETA", "EXPLOSIVO", "C4", "DETONADOR", "SICARIO", "MUERTE"));
    // DELITOS FINANCIEROS / CORRUPCIÓN
    BLACKLIST_WORDS.addAll(Arrays.asList("SOBORNO", "COIMA", "BLANQUEO", "PARAISO", "OFFSHORE", "TESTAFERRO", "MALETIN", "POLITICO", "ALCALDE"));
    // CIBERCRIMEN Y DARK WEB
    BLACKLIST_WORDS.addAll(Arrays.asList("HACKER", "DDOS", "BOTNET", "RANSOMWARE", "TOR", "ONION", "DARKWEB", "EXPLOIT", "BITCOIN MIXER"));
    // OTROS
    BLACKLIST_WORDS.addAll(Arrays.asList("RESCATE", "SECUESTRO", "TRATA", "ORGANO", "FALSO", "FALSIFICADO"));
  }

  @Override
  public void onTransactionAttempt(Transaction tx) throws SecurityException {
    String iban = tx.getSourceIban();
    double currentAmount = tx.getAmount();
    long now = System.currentTimeMillis();
    String concept = tx.getConcept().toUpperCase();

    // 1. REGLA VELOCIDAD
    if (lastTime.containsKey(iban)) {
      long diff = now - lastTime.get(iban);
      if (diff < 5000) {
        throw new SecurityException("⚠️ Error 429: Solicitud duplicada. Sistema saturado, espere 5 segundos.");
      }
    }

    // 2. REGLA PITUFEO (Mismo importe exacto)
    if (lastAmount.containsKey(iban)) {
      double previousAmount = lastAmount.get(iban);
      // Si coincide importe y ha pasado menos de 1 minuto
      if (Double.compare(currentAmount, previousAmount) == 0 && (now - lastTime.get(iban) < 60000)) {
        throw new SecurityException("ℹ️ Operación cancelada: Posible duplicidad (Importe idéntico detectado).");
      }
    }

    // 3. REGLA LÍMITE
    if (currentAmount > 10000) {
      throw new SecurityException("⛔ Límite excedido (Normativa 1024/2024). Requiere autorización presencial.");
    }

    // 4. REGLA DICCIONARIO NEGRO (Búsqueda parcial)
    for (String badWord : BLACKLIST_WORDS) {
      // Buscamos que la palabra esté contenida (ej: "PAGO COCAINA" detecta "COCA")
      if (concept.contains(badWord)) {
        System.out.println("ALERTA BLOQUEO: " + badWord + " detectado en " + concept);
        throw new SecurityException("🚫 Operación denegada por Política de Cumplimiento (Compliance Code: R-99).");
      }
    }

    // 5. GEO-BLOQUEO
    String destPrefix = tx.getDestIban().substring(0, 2).toUpperCase();
    if (List.of("RU", "KP", "IR", "KY", "SY").contains(destPrefix)) {
      throw new SecurityException("🌐 Destino no autorizado por sanciones internacionales.");
    }

    lastTime.put(iban, now);
    lastAmount.put(iban, currentAmount);
  }
}