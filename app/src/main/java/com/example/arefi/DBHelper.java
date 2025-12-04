package com.example.arefi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BASE_DATOS = "fichajes.db";
    private static final int VERSION_BASE_DATOS = 21;

    public DBHelper(Context contexto) {
        super(contexto, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE Empleado (" +
                "idEmpleado INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "uid_nfc TEXT UNIQUE, " +  //para cuando en un futuro le añadamos el nfc
                "dni TEXT UNIQUE NOT NULL)");


        db.execSQL("CREATE TABLE Localizacion (" +
                "idLocalizacion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "radio INTEGER DEFAULT 50,  "+
                "tipo TEXT DEFAULT 'AREA', " +  //puede ser zona (para el lugar general del trabajo) o area (lugar concreto dentro del trabajo)
                "idZonaPadre INTEGER DEFAULT NULL, " +
                "FOREIGN KEY(idZonaPadre) REFERENCES Localizacion(idLocalizacion))");  // para hacer la subcategoria hacemos referencia a la misma tabla


        db.execSQL("CREATE TABLE Tarea (" +
                "idTarea INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombreTarea TEXT NOT NULL, " +
                "descripcion TEXT)");

        // tabla para relacionar tareas con localizaciones
        db.execSQL("CREATE TABLE LocalizacionTarea (" +
                "idLocTarea INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idLocalizacion INTEGER NOT NULL, " +
                "idTarea INTEGER NOT NULL, " +
                "FOREIGN KEY(idLocalizacion) REFERENCES Localizacion(idLocalizacion), " +
                "FOREIGN KEY(idTarea) REFERENCES Tarea(idTarea))");

        // fichaje global diario
        db.execSQL("CREATE TABLE Fichaje (" +
                "idFichaje INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idEmpleado INTEGER NOT NULL, " +
                "horaEntrada INTEGER NOT NULL, " +  // sqlite aceptaría datetime pero lo cambiaría a integer, asi que usamos int y hacemos un epoch milis
                "horaSalida INTEGER, " +
                "estado TEXT DEFAULT 'EN_CURSO', " +
                "FOREIGN KEY(idEmpleado) REFERENCES Empleado(idEmpleado))");

     //tabla que une fichaje y localizacion para registrar el tiempo "real" trabajado
        db.execSQL("CREATE TABLE Visita (" +
                "idVisita INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idFichaje INTEGER NOT NULL, " +
                "idLocalizacion INTEGER NOT NULL, " +
                "horaEntrada INTEGER NOT NULL, " +
                "horaSalida INTEGER, " +
                "duracionMinutos INTEGER DEFAULT 0, " +
                "FOREIGN KEY(idFichaje) REFERENCES Fichaje(idFichaje), " +
                "FOREIGN KEY(idLocalizacion) REFERENCES Localizacion(idLocalizacion))");

        // guarda las tareas que se han realizado en las diferentes visitas
        db.execSQL("CREATE TABLE TareaRealizada (" +
                "idTareaRealizada INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idVisita INTEGER NOT NULL, " +
                "idLocTarea INTEGER NOT NULL, " +
                "horaRegistro INTEGER NOT NULL, " +
                "FOREIGN KEY(idVisita) REFERENCES Visita(idVisita), " +
                "FOREIGN KEY(idLocTarea) REFERENCES LocalizacionTarea(idLocTarea))");

        Log.d("DBHelper", "Base de datos creada correctamente");

        insertarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAntigua, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS TareaRealizada");
        db.execSQL("DROP TABLE IF EXISTS Visita");
        db.execSQL("DROP TABLE IF EXISTS Fichaje");
        db.execSQL("DROP TABLE IF EXISTS LocalizacionTarea");
        db.execSQL("DROP TABLE IF EXISTS Tarea");
        db.execSQL("DROP TABLE IF EXISTS Localizacion");
        db.execSQL("DROP TABLE IF EXISTS Empleado");
        onCreate(db);
    }

    // ----------------------------- DATOS INICIALES -----------------------------------

    private void insertarDatosIniciales(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // ===== EMPLEADOS =====
            insertarEmpleado(db, "Manuel de Larrea", "23308037K");
            insertarEmpleado(db, "Anna Lisa", "12345678A");
            insertarEmpleado(db, "Sergio García", "87654321B");
            insertarEmpleado(db, "Ana Martínez", "11223344C");

            // LOCALIZACIONES

            // ===== ZONAS DE TRABAJO =====
            long zonaAlmeria = insertarLocalizacion(db, "Almería", 36.8443, -2.4657, 1000, "ZONA", null);
            long zonaMurcia = insertarLocalizacion(db, "Murcia", 38.012621, -1.034319, 300, "ZONA", null);
            long zonaAlmoradi = insertarLocalizacion(db, "Almoradi", 38.1097287, -0.793078, 1000, "ZONA", null);

            // ===== ÁREAS DE ALMERÍA ===== 36.844265, -2.465676 36.84494,-2.4594991
            long casaId = insertarLocalizacion(db, "Casa", 36.844265, -2.465676, 20, "AREA",  (int) zonaAlmeria);
            long invernaderoId = insertarLocalizacion(db, "Invernadero", 36.8440751, -2.4646237,30, "AREA", (int) zonaAlmeria);
            long semilleroId = insertarLocalizacion(db, "Semillero", 36.8446964, -2.4642306, 50, "AREA", (int) zonaAlmeria);
            long casaSergioId = insertarLocalizacion(db, "Casa Sergio", 36.844617, -2.458773, 50, "AREA", (int) zonaAlmeria);
            long casaPedro = insertarLocalizacion(db,"Casa Pedro", 38.109838, -0.7928808, 800, "AREA", (int) zonaAlmoradi);

            // ===== ÁREAS DE MURCIA =====
            long instiId = insertarLocalizacion(db, "Instituto", 38.0127969, -1.0342223, 60, "AREA", (int) zonaMurcia);

            // ===== TAREAS GENERALES =====
            long tarea1 = insertarTarea( db, "Limpieza", "Limpieza general");
            long tarea2 =insertarTarea(db, "Supervisión", "Repaso por la plantación para revisar que esté todo bien");

            // ===== TAREAS DE INVERNADERO =====
            long tarea3 = insertarTarea(db, "Recolecta", "Ya sea de tomate, pimiento, lechuga...");
            long tarea4 = insertarTarea(db, "Deshoje", "Quitar hoja y tallos");
            long tarea5 = insertarTarea(db, "Instalación goteros", "Cambio de goteros o puesta de unos nuevos");
            long tarea6 = insertarTarea(db, "Quitar mala hierba", "Tanto en la plantación como alrededores");
            long tarea7 = insertarTarea(db, "Revisar goteros", "Cambiar o limpiar si no funcionan bien");
            long tarea8 = insertarTarea(db, "Revisar aspersores", "En funcionamiento");
            long tarea9 = insertarTarea(db, "Transplante", "De semillero a invernadero");
            long tarea10 = insertarTarea(db, "Toma de muestras", "Puede ser de hoja, fruto, flor...");

            // ===== TAREAS DE SEMILLERO =====
            long tarea11 = insertarTarea(db, "Siembra", "Siembra de semillas");
            long tarea12 = insertarTarea(db, "Regar manual", "Tomar especial atención con las plantas pequeñas");

            // ===== TAREAS ADMINISTRATIVAS (CASA/OFICINA) =====
            long tarea13 = insertarTarea(db, "Reuniones con clientes", "Coordinación y ventas");
            long tarea14 = insertarTarea(db, "Gestión de facturas", "Contabilidad y pagos");
            long tarea15 = insertarTarea(db, "Gestión de empleados", "Nóminas, contratos, planificación");
            long tarea16 = insertarTarea(db, "Gestión de pedidos", "Compra de insumos, semillas, etc.");
            long tarea17 = insertarTarea(db, "Elaboración de informes", "Reportes de producción");
            long tarea18 = insertarTarea(db, "Llamadas telefónicas", "Atención a clientes/proveedores");

            long tarea19 = insertarTarea(db, "Presentar proyecto", "Presentar mi magnífico proyecto a los profes");
            long tarea20 = insertarTarea(db, "Pruebas de la aplicación", "Pruebas varias");

            long tarea21 = insertarTarea(db, "Hacer como que trabajo", "Mirar memes mientras estoy conectado al teams");
            long tarea22 = insertarTarea(db, "No trabajar", "Mirar memes pero sin estar conectado al teams");

            // ===== ASIGNAR TAREAS A LOCALIZACIONES =====

            // CASA
            insertarLocTarea(db, casaId, tarea13);
            insertarLocTarea(db, casaId, tarea14);
            insertarLocTarea(db, casaId, tarea15);
            insertarLocTarea(db, casaId, tarea16);
            insertarLocTarea(db, casaId, tarea17);
            insertarLocTarea(db, casaId, tarea18);

            // INVERNADERO
            insertarLocTarea(db, invernaderoId, tarea1);
            insertarLocTarea(db, invernaderoId, tarea2);
            insertarLocTarea(db, invernaderoId, tarea3);
            insertarLocTarea(db, invernaderoId, tarea4);
            insertarLocTarea(db, invernaderoId, tarea5);
            insertarLocTarea(db, invernaderoId, tarea6);
            insertarLocTarea(db, invernaderoId, tarea7);
            insertarLocTarea(db, invernaderoId, tarea8);
            insertarLocTarea(db, invernaderoId, tarea11);
            insertarLocTarea(db, invernaderoId, tarea10);

            // SEMILLERO
            insertarLocTarea(db, semilleroId, tarea1);
            insertarLocTarea(db, semilleroId, tarea2);
            insertarLocTarea(db, semilleroId, tarea9);
            insertarLocTarea(db, semilleroId, tarea10);
            insertarLocTarea(db, semilleroId, tarea11);
            insertarLocTarea(db, semilleroId, tarea12);


            insertarLocTarea(db, instiId, tarea19);
            insertarLocTarea(db, instiId, tarea20);
            insertarLocTarea(db, instiId, tarea13);
            insertarLocTarea(db, instiId, tarea18);

            insertarLocTarea(db, casaSergioId, tarea21);
            insertarLocTarea(db, casaSergioId, tarea22);

            insertarLocTarea(db, casaPedro, tarea21);
            insertarLocTarea(db, casaPedro, tarea22);




            db.setTransactionSuccessful();
            Log.d("DBHelper", "Datos iniciales insertados: 4 empleados, 6 localizaciones (2 zonas + 4 áreas), 20 tareas");
        } finally {
            db.endTransaction();
        }
    }

    private void insertarEmpleado(SQLiteDatabase db, String nombre, String dni) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("dni", dni);
        db.insert("Empleado", null, valores);
    }

    private long insertarLocalizacion(SQLiteDatabase db, String nombre, double lat, double lon, int radio, String tipo, Integer idZonaPadre) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("latitud", lat);
        valores.put("longitud", lon);
        valores.put("radio", radio);
        valores.put("tipo", tipo);
        if (idZonaPadre != null) {
            valores.put("idZonaPadre", idZonaPadre);
        }
        return db.insert("Localizacion", null, valores);
    }

    private long insertarTarea(SQLiteDatabase db, String nombre, String descripcion) {
        ContentValues valores = new ContentValues();
        valores.put("nombreTarea", nombre);
        valores.put("descripcion", descripcion);
        return db.insert("Tarea", null, valores);
    }

    private void insertarLocTarea(SQLiteDatabase db, long idLoc, long idTarea) {
        ContentValues valores = new ContentValues();
        valores.put("idLocalizacion", idLoc);
        valores.put("idTarea", idTarea);
        db.insert("LocalizacionTarea", null, valores);
    }

// se queda

    public Cursor obtenerAreasAgrupadasPorFichaje(int idFichaje) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT l.idLocalizacion AS _id, " +
                        "l.nombre AS nombreArea, " +
                        "SUM(v.duracionMinutos) AS tiempoTotal, " +
                        "COUNT(v.idVisita) AS numeroVisitas " +
                        "FROM Visita v " +
                        "INNER JOIN Localizacion l ON v.idLocalizacion = l.idLocalizacion " +
                        "WHERE v.idFichaje = ? " +
                        "GROUP BY l.idLocalizacion " +
                        "ORDER BY tiempoTotal DESC",
                new String[]{String.valueOf(idFichaje)});
    }

    public Cursor obtenerTareasPorLocalizacion(int idLocalizacion) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT lt.idLocTarea AS _id, " +
                        "t.nombreTarea, t.descripcion " +
                        "FROM LocalizacionTarea lt " +
                        "INNER JOIN Tarea t ON lt.idTarea = t.idTarea " +
                        "WHERE lt.idLocalizacion=?",
                new String[]{String.valueOf(idLocalizacion)});
    }

}


