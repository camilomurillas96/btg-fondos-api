package com.btg.fondosapi.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FundSubscriptionRequest {

    @NotBlank(message = "El ID del cliente no puede estar vacío")
    private String clienteId;

    @NotBlank(message = "El ID del fondo no puede estar vacío")
    private String fondoId;

    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto a invertir debe ser mayor a 0") // Validación de seguridad
    private Integer monto;
}