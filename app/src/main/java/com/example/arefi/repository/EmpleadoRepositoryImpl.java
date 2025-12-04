
package com.example.arefi.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.arefi.DBHelper;
import com.example.arefi.entity.Empleado;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoRepositoryImpl implements EmpleadoRepository {

    private final DBHelper dbHelper;

    public EmpleadoRepositoryImpl(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    @Override
    public long insertarEmpleado(Empleado empleado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", empleado.getNombre());
        valores.put("dni", empleado.getDni());

        return db.insert("Empleado", null, valores);
    }

    @Override
    public Empleado obtenerEmpleadoPorDni(String dni) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idEmpleado, nombre, dni, uid_nfc FROM Empleado WHERE dni=?",
                new String[]{dni}
        );

        if (cursor.moveToFirst()) {
            Empleado e = new Empleado(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            cursor.close();
            return e;
        }

        cursor.close();
        return null;
    }

    @Override
    public int obtenerIdEmpleadoPorDni(String dni) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idEmpleado FROM Empleado WHERE dni=?",
                new String[]{dni}
        );

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    @Override
    public boolean existeDni(String dni) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Empleado WHERE dni=?",
                new String[]{dni}
        );

        boolean existe = false;
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0;
        }
        cursor.close();
        return existe;
    }

    @Override
    public Empleado obtenerEmpleadoPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT idEmpleado, nombre, dni, uid_nfc FROM Empleado WHERE idEmpleado=?",
                new String[]{String.valueOf(id)}
        );

        if (cursor.moveToFirst()) {
            Empleado e = new Empleado(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            cursor.close();
            return e;
        }

        cursor.close();
        return null;
    }

    @Override
    public List<Empleado> obtenerTodosLosEmpleados() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Empleado> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT idEmpleado, nombre, dni, uid_nfc FROM Empleado ORDER BY nombre ASC",
                null
        );

        while (cursor.moveToNext()) {
            lista.add(new Empleado(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }

        cursor.close();
        return lista;
    }
}
