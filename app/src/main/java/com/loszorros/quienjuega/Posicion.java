package com.loszorros.quienjuega;

public class Posicion {
    private double latitud;
    private double longitud;

    public Posicion() {
        // Constructor vacío requerido por Firebase
    }

    public Posicion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
