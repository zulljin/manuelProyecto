package com.example.arefi.entity;

public class Empleado {

    private int idEmpleado;
    private String nombre;
    private String dni;


    public Empleado(int idEmpleado, String nombre, String dni, String uidNfc) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.dni = dni;
    }

    // Constructor sin ID (para insertar)
    public Empleado(String nombre, String dni, String uidNfc) {
        this.nombre = nombre;
        this.dni = dni;
    }

    // ===== GETTERS =====
    public int getIdEmpleado() { return idEmpleado; }
    public String getNombre() { return nombre; }
    public String getDni() { return dni; }

    // ===== SETTERS =====
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDni(String dni) { this.dni = dni; }
}
