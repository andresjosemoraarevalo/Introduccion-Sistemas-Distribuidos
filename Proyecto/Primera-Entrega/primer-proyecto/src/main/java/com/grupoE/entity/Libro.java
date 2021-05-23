package com.grupoE.entity;

import java.time.*;

public class Libro {
    private int idLibro;
    private String titulo;
    private String autor;
    private long ISBN;
    private String editorial;
    private LocalDate fechaPublicacion;
    private String idioma;
    private String categoria;
    private boolean estado;
    private int numEjemplares;
    
    /**
     * @return String return the titulo
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * @param titulo the titulo to set
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * @return String return the autor
     */
    public String getAutor() {
        return autor;
    }

    /**
     * @param autor the autor to 
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /**
     * @return long return the ISBN
     */
    public long getISBN() {
        return ISBN;
    }

    /**
     * @param ISBN the ISBN to set
     */
    public void setISBN(long ISBN) {
        this.ISBN = ISBN;
    }

    @Override
    public String toString(){
        return "Titulo: "+ this.titulo + "\t Autor: "+this.autor + "\t ISBN: " + this.ISBN + "\n";
    }

    /**
     * @return LocalDate return the fechaPublicacion
     */
    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    /**
     * @param fechaPublicacion the fechaPublicacion to set
     */
    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }


    /**
     * @return String return the editorial
     */
    public String getEditorial() {
        return editorial;
    }

    /**
     * @param editorial the editorial to set
     */
    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    /**
     * @return String return the idioma
     */
    public String getIdioma() {
        return idioma;
    }

    /**
     * @param idioma the idioma to set
     */
    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    /**
     * @return String return the categoria
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * @param categoria the catogoria to set
     */
    public void setCatogoria(String categoria) {
        this.categoria = categoria;
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
     * @return boolean return the estado
     */
    public boolean isEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(boolean estado) {
        this.estado = estado;
    }


    /**
     * @param categoria the categoria to set
     */
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    /**
     * @return int return the numEjemplares
     */
    public int getNumEjemplares() {
        return numEjemplares;
    }

    /**
     * @param numEjemplares the numEjemplares to set
     */
    public void setNumEjemplares(int numEjemplares) {
        this.numEjemplares = numEjemplares;
    }

}
