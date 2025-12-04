package com.example.arefi.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.arefi.DBHelper;
import com.example.arefi.entity.Fichaje;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FichajeRepositoryImpl implements FichajeRepository {

    private final DBHelper dbHelper;

    public FichajeRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public long crearFichaje(int idEmpleado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("idEmpleado", idEmpleado);
        valores.put("horaEntrada", System.currentTimeMillis());
        valores.put("estado", "EN_CURSO");

        long resultado = db.insert("Fichaje", null, valores);
        Log.d("FichajeRepo", "Fichaje creado ID: " + resultado + " para empleado: " + idEmpleado);
        return resultado;
    }

    @Override
    public Fichaje obtenerFichajeActivo(int idEmpleado) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idFichaje, idEmpleado, horaEntrada, horaSalida, estado " +
                        "FROM Fichaje WHERE idEmpleado=? AND estado='EN_CURSO'",
                new String[]{String.valueOf(idEmpleado)}
        );

        Fichaje fichaje = null;
        if (cursor.moveToFirst()) {
            fichaje = new Fichaje(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getLong(2),
                    cursor.getLong(3),
                    cursor.getString(4)
            );
        }
        cursor.close();
        return fichaje;
    }

    @Override
    public void finalizarFichaje(int idFichaje) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("horaSalida", System.currentTimeMillis());
        valores.put("estado", "FINALIZADO");

        db.update("Fichaje", valores, "idFichaje=?", new String[]{String.valueOf(idFichaje)});
        Log.d("FichajeRepo", "Fichaje finalizado ID: " + idFichaje);
    }

    @Override
    public List<Fichaje> obtenerFichajesHoy(int idEmpleado) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT idFichaje, idEmpleado, horaEntrada, horaSalida, estado " +
                        "FROM Fichaje " +
                        "WHERE idEmpleado=? AND strftime('%Y%m%d', horaEntrada/1000, 'unixepoch')=? " +
                        "ORDER BY horaEntrada ASC",
                new String[]{String.valueOf(idEmpleado), fechaHoy}
        );

        List<Fichaje> lista = new ArrayList<>();
        while (cursor.moveToNext()) {
            lista.add(new Fichaje(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getLong(2),
                    cursor.getLong(3),
                    cursor.getString(4)
            ));
        }
        cursor.close();
        return lista;
    }

    @Override
    public int calcularHorasTrabajadasHoy(int idEmpleado) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT horaEntrada, horaSalida " +
                        "FROM Fichaje " +
                        "WHERE idEmpleado=? AND strftime('%Y%m%d', horaEntrada/1000, 'unixepoch')=? AND estado='FINALIZADO'",
                new String[]{String.valueOf(idEmpleado), fechaHoy}
        );

        int minutosTotal = 0;
        while (cursor.moveToNext()) {
            long entrada = cursor.getLong(0);
            long salida = cursor.getLong(1);
            minutosTotal += (int) ((salida - entrada) / 60000);
        }

        cursor.close();
        Log.d("FichajeRepo", "Total minutos hoy: " + minutosTotal);
        return minutosTotal;
    }

    @Override
    public int calcularHorasTrabajadasMes(int idEmpleado) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mesActual = new SimpleDateFormat("yyyyMM").format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT horaEntrada, horaSalida " +
                        "FROM Fichaje " +
                        "WHERE idEmpleado=? AND strftime('%Y%m', horaEntrada/1000, 'unixepoch')=? AND estado='FINALIZADO'",
                new String[]{String.valueOf(idEmpleado), mesActual}
        );

        int minutosTotal = 0;
        while (cursor.moveToNext()) {
            long entrada = cursor.getLong(0);
            long salida = cursor.getLong(1);
            minutosTotal += (int) ((salida - entrada) / 60000);
        }

        cursor.close();
        Log.d("FichajeRepo", "Total minutos mes: " + minutosTotal);
        return minutosTotal;
    }
}
