package com.example.arefi.repository;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.arefi.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class TareaRealizadaRepositoryImpl implements TareaRealizadaRepository {

    private DBHelper dbHelper;

    public TareaRealizadaRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public void guardarTareaRealizada(int idVisita, int idLocTarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("idVisita", idVisita);
        valores.put("idLocTarea", idLocTarea);
        valores.put("horaRegistro", System.currentTimeMillis());

        db.insert("TareaRealizada", null, valores);
        Log.d("TareaRealizadaRepo", "Tarea realizada guardada: visita=" + idVisita + ", idLocTarea=" + idLocTarea);
    }

    @Override
    public List<String> obtenerTareasRealizadasConNombres(int idVisita) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT t.nombreTarea " +
                        "FROM TareaRealizada tr " +
                        "INNER JOIN LocalizacionTarea lt ON tr.idLocTarea = lt.idLocTarea " +
                        "INNER JOIN Tarea t ON lt.idTarea = t.idTarea " +
                        "WHERE tr.idVisita=?",
                new String[]{String.valueOf(idVisita)}
        );

        List<String> tareas = new ArrayList<>();
        while (cursor.moveToNext()) {
            tareas.add(cursor.getString(0));
        }
        cursor.close();
        return tareas;
    }

    @Override
    public List<String> obtenerTareasUnicasDeAreaEnFichaje(int idFichaje, int idArea) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT t.nombreTarea " +
                        "FROM TareaRealizada tr " +
                        "INNER JOIN Visita v ON tr.idVisita = v.idVisita " +
                        "INNER JOIN LocalizacionTarea lt ON tr.idLocTarea = lt.idLocTarea " +
                        "INNER JOIN Tarea t ON lt.idTarea = t.idTarea " +
                        "WHERE v.idFichaje = ? AND v.idLocalizacion = ? " +
                        "ORDER BY t.nombreTarea",
                new String[]{String.valueOf(idFichaje), String.valueOf(idArea)}
        );

        List<String> tareas = new ArrayList<>();
        while (cursor.moveToNext()) {
            tareas.add(cursor.getString(0));
        }
        cursor.close();
        return tareas;
    }
}