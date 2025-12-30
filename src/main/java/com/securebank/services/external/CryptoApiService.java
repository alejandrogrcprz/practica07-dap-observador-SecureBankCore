package com.securebank.services.external;

import com.securebank.interfaces.IExchangeProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoApiService implements IExchangeProvider {

  @Override
  public double getLiveRate(String from, String to) {
    // Ejemplo simplificado: Obtener precio de Bitcoin en EUR
    // URL: https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur
    try {
      String id = from.toLowerCase().equals("btc") ? "bitcoin" : "ethereum";
      String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + id + "&vs_currencies=" + to.toLowerCase();

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // Usamos Regex para extraer el precio del JSON sin añadir librerías externas
      Pattern p = Pattern.compile("(\"eur\":)(\\d+\\.?\\d*)");
      Matcher m = p.matcher(response.body());

      if (m.find()) {
        return Double.parseDouble(m.group(2));
      }
    } catch (Exception e) {
      System.err.println("Error al conectar con la API de Cripto: " + e.getMessage());
    }
    return 0.0; // Valor por defecto si falla
  }
}