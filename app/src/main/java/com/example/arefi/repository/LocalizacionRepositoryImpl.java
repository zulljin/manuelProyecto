package com.example.arefi.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.example.arefi.DBHelper;

public class LocalizacionRepositoryImpl implements LocalizacionRepository {

    private final DBHelper dbHelper;

    public LocalizacionRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public int detectarZonaDeTrabajo(double latActual, double lonActual) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idLocalizacion AS _id, latitud, longitud, radio " +
                        "FROM Localizacion WHERE tipo='ZONA'", null
        );

        int zonaDetectada = -1;
        boolean encontrado = false;

        while (cursor.moveToNext() && !encontrado) {
            int idZona = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            double latZona = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
            double lonZona = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));
            int radio = cursor.getInt(cursor.getColumnIndexOrThrow("radio"));

            float[] resultados = new float[1];
            Location.distanceBetween(latActual, lonActual, latZona, lonZona, resultados);
            float distancia = resultados[0];

            if (distancia <= radio) {
                zonaDetectada = idZona;
                encontrado = true;
            }
        }
        cursor.close();
        return zonaDetectada;
    }

    @Override
    public int detectarArea(double latActual, double lonActual) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idLocalizacion AS _id, latitud, longitud, radio " +
                        "FROM Localizacion WHERE tipo='AREA'", null
        );

        int areaDetectada = -1;
        float distanciaMinima = Float.MAX_VALUE;

        while (cursor.moveToNext()) {
            int idArea = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            double latArea = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"));
            double lonArea = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"));
            int radio = cursor.getInt(cursor.getColumnIndexOrThrow("radio"));

            float[] resultados = new float[1];
            Location.distanceBetween(latActual, lonActual, latArea, lonArea, resultados);
            float distancia = resultados[0];

            if (distancia <= radio && distancia < distanciaMinima) {
                distanciaMinima = distancia;
                areaDetectada = idArea;
            }
        }
        cursor.close();
        return areaDetectada;
    }


    @Override
    public String obtenerNombreLocalizacion(int idLocalizacion) {
        if (idLocalizacion == -1) return "Ubicación desconocida";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre FROM Localizacion WHERE idLocalizacion=?",
                new String[]{String.valueOf(idLocalizacion)}
        );

        String nombre = "Desconocida";
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0);
        }

        cursor.close();
        return nombre;
    }

    @Override
    public String obtenerNombreSegunVisita(int idVisita) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT l.nombre " +
                        "FROM Localizacion l " +
                        "INNER JOIN Visita v ON v.idLocalizacion = l.idLocalizacion " +
                        "WHERE v.idVisita = ?",
                new String[]{String.valueOf(idVisita)}
        );

        String nombre = "Fuera de las áreas";
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0);
        }

        cursor.close();
        return nombre;
    }
}
