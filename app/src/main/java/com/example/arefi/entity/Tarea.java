package com.example.arefi.entity;


public class Tarea {
    private int idTarea;
    private String nombreTarea;
    private String descripcion;

    public Tarea(int idTarea, String nombreTarea, String descripcion) {
        this.idTarea = idTarea;
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public int getIdTarea() { return idTarea; }
    public void setIdTarea(int idTarea) { this.idTarea = idTarea; }

    public String getNombreTarea() { return nombreTarea; }
    public void setNombreTarea(String nombreTarea) { this.nombreTarea = nombreTarea; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
