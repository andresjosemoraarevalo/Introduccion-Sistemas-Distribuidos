package com.grupoE.entity;

public class Peticion {
    
    private TipoPeticion tipo;
    private int idLibro;

    /**
     * @return TipoPeticion return the tipo
     */
    public TipoPeticion getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(TipoPeticion tipo) {
        this.tipo = tipo;
    }

    /**
     * @return int return the idLibro
     */
    public int getIdLibro() {
        return idLibro;
    }

    /**
     * @param idLibro the idLibro to set
     */
    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    @Override
    public String toString(){
        return "ID: " + this.idLibro + "\t Tipo: " + this.tipo + "\n"; 
    }

}
