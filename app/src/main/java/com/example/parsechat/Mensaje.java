package com.example.parsechat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Mensaje")
public class Mensaje extends ParseObject {

    private static final String MENSAJE = "Mensaje";
    private static final String NOMBRE = "Nombre";
    private static final String FOTOPERFIL = "FotoPerfil";
    private static final String TYPE_MENSAJE = "Tipo";
    private static final String HORA = "Hora";

    public void guardar(String mensaje, String nombre, String fotoPerfil, String type_mensaje, String hora){
        put("Mensaje", mensaje);
        put("Nombre", nombre);
        put("FotoPerfil", fotoPerfil);
        put("Tipo", type_mensaje);
        put("Hora", hora);
    }

    public String getMensaje() {
        return getString(MENSAJE);
    }

    public void setMensaje(String mensaje) {
        put(MENSAJE, mensaje);
    }

    public String getNombre() {
        return getString(NOMBRE);
    }

    public void setNombre(String nombre) {
        put(NOMBRE, nombre);
    }

    public String getFotoPerfil() {
        return getString(FOTOPERFIL);
    }

    public void setFotoPerfil(String fotoPerfil) {
        put(FOTOPERFIL, fotoPerfil);
    }

    public String getType_mensaje() {
        return getString(TYPE_MENSAJE);
    }

    public void setType_mensaje(String type_mensaje) {
        put(TYPE_MENSAJE, type_mensaje);
    }

    public String getHora() {
        return getString(HORA);
    }

    public void setHora(String hora) {
        put(HORA, hora);
    }
}