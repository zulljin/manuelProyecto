package com.example.arefi.entity;

public class Localizacion {
    private int idLocalizacion;
    private String nombre;
    private double latitud;
    private double longitud;
    private int radio;
    private String tipo; // "ZONA" o "AREA"
    private Integer idZonaPadre; // null si es zona

    public Localizacion(int idLocalizacion, String nombre, double latitud, double longitud,
                        int radio, String tipo, Integer idZonaPadre) {
        this.idLocalizacion = idLocalizacion;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.radio = radio;
        this.tipo = tipo;
        this.idZonaPadre = idZonaPadre;
    }

    // Getters y setters
    public int getIdLocalizacion() { return idLocalizacion; }
    public String getNombre() { return nombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public int getRadio() { return radio; }
    public String getTipo() { return tipo; }
    public Integer getIdZonaPadre() { return idZonaPadre; } //puede ser null

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setRadio(int radio) { this.radio = radio; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setIdZonaPadre(Integer idZonaPadre) { this.idZonaPadre = idZonaPadre; }
}
