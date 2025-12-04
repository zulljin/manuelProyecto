package com.example.arefi.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.arefi.DBHelper;
import com.example.arefi.entity.LocalizacionTarea;
import java.util.ArrayList;
import java.util.List;

public class LocalizacionTareaRepositoryImpl implements LocalizacionTareaRepository {

    private final DBHelper dbHelper;

    public LocalizacionTareaRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public long asignarTareaALocalizacion(LocalizacionTarea locTarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("idLocalizacion", locTarea.getIdLocalizacion());
        valores.put("idTarea", locTarea.getIdTarea());
        return db.insert("LocalizacionTarea", null, valores);
    }

    @Override
    public List<LocalizacionTarea> obtenerTareasPorLocalizacion(int idLocalizacion) {
        List<LocalizacionTarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idLocTarea, idLocalizacion, idTarea FROM LocalizacionTarea WHERE idLocalizacion=?",
                new String[]{String.valueOf(idLocalizacion)}
        );

        while (cursor.moveToNext()) {
            lista.add(new LocalizacionTarea(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2)
            ));
        }

        cursor.close();
        return lista;
    }
}
