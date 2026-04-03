package com.btg.fondosapi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.btg.fondosapi.notifications.*;

import java.time.Instant;
import java.util.*;

@Service
public class FundService {

    // llamar a AWS
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    private final NotificationManager notificationManager;

    public FundService(DynamoDbClient dynamoDbClient,
                       @Value("${dynamodb.table.name}") String tableName,
                       NotificationManager notificationManager) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        this.notificationManager = notificationManager;
    }

    public String suscribirClienteFondo(String clienteId, String fondoId, Integer monto) {
        String transaccionId = UUID.randomUUID().toString();
        String fechaActual = Instant.now().toString();

        // Le decimos a DynamoDB a qué registro vamos a apuntar
        Map<String, AttributeValue> llaveCliente = new HashMap<>();
        llaveCliente.put("PK", AttributeValue.builder().s(clienteId).build());
        llaveCliente.put("SK", AttributeValue.builder().s("PROFILE").build());

        // Definimos la variable matemática (el monto que vamos a restar)
        Map<String, AttributeValue> valores = new HashMap<>();
        valores.put(":monto", AttributeValue.builder().n(String.valueOf(monto)).build());

        // 3. Preparamos la orden exacta a la base de datos
        UpdateItemRequest actualizacionSegura = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(llaveCliente)
                .updateExpression("SET saldo = saldo - :monto") // Restamos
                .conditionExpression("saldo >= :monto") // CONDICIÓN CRÍTICA
                .expressionAttributeValues(valores)
                .build();

        try {
            // Disparamos la actualización condicional
            dynamoDbClient.updateItem(actualizacionSegura);


            // guardamos el recibo
            guardarTransaccion(clienteId, fondoId, monto, transaccionId, fechaActual, "APERTURA");
            String mensajeNotificacion = "Hola, te has suscrito exitosamente al fondo " + fondoId + " por un monto de $" + monto;
            notificationManager.notificar("EMAIL", mensajeNotificacion, "cliente@ejemplo.com");
            return transaccionId;

        } catch (ConditionalCheckFailedException e) {
            // Si la base de datos detecta que el saldo era menor al monto, rechaza la operación.
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No tiene saldo disponible para vincularse al fondo " + fondoId
            );
        }
    }

    // método auxiliar privado para guardar el historial ordenadamente
    private void guardarTransaccion(String clienteId, String fondoId, Integer monto, String transaccionId, String fecha, String tipo) {
        Map<String, AttributeValue> itemTransaccion = new HashMap<>();
        itemTransaccion.put("PK", AttributeValue.builder().s(clienteId).build());
        itemTransaccion.put("SK", AttributeValue.builder().s("TX#" + fecha + "#" + transaccionId).build());
        itemTransaccion.put("fondo_id", AttributeValue.builder().s(fondoId).build());
        itemTransaccion.put("monto", AttributeValue.builder().n(String.valueOf(monto)).build());
        itemTransaccion.put("tipo", AttributeValue.builder().s(tipo).build());

        PutItemRequest guardarRecibo = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemTransaccion)
                .build();

        dynamoDbClient.putItem(guardarRecibo);
    }

    public String cancelarSuscripcionFondo(String clienteId, String fondoId, Integer montoDevolucion) {
        String transaccionId = UUID.randomUUID().toString();
        String fechaActual = Instant.now().toString();

        // Apunta al perfil del cliente
        Map<String, AttributeValue> llaveCliente = new HashMap<>();
        llaveCliente.put("PK", AttributeValue.builder().s(clienteId).build());
        llaveCliente.put("SK", AttributeValue.builder().s("PROFILE").build());

        // Define el monto que vamos a sumar
        Map<String, AttributeValue> valores = new HashMap<>();
        valores.put(":monto", AttributeValue.builder().n(String.valueOf(montoDevolucion)).build());

        // Devuelve el dinero al saldo
        UpdateItemRequest actualizacionSaldo = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(llaveCliente)
                .updateExpression("SET saldo = saldo + :monto") // Ahora sumamos
                .expressionAttributeValues(valores)
                .build();

        dynamoDbClient.updateItem(actualizacionSaldo);

        // Guarda el recibo de la cancelación
        guardarTransaccion(clienteId, fondoId, montoDevolucion, transaccionId, fechaActual, "CANCELACION");
        return transaccionId;
    }

    public List<Map<String, String>> obtenerHistorialTransacciones(String clienteId) {
        // DynamoDB "Busca a este cliente y tráe solo lo que empiece con TX#"
        Map<String, AttributeValue> valoresBusqueda = new HashMap<>();
        valoresBusqueda.put(":pk", AttributeValue.builder().s(clienteId).build());
        valoresBusqueda.put(":sk_prefix", AttributeValue.builder().s("TX#").build());

        QueryRequest busqueda = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("PK = :pk and begins_with(SK, :sk_prefix)")
                .expressionAttributeValues(valoresBusqueda)
                .build();

        QueryResponse respuestaDb = dynamoDbClient.query(busqueda);
        List<Map<String, String>> historial = new ArrayList<>();

        // Convierte la respuesta cruda de AWS a una lista bonita para el usuario
        for (Map<String, AttributeValue> item : respuestaDb.items()) {
            Map<String, String> transaccion = new HashMap<>();
            transaccion.put("id", item.get("SK").s().replace("TX#", "")); // Limpiamos el prefijo
            transaccion.put("tipo", item.get("tipo").s());
            transaccion.put("fondo_id", item.get("fondo_id").s());
            transaccion.put("monto", item.get("monto").n());
            historial.add(transaccion);
        }

        return historial;
    }
}