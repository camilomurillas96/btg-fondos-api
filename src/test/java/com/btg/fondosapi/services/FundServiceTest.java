package com.btg.fondosapi.services;

import com.btg.fondosapi.notifications.NotificationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    @Mock // Creamos una base de datos AWS Falsa
    private DynamoDbClient dynamoDbClientFalso;

    @Mock // Creamos un enviador de notificaciones Falso
    private NotificationManager notificationManagerFalso;

    // Nuestro trabajador real, al que le inyectaremos las herramientas falsas
    private FundService fundService;

    @BeforeEach
    void prepararTodo() {
        // Antes de cada prueba, armamos a nuestro trabajador con los clones
        fundService = new FundService(dynamoDbClientFalso, "tabla-prueba", notificationManagerFalso);
    }

    @Test
    void testSuscripcionExitosa() {
        // Preparación: Le decimos a la BD Falsa que cuando le pidan actualizar, responda que todo salió bien.
        Mockito.when(dynamoDbClientFalso.updateItem(ArgumentMatchers.any(UpdateItemRequest.class)))
                .thenReturn(UpdateItemResponse.builder().build());

        // Ejecución: Mandamos a un cliente a suscribirse
        String transaccionId = fundService.suscribirClienteFondo("USER#1", "FDO-ACCIONES", 250000);

        // Verificación: Comprobamos que sí nos devolvió un ID y que no falló
        assertNotNull(transaccionId, "El ID de transacción no debería ser nulo");

        // Verificamos que sí intentó guardar el recibo en la base de datos
        Mockito.verify(dynamoDbClientFalso, Mockito.times(1))
                .putItem(ArgumentMatchers.any(PutItemRequest.class));
    }

    @Test
    void testSuscripcionFallaPorSaldoInsuficiente() {
        // Preparación: Le decimos a la BD Falsa que SIMULE el error de DynamoDB
        // que ocurre cuando la condición (saldo >= monto) no se cumple.
        Mockito.when(dynamoDbClientFalso.updateItem(ArgumentMatchers.any(UpdateItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        // Ejecución y Verificación:
        // Comprobamos que cuando el cliente intente suscribirse, nuestro código lance
        // exactamente el error 400 (Bad Request) con el mensaje que pidió BTG Pactual.
        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class, () -> {
            fundService.suscribirClienteFondo("USER#1", "FDO-ACCIONES", 1000000); // Intenta invertir un millón
        });

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatusCode());
        assertEquals("No tiene saldo disponible para vincularse al fondo FDO-ACCIONES", excepcion.getReason());
    }

    @Test
    void testConcurrenciaDobleClic() {

        Mockito.when(dynamoDbClientFalso.updateItem(ArgumentMatchers.any(UpdateItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        assertThrows(ResponseStatusException.class, () -> {
            fundService.suscribirClienteFondo("USER#2", "DEUDAPRIVADA", 50000);
        });
    }
}