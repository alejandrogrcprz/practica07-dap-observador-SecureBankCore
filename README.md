# 🏦 SecureBank Ultimate Core

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green) ![Architecture](https://img.shields.io/badge/Architecture-Clean-blue) ![Pattern](https://img.shields.io/badge/Pattern-Observer-blueviolet)

**SecureBank Ultimate** es el backend robusto para una plataforma bancaria de nueva generación. Este sistema gestiona transacciones financieras, detección de fraude con IA simulada y lógica de negocio avanzada utilizando patrones de diseño y principios SOLID.

## 🚀 Funcionalidades

### 💳 Core Bancario
* **Gestión de Cuentas:** Registro de usuarios, generación de IBANs y persistencia de datos.
* **Transacciones Atómicas:** Transferencias entre cuentas con validación de saldo y atomicidad.
* **Ingresos y Depósitos:** API para cargar saldo desde fuentes externas.

### 🌟 Sistema Premium & Estrategias
* **Lógica Diferenciada:** Cálculo de comisiones dinámico basado en el nivel del usuario (Standard vs Premium) utilizando el **Patrón Strategy**.
* **Ventajas:** Comisiones reducidas o nulas para usuarios VIP.

### 🛡️ Seguridad y Fraude (Chain of Responsibility)
El sistema implementa un motor de análisis de fraude que evalúa cada transacción en tiempo real a través de una cadena de validadores:
1.  **Blacklist Check:** Verificación de IBANs prohibidos.
2.  **Geo Check:** Detección de ubicaciones sospechosas.
3.  **Velocity Check:** Control de frecuencia de operaciones.
4.  **Limit Check:** Validación de importes máximos.

### 📡 Sistema de Eventos (Observer)
Arquitectura reactiva donde el núcleo notifica eventos sin acoplarse a las implementaciones:
* **Audit Logger:** Registro inmutable de operaciones.
* **Notification Service:** Envío asíncrono de alertas al usuario.
* **General Ledger:** Contabilidad interna del banco.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot (Web, Data JPA)
* **Base de Datos:** H2 Database (Modo memoria/archivo para desarrollo rápido).
* **Frontend:** HTML5, CSS3, JavaScript (integrado en `resources/static` para el panel de control).
* **Build Tool:** Maven.

## 📂 Estructura del Proyecto

```text
src/main/java/com/securebank
├── controllers      # Endpoints REST (API)
├── fraud            # Lógica de detección de fraude (Chain of Responsibility)
├── interfaces       # Contratos de sistema (Observers, Providers)
├── models           # Entidades de Base de Datos (JPA)
├── observers        # Suscriptores de eventos (Logger, Notificaciones)
├── repositories     # Capa de acceso a datos (DAO)
├── services         # Lógica de negocio principal
└── strategies       # Algoritmos de comisiones (Strategy Pattern)
```

## 🔌 API Endpoints

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| **POST** | `/api/register` | Registrar nuevo usuario y cuenta. |
| **POST** | `/api/login` | Autenticación de usuarios. |
| **POST** | `/api/transfer` | Realizar transferencia o Bizum. |
| **POST** | `/api/deposit` | Ingresar dinero (Cajero/Nómina). |
| **GET** | `/api/accounts/{dni}` | Obtener estado y saldo de la cuenta. |
| **GET** | `/api/history/{iban}` | Historial de transacciones. |
| **POST** | `/api/users/{dni}/premium`| Cambiar estado de suscripción. |

## ▶️ Instalación y Ejecución

1. Clonar el repositorio
```
   git clone https://github.com/usuario/securebank-core.git
   cd securebank-core
```
2. Ejecutar la aplicación
```
   ./mvnw spring-boot:run
```
3. Acceder al Panel Web: Abre tu navegador en: http://localhost:8081

---
*Desarrollado como parte del Proyecto Final de la asignatura Diseño Arquitectónico y Patrones (DAP) - Implementación de Patrones de Software.*
