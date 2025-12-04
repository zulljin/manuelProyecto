package com.example.arefi.repository;

import com.example.arefi.entity.LocalizacionTarea;
import java.util.List;

public interface LocalizacionTareaRepository {
    long asignarTareaALocalizacion(LocalizacionTarea locTarea);
    List<LocalizacionTarea> obtenerTareasPorLocalizacion(int idLocalizacion);
}
