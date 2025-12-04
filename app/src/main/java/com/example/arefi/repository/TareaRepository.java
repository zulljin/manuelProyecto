package com.example.arefi.repository;

import com.example.arefi.entity.Tarea;
import java.util.List;

public interface TareaRepository {
    long crearTarea(Tarea tarea);
    Tarea obtenerTareaPorId(int idTarea);
    List<Tarea> obtenerTodasLasTareas();
}
