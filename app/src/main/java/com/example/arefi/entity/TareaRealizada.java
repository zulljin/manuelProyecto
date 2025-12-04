package com.example.arefi.entity;

public class TareaRealizada {
    private int idTareaRealizada;
    private int idVisita;
    private int idLocTarea;
    private long horaRegistro;

    public TareaRealizada() {}

    public TareaRealizada(int idTareaRealizada, int idVisita, int idLocTarea, long horaRegistro) {
        this.idTareaRealizada = idTareaRealizada;
        this.idVisita = idVisita;
        this.idLocTarea = idLocTarea;
        this.horaRegistro = horaRegistro;
    }

    public int getIdTareaRealizada() {
        return idTareaRealizada;
    }

    public void setIdTareaRealizada(int idTareaRealizada) {
        this.idTareaRealizada = idTareaRealizada;
    }

    public int getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(int idVisita) {
        this.idVisita = idVisita;
    }

    public int getIdLocTarea() {
        return idLocTarea;
    }

    public void setIdLocTarea(int idLocTarea) {
        this.idLocTarea = idLocTarea;
    }

    public long getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(long horaRegistro) {
        this.horaRegistro = horaRegistro;
    }
}