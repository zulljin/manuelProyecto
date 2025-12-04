package com.example.arefi.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.arefi.DBHelper;
import com.example.arefi.entity.Tarea;

import java.util.ArrayList;
import java.util.List;

public class TareaRepositoryImpl implements TareaRepository {

    private final DBHelper dbHelper;

    public TareaRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public long crearTarea(Tarea tarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombreTarea", tarea.getNombreTarea());
        valores.put("descripcion", tarea.getDescripcion());
        return db.insert("Tarea", null, valores);
    }

    @Override
    public Tarea obtenerTareaPorId(int idTarea) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idTarea, nombreTarea, descripcion FROM Tarea WHERE idTarea=?",
                new String[]{String.valueOf(idTarea)}
        );

        if (cursor.moveToFirst()) {
            Tarea tarea = new Tarea(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            );
            cursor.close();
            return tarea;
        }

        cursor.close();
        return null;
    }

    @Override
    public List<Tarea> obtenerTodasLasTareas() {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idTarea, nombreTarea, descripcion FROM Tarea ORDER BY nombreTarea ASC",
                null
        );

        while (cursor.moveToNext()) {
            lista.add(new Tarea(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        }

        cursor.close();
        return lista;
    }

}

