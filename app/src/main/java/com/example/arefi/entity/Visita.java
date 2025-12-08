package com.example.arefi.entity;

public class Visita {

    private int idVisita;
    private int idFichaje;
    private int idLocalizacion;
    private long horaEntrada;
    private Long horaSalida; // Puede ser null si la visita sigue activa
    private Integer duracionMinutos; // Puede ser null si no se ha finalizado


    public Visita(int idVisita, int idFichaje, int idLocalizacion, long horaEntrada, Long horaSalida, Integer duracionMinutos ) {
        this.idVisita = idVisita;
        this.idFichaje = idFichaje;
        this.idLocalizacion = idLocalizacion;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.duracionMinutos = duracionMinutos;
    }

    public Visita(int idVisita,int idLocalizacion, long horaEntrada, Long horaSalida, Integer duracionMinutos) {
        this.idVisita = idVisita;
        this.idLocalizacion = idLocalizacion;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.duracionMinutos = duracionMinutos;
    }
    public Visita(int idVisita, int idLocalizacion, long horaEntrada, Integer duracionMinutos) {
        this.idVisita = idVisita;
        this.idLocalizacion = idLocalizacion;
        this.horaEntrada = horaEntrada;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y setters
    public int getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(int idVisita) {
        this.idVisita = idVisita;
    }

    public int getIdFichaje() {
        return idFichaje;
    }

    public void setIdFichaje(int idFichaje) {
        this.idFichaje = idFichaje;
    }

    public int getIdLocalizacion() {
        return idLocalizacion;
    }

    public void setIdLocalizacion(int idLocalizacion) {
        this.idLocalizacion = idLocalizacion;
    }

    public long getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(long horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public Long getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Long horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
