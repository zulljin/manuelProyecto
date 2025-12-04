package com.example.arefi.repository;

public interface LocalizacionRepository {

    int detectarZonaDeTrabajo(double lat, double lon);
    int detectarArea(double lat, double lon);
    String obtenerNombreLocalizacion(int idLocalizacion);


    String obtenerNombreSegunVisita(int idVisita);
}
