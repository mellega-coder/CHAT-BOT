package com.chatbot.model;
import com.chatbot.model.Conversacion;

public class Conversacion {

    private int ultimoProductoId;
    private String ultimaIntencion;

    public int getUltimoProductoId() {
        return ultimoProductoId;
    }

    public void setUltimoProductoId(int ultimoProductoId) {
        this.ultimoProductoId = ultimoProductoId;
    }

    public String getUltimaIntencion() {
        return ultimaIntencion;
    }

    public void setUltimaIntencion(String ultimaIntencion) {
        this.ultimaIntencion = ultimaIntencion;
    }
}