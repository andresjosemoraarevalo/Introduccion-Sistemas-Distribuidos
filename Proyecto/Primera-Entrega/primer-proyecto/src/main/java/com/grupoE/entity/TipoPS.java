package com.grupoE;

public enum TipoPS {
    
    Devolver(1),
    Renovar(2),
    Solicitar(3);
    
    private int numSolicitud; 

    private TipoPS(int numSolicitud) {
        this.numSolicitud = numSolicitud;
    }
    public int getNumSolicitud() {
        return this.numSolicitud;
    }
}