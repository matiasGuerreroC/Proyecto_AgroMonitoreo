# AgroMonitoreo - Sistema Distribuido de Consulta Climática con Java RMI

## Descripción

**AgroMonitoreo** es una aplicación cliente-servidor distribuida desarrollada en Java, que permite consultar el clima actual de ciudades chilenas utilizando la API de OpenWeatherMap. El servidor obtiene los datos climáticos, los procesa, guarda un historial en una base de datos MySQL y expone esta información a través de Java RMI (Remote Method Invocation).

## Características principales

* Consulta del clima actual de ciudades chilenas, incluyendo nombres compuestos (ej. "Viña del Mar").
* Almacenamiento automático del historial de consultas en una base de datos MySQL.
* Generación de alertas climáticas básicas según temperatura, humedad y condiciones meteorológicas.
* Servicios disponibles a través de Java RMI para interacción remota entre cliente y servidor.

## Requisitos

* **Java 11 o superior**
* **MySQL** (se recomienda usar [XAMPP](https://www.apachefriends.org/index.html) para facilitar la instalación y gestión)
* Conexión a Internet para acceder a la API de OpenWeatherMap.
* Clave API de OpenWeatherMap (disponible gratis en [https://openweathermap.org/api](https://openweathermap.org/api)).

## Configuración de la base de datos

1. Instalar y ejecutar XAMPP para levantar el servicio de MySQL.
2. El proyecto creará automáticamente la base de datos `clima` y la tabla `clima_ciudad` si no existen.
3. Verificar las credenciales de MySQL en `ServerImpl.java`. Por defecto se usa:

   * Usuario: `root`
   * Contraseña: *(vacía)*

## Uso

1. Ejecutar el servidor con la clase `RunServer.java`. Esto inicializa el servidor RMI, conecta a la base de datos y lo deja listo para recibir solicitudes.
2. Luego, ejecutar el cliente con la clase `RunCliente.java`. El cliente permite:

   * Consultar el clima actual de una ciudad.
   * Ver el historial de consultas.
   * Generar alertas climáticas automáticas.
3. Las consultas codifican correctamente los nombres de ciudades con espacios y caracteres especiales, para evitar errores con la API.

## Estructura del proyecto

* `server/` - Implementación del servidor RMI, integración con la API y acceso a la base de datos.
* `common/` - Clases compartidas entre cliente y servidor (interfaces remotas y modelos de datos).
* `client/` - Cliente RMI que interactúa con el servidor.
* `lib/` - Librerías externas necesarias para compilar y ejecutar:

  * `jackson-annotations-2.14.0.jar`
  * `jackson-core-2.14.0.jar`
  * `jackson-databind-2.14.0.jar`
  * `mysql-connector-j-8.3.0.jar`

> Asegúrate de incluir todos los `.jar` del directorio `lib/` en el classpath al compilar y ejecutar el proyecto.

## Convenciones y notas

* Las ciudades con espacios o caracteres especiales son codificadas correctamente en UTF-8.
* El historial almacena fecha y hora utilizando tipos `DATE` y `TIME` en MySQL.
* El servidor maneja errores HTTP y excepciones para evitar fallos en tiempo de ejecución.

---
