package com.example.arefi.repository;

import java.util.List;

public interface TareaRealizadaRepository {
    void guardarTareaRealizada(int idVisita, int idLocTarea);
    List<String> obtenerTareasRealizadasConNombres(int idVisita);
    List<String> obtenerTareasUnicasDeAreaEnFichaje(int idFichaje, int idArea);
}