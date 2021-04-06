package com.grupoE.entity;

public enum TipoPeticion {
    
    Devolver(1),
    Renovar(2),
    Solicitar(3);
    
    private int numSolicitud; 
    
    private TipoPeticion(int numSolicitud) {
        this.numSolicitud = numSolicitud;
    }

    public int getNumSolicitud() {
        return this.numSolicitud;
    }
}