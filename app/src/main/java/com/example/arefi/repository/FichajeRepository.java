package com.example.arefi.repository;

import com.example.arefi.entity.Fichaje;
import java.util.List;

public interface FichajeRepository {

    long crearFichaje(int idEmpleado);

    Fichaje obtenerFichajeActivo(int idEmpleado);

    void finalizarFichaje(int idFichaje);

    List<Fichaje> obtenerFichajesHoy(int idEmpleado);

    int calcularHorasTrabajadasHoy(int idEmpleado);

    int calcularHorasTrabajadasMes(int idEmpleado);
}
