package com.btg.fondosapi.notifications;

import org.springframework.stereotype.Service;

@Service("SMS")
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public void enviar(String mensaje, String destino) {
        System.out.println("SIMULADOR SMS: Enviando al número " + destino + ": " + mensaje);
    }
}