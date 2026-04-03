package com.btg.fondosapi.controllers;

import com.btg.fondosapi.models.FundSubscriptionRequest;
import com.btg.fondosapi.services.FundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.btg.fondosapi.models.FundCancelRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/funds")
public class FundController {

    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    // POST http://localhost:8080/api/v1/funds/subscribe
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> suscribirse(
            @Valid @RequestBody FundSubscriptionRequest request) {
        // @Valid revisa que el monto sea > 0 y los IDs no estén vacíos

        String transaccionId = fundService.suscribirClienteFondo(
                request.getClienteId(),
                request.getFondoId(),
                request.getMonto()
        );

        // Prepara una respuesta bonita en JSON
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Suscripción exitosa");
        respuesta.put("transaccion_id", transaccionId);

        // Retorna un 200 OK
        return ResponseEntity.ok(respuesta);
    }


    // DELETE http://localhost:8080/api/v1/funds/unsubscribe
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> cancelar(
            @Valid @RequestBody FundCancelRequest request) {

        // Aquí simulo que le devolvemos un monto fijo.
        Integer montoADevolver = 75000;

        String transaccionId = fundService.cancelarSuscripcionFondo(
                request.getClienteId(),
                request.getFondoId(),
                montoADevolver
        );

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Suscripción cancelada exitosamente");
        respuesta.put("transaccion_id", transaccionId);

        return ResponseEntity.ok(respuesta);
    }

    // Historial: GET http://localhost:8080/api/v1/funds/{clienteId}/transactions
    @GetMapping("/{clienteId}/transactions")
    public ResponseEntity<List<Map<String, String>>> verHistorial(@PathVariable String clienteId) {

        List<Map<String, String>> historial = fundService.obtenerHistorialTransacciones(clienteId);
        return ResponseEntity.ok(historial);
    }
}