package com.example.arefi.entity;

/**
 * Clase que representa un fichaje.
 *
 * Las columnas horaEntrada y horaSalida se guardan en SQLite como INTEGER (epoch millis),
 * porque SQLite no tiene un tipo datetime real.
 *
 * En Java:
 *   - horaEntrada se guarda como 'long' porque siempre tiene valor (nunca null).
 *   - horaSalida se guarda como 'Long' (objeto) porque puede ser null hasta que el empleado
 *     finalice el fichaje.
 */
public class Fichaje {

    private int idFichaje;
    private int idEmpleado;
    private long horaEntrada;   // hora de entrada en epoch millis, siempre tiene valor
    private Long horaSalida;    // hora de salida en epoch millis, puede ser null
    private String estado;      // "EN_CURSO" o "FINALIZADO"

    // Constructor vacío
    public Fichaje() {}

    // Constructor con todos los campos
    public Fichaje(int idFichaje, int idEmpleado, long horaEntrada, Long horaSalida, String estado) {
        this.idFichaje = idFichaje;
        this.idEmpleado = idEmpleado;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdFichaje() { return idFichaje; }
    public void setIdFichaje(int idFichaje) { this.idFichaje = idFichaje; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public long getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(long horaEntrada) { this.horaEntrada = horaEntrada; }

    public Long getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Long horaSalida) { this.horaSalida = horaSalida; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
