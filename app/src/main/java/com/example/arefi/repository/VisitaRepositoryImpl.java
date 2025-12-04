package com.example.arefi.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.arefi.DBHelper;
import com.example.arefi.entity.Visita;

import java.util.ArrayList;
import java.util.List;

public class VisitaRepositoryImpl implements VisitaRepository {

    private final DBHelper dbHelper;

    public VisitaRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public Visita obtenerVisitaActiva(int idFichaje) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idVisita, idLocalizacion, horaEntrada " +
                        "FROM Visita " +
                        "WHERE idFichaje=? AND horaSalida IS NULL",
                new String[]{String.valueOf(idFichaje)}
        );

        Visita visita = null;
        if (cursor.moveToFirst()) {
            visita = new Visita(
                    cursor.getInt(0), // idVisita
                    cursor.getInt(1), // idLocalizacion
                    cursor.getLong(2), // horaEntrada
                    null // horaSalida sigue siendo null porque está activa
            );
        }

        cursor.close();
        return visita;
    }

    @Override
    public List<Visita> obtenerVisitasPorFichaje(int idFichaje) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT v.idVisita AS _id, v.idLocalizacion, v.horaEntrada, v.duracionMinutos " +
                        "FROM Visita v " +
                        "WHERE v.idFichaje=?",
                new String[]{String.valueOf(idFichaje)}
        );

        List<Visita> lista = new ArrayList<>();
        while (cursor.moveToNext()) {
            lista.add(new Visita(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getLong(2),
                    cursor.getLong(3)
            ));
        }
        cursor.close();
        return lista;
    }

    @Override
    public long crearVisita(int idFichaje, int idLocalizacion, int duracionMinutos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("idFichaje", idFichaje);
        valores.put("idLocalizacion", idLocalizacion);
        valores.put("horaEntrada", System.currentTimeMillis());
        valores.put("duracionMinutos", duracionMinutos);

        long resultado = db.insert("Visita", null, valores);
        Log.d("VisitaRepository", "Visita creada ID: " + resultado);
        return resultado;
    }

    @Override
    public void finalizarVisita(int idVisita) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Obtener horaEntrada para calcular duración
        Cursor cursor = db.rawQuery(
                "SELECT horaEntrada FROM Visita WHERE idVisita=?",
                new String[]{String.valueOf(idVisita)}
        );

        if (cursor.moveToFirst()) {
            long horaEntrada = cursor.getLong(0);
            long ahora = System.currentTimeMillis();
            int duracionMinutos = (int) ((ahora - horaEntrada) / 60000);

            ContentValues valores = new ContentValues();
            valores.put("horaSalida", ahora);
            valores.put("duracionMinutos", duracionMinutos);

            db.update("Visita", valores, "idVisita=?", new String[]{String.valueOf(idVisita)});

            Log.d("VisitaRepository", "Visita finalizada ID: " + idVisita + " (duración: " + duracionMinutos + " min)");
        }

        cursor.close();
    }
   /* @Override
    public List<LocalizacionTarea> obtenerAreasAgrupadasPorFichajeConTiempoTotal(int idFichaje) {
        List<LocalizacionTarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT l.idLocalizacion AS _id, " +
                        "l.nombre AS nombreArea, " +
                        "SUM(v.duracionMinutos) AS tiempoTotalArea " +
                        "FROM Visita v " +
                        "INNER JOIN Localizacion l ON v.idLocalizacion = l.idLocalizacion " +
                        "WHERE v.idFichaje = ? " +
                        "GROUP BY l.idLocalizacion " +
                        "ORDER BY tiempoTotalArea DESC",
                new String[]{String.valueOf(idFichaje)});

        while (cursor.moveToNext()) {
            int idLocalizacion = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String nombreArea = cursor.getString(cursor.getColumnIndexOrThrow("nombreArea"));
            int tiempoTotalArea = cursor.getInt(cursor.getColumnIndexOrThrow("tiempoTotalArea"));

            lista.add(new LocalizacionTarea(idLocalizacion, nombreArea, tiempoTotalArea));
        }

        cursor.close();
        return lista;
    }*/
   @Override
   public Cursor obtenerAreasAgrupadasPorFichajeConTiempoTotal(int idFichaje) {
       SQLiteDatabase db = dbHelper.getReadableDatabase();

       return db.rawQuery(
               "SELECT l.idLocalizacion AS _id, " +
                       "l.nombre AS nombreArea, " +
                       "SUM(v.duracionMinutos) AS tiempoTotalArea " +
                       "FROM Visita v " +
                       "INNER JOIN Localizacion l ON v.idLocalizacion = l.idLocalizacion " +
                       "WHERE v.idFichaje = ? " +
                       "GROUP BY l.idLocalizacion " +
                       "ORDER BY tiempoTotalArea DESC",
               new String[]{String.valueOf(idFichaje)}
       );
   }

}
