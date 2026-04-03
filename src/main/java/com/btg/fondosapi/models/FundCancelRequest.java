package com.btg.fondosapi.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FundCancelRequest {

    @NotBlank(message = "El ID del cliente no puede estar vacío")
    private String clienteId;

    @NotBlank(message = "El ID del fondo no puede estar vacío")
    private String fondoId;
}