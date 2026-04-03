package com.btg.fondosapi.notifications;

public interface NotificationStrategy {
    void enviar(String mensaje, String destino);
}