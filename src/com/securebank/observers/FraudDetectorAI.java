package com.securebank.observers;
import com.securebank.interfaces.IBankObserver;

public class FraudDetectorAI implements IBankObserver {
  @Override
  public void onTransactionExecuted(String transactionData) {
    System.out.println("   -> [FRAUDE AI] Analizando patrón de comportamiento... (IP, Ubicación)");
    // Aquí iría la lógica de conectar con la API externa en el futuro
    System.out.println("   -> [FRAUDE AI] Resultado: Transacción SEGURA.");
  }
}