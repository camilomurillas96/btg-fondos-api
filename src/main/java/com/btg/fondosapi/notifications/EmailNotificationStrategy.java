package com.btg.fondosapi.notifications;

import org.springframework.stereotype.Service;

@Service("EMAIL") // Le ponemos un nombre clave a este servicio
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public void enviar(String mensaje, String destino) {
        System.out.println("SIMULADOR EMAIL: Enviando a " + destino + ": " + mensaje);
    }
}