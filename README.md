# 🏦 API de Gestión de Fondos de Inversión - BTG Pactual

Plataforma backend diseñada para permitir a los clientes gestionar su vinculación a fondos de inversión de forma autónoma. Este proyecto fue construido aplicando arquitectura limpia, principios SOLID y está optimizado para su despliegue en la nube con AWS.

## 🏗️ Arquitectura del Sistema

El sistema utiliza un diseño de modelo de datos **Single-Table en DynamoDB**, separando claramente las responsabilidades en capas (Controladores, Servicios, Modelos) para garantizar un código mantenible y escalable.

- **Frontend / Cliente:** Realiza peticiones HTTP REST.
- **Controladores:** Validan la información de entrada.
- **Servicios:** Contienen la lógica matemática y de negocio.
- **Base de Datos:** AWS DynamoDB.
- **Notificaciones:** Patrón de diseño Strategy para simular envíos de SMS o Email de forma dinámica.

## 🛠️ Stack Tecnológico Utilizado
* **Lenguaje:** Java 21 (LTS)
* **Framework:** Spring Boot 3.x
* **Base de Datos:** AWS DynamoDB
* **Infraestructura:** AWS CloudFormation (Infraestructura como Código)
* **Testing:** JUnit 5 + Mockito

## ⚠️ Resolución Crítica: Concurrencia e Integridad de Datos

Para evitar condiciones de carrera (por ejemplo, un cliente haciendo doble clic rápido y descontando saldo que no tiene), **NO** se utilizaron bloqueos a nivel de aplicación (Mutex/Synchronized), ya que no son escalables ni seguros en entornos distribuidos.

La solución implementada fue **Optimistic Concurrency Control nativo en la base de datos** utilizando `ConditionExpression` de DynamoDB.
La transacción envía una orden matemática atómica: `SET saldo = saldo - monto CONDITION saldo >= monto`. Si dos solicitudes concurrentes intentan procesar la petición al mismo tiempo y el saldo ya no es suficiente, DynamoDB rechaza la transacción con un error `ConditionalCheckFailedException`. La API captura este error de forma elegante y lo traduce en un mensaje HTTP 400 claro para el cliente, protegiendo así la integridad financiera del sistema al 100%.

## 🚀 Prerequisitos para ejecución local
* Java 21 instalado.
* Maven instalado.
* Editor de código (IntelliJ IDEA o similar).

## ⚙️ Instrucciones de Instalación
1. Clonar el repositorio en tu máquina local.
2. Abrir el proyecto y permitir que Maven descargue las dependencias.
3. En la carpeta `src/main/resources`, verificar que exista el archivo `application.properties` con las variables de entorno de AWS (se pueden usar credenciales simuladas para pruebas locales).
4. Para ejecutar las pruebas unitarias y validar la lógica, usar el comando:
   `mvn clean test`
5. Para levantar la aplicación, usar el comando:
   `mvn spring-boot:run`

## ☁️ Despliegue en AWS
La infraestructura completa está declarada en la carpeta `/cloudformation`. El archivo `template.yaml` crea la tabla con facturación bajo demanda (Pay-Per-Request) e implementa roles IAM siguiendo el **principio de mínimo privilegio**, asegurando que la API solo tenga permisos de escritura/lectura exclusivamente sobre la tabla del proyecto.

## 📚 Endpoints (Rutas de la API)

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/funds/subscribe` | Vincula a un cliente a un fondo de inversión |
| `DELETE` | `/api/v1/funds/unsubscribe` | Cancela una suscripción activa y devuelve el saldo |
| `GET` | `/api/v1/funds/{clienteId}/transactions` | Obtiene el historial completo de transacciones del cliente |

---
**Desarrollado por:** Andrés Camilo González Murillas
*Ingeniero de Sistemas* | acamilogonzalez96@gmail.com