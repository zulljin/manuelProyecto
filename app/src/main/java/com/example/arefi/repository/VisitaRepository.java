package com.example.arefi.repository;

import android.database.Cursor;

import com.example.arefi.entity.Visita;

import java.util.List;

public interface VisitaRepository {
    Visita obtenerVisitaActiva(int idFichaje);
    List<Visita> obtenerVisitasPorFichaje(int idFichaje);
    long crearVisita(int idFichaje, int idLocalizacion, int duracionMinutos);
    void finalizarVisita(int idVisita);
    //List<LocalizacionTarea> obtenerAreasAgrupadasPorFichajeConTiempoTotal(int idFichaje);
    Cursor obtenerAreasAgrupadasPorFichajeConTiempoTotal(int idFichaje);
}
