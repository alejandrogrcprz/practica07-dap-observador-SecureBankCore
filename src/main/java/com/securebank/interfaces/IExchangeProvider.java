package com.securebank.interfaces;

public interface IExchangeProvider {
  // Obtiene el precio real de una API (Cripto o Fiat)
  double getLiveRate(String from, String to);
}