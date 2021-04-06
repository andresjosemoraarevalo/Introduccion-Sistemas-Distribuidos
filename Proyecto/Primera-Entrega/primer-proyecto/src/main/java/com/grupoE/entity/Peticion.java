package com.grupoE.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Peticion {
    
    private TipoPeticion tipo;
    private int idLibro;
    private LocalDate fecha;

    public Peticion(){
        this.fecha = LocalDate.now();
    }
    public Peticion(int id, int tipo, String fecha){
        this.idLibro = id;
        this.tipo = this.buscarPeticion(tipo);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
        this.fecha = LocalDate.parse(fecha, dateFormat);
    }
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

    /**
     * Función para ver la clase libro de manera más estética
     */
    @Override
    public String toString(){
        return "ID: " + this.idLibro + "\t Tipo: " + this.tipo + "\n"; 
    }

    /**
     * Función que dado el tipo de pertición en numero, retorna su valor de Enumerado
     * @param valor
     * @return enum TipoPeticion y null si no corr
     */
    public TipoPeticion buscarPeticion(int valor){
        switch(valor){
            case 1:
                return TipoPeticion.Devolver;
            case 2:
                return TipoPeticion.Renovar;
            case 3:
                return TipoPeticion.Solicitar;
            default:
                return null;
        }
    }

    /**
     * @return LocalDate return the fecha
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

}
