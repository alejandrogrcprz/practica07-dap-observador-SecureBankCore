# 🏦 SecureBank Core - Sistema de Infraestructura Bancaria

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Pattern](https://img.shields.io/badge/Design_Pattern-Observer-blue?style=for-the-badge)
![GUI](https://img.shields.io/badge/GUI-Swing-green?style=for-the-badge)

**SecureBank Core** es un simulador de infraestructura bancaria crítica diseñado para demostrar la implementación práctica del **Patrón de Diseño Observador (Observer Pattern)** en un entorno de alta concurrencia.

El sistema simula el ciclo de vida completo de las transacciones financieras: desde la app del cliente hasta el núcleo bancario, pasando por motores de IA antifraude y registros inmutables (Ledger).

---

## 📋 Tabla de Contenidos
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Características Principales](#-características-principales)
- [Motor de Seguridad (Fraud AI)](#-motor-de-seguridad-fraud-ai)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Guía de Prueba (Demo)](#-guía-de-prueba-demo)
- [Configuración de Datos](#-configuración-de-datos)

---

## 🏗 Arquitectura del Sistema

El proyecto sigue una arquitectura **Event-Driven** (dirigida por eventos) desacoplada mediante el Patrón Observador.

### 1. El Sujeto (Subject)
* **`TransactionEngine`:** Es el corazón del sistema. Procesa las solicitudes de transferencia y emite eventos de difusión (*broadcast*) a todos los componentes conectados. No conoce la identidad de los observadores, garantizando el principio *Open/Closed*.

### 2. Los Observadores (Observers)
Componentes independientes que reaccionan a los eventos del motor en tiempo real:

| Componente | Rol | Responsabilidad |
|:---|:---|:---|
| **📱 MobilePhoneSimulator** | Frontend | Simula dispositivos de clientes. Filtra eventos, gestiona sesiones y notificaciones Push. |
| **🖥️ BankAdminConsole** | Dashboard | Centro de Operaciones (NOC). Visualiza tráfico, métricas de infraestructura y logs. |
| **🛡️ FraudDetectorAI** | Seguridad | Analiza patrones en tiempo real (Behavioral Analytics) y bloquea operaciones sospechosas. |
| **📒 GeneralLedger** | Integridad | Simulación de Blockchain. Genera Hashes SHA-256 inmutables para cada transacción. |
| **📩 NotificationService** | Comms | Simula el envío de correos y SMS transaccionales. |
| **📜 AuditLogger** | Auditoría | Registra trazas técnicas para cumplimiento normativo. |

---

## ✨ Características Principales

* **Persistencia Real:** Los saldos y transacciones se guardan en disco (`bank_accounts.csv`), permitiendo la continuidad de datos entre ejecuciones.
* **Simulación Multi-Dispositivo:** Capacidad para lanzar múltiples instancias de la App Móvil para simular emisor y receptor simultáneamente.
* **UX/UI Profesional:**
    * Login mediante DNI.
    * Autocompletado de beneficiarios por IBAN.
    * Generación de **Recibos Digitales** (HTML) con validación visual.
    * Notificaciones tipo "Push" y centro de mensajes.
* **Protocolo de Comunicación:** Uso de mensajería estructurada (`TX##ORIGEN##DESTINO...`) para la comunicación entre componentes.

---

## 🚨 Motor de Seguridad (Fraud AI)

El sistema implementa un motor de **Behavioral Analytics** (Análisis de Comportamiento) que evalúa riesgos en tiempo real:

1.  **Velocity Check:** Bloquea intentos de transacción inhumanamente rápidos (<10s entre operaciones) típicos de bots.
2.  **Pattern Recognition:** Detecta "Pitufeo" (Structuring) y patrones de números redondos sospechosos de sobornos.
3.  **Geo-Blocking & AML:** Bloquea fugas de capitales a IBANs no nacionales (fuera de zona SEPA) con importes altos.
4.  **Blacklist Dinámica:** Filtra conceptos prohibidos cargados desde un fichero externo (`blocked_concepts.csv`).

---

## 🚀 Instalación y Ejecución

### Requisitos
* Java Development Kit (JDK) 11 o superior.
* IDE recomendado: IntelliJ IDEA o Eclipse.

### Pasos
1.  Clona el repositorio.
2.  Asegúrate de que los archivos de datos están en la **raíz del proyecto** (al mismo nivel que la carpeta `src`):
    * `bank_accounts.csv`
    * `blocked_concepts.csv`
3.  Ejecuta la clase principal: `src/com/securebank/main/MainSystem.java`.

---

## 🧪 Guía de Prueba (Demo)

Sigue este guion para probar todas las capacidades del sistema:

### Escenario 1: Transferencia Exitosa
1.  Inicia el programa. Se abrirán 3 ventanas: Dashboard y 2 Móviles.
2.  **Móvil A:** Inicia sesión como **Antonio** (DNI: `41293847S` / PIN: `1234`).
3.  **Móvil B:** Inicia sesión como **Maria** (DNI: `03928174Q` / PIN: `1234`).
4.  En el **Móvil A**:
    * Ve a "Transferir".
    * Copia el IBAN de Maria del Móvil B.
    * Pégalo en destinatario y pulsa TAB (verás que aparece "Maria Lopez" en verde).
    * Envía **150€** con concepto "Cena".
5.  **Resultado:**
    * Antonio recibe ventana para descargar recibo.
    * Maria recibe notificación verde inmediata (+150€).
    * El Dashboard registra la operación en la tabla.

### Escenario 2: Detección de Fraude
1.  En el **Móvil A**, intenta enviar dinero.
2.  En el concepto escribe: **"Pago de ARMAS"**.
3.  Pulsa enviar.
4.  **Resultado:**
    * El Dashboard (Panel Fraud AI) comienza a escanear.
    * Se detecta "Lista Negra" en rojo.
    * El móvil muestra "Operación rechazada por política de seguridad".
    * No se descuenta dinero.

---

## ⚙️ Configuración de Datos

El sistema es 100% configurable mediante archivos CSV en la raíz:

* **`bank_accounts.csv`**: Base de datos de clientes.
    * Formato: `DNI,CLIENTE,IBAN,TIPO_CUENTA,SALDO`
* **`blocked_concepts.csv`**: Diccionario de términos para el motor AML.
    * Formato: Una palabra prohibida por línea.

---

**Autor:** [Alejandro García Pérez - alu0101441207]  
**Asignatura:** Diseño arquitectónicos y patrones
**Tecnología:** Java Swing + Observer Pattern
