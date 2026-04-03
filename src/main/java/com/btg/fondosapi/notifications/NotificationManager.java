package com.btg.fondosapi.notifications;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationManager {

    // La llave del mapa es el nombre clave ("EMAIL" o "SMS")
    private final Map<String, NotificationStrategy> estrategias;

    public NotificationManager(Map<String, NotificationStrategy> estrategias) {
        this.estrategias = estrategias;
    }

    public void notificar(String preferencia, String mensaje, String destino) {
        NotificationStrategy estrategia = estrategias.get(preferencia.toUpperCase());

        if (estrategia != null) {
            estrategia.enviar(mensaje, destino);
        } else {
            System.out.println("Preferencia de notificación desconocida: " + preferencia);
        }
    }
}