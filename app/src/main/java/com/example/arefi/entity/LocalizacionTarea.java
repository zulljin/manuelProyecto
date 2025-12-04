package com.example.arefi.entity;


public class LocalizacionTarea {
    private int idLocTarea;
    private int idLocalizacion;
    private int idTarea;

    public LocalizacionTarea(int idLocTarea, int idLocalizacion, int idTarea) {
        this.idLocTarea = idLocTarea;
        this.idLocalizacion = idLocalizacion;
        this.idTarea = idTarea;
    }

    // Getters y setters
    public int getIdLocTarea() { return idLocTarea; }
    public void setIdLocTarea(int idLocTarea) { this.idLocTarea = idLocTarea; }

    public int getIdLocalizacion() { return idLocalizacion; }
    public void setIdLocalizacion(int idLocalizacion) { this.idLocalizacion = idLocalizacion; }

    public int getIdTarea() { return idTarea; }
    public void setIdTarea(int idTarea) { this.idTarea = idTarea; }
}
