package com.example.arefi.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arefi.DBHelper;
import com.example.arefi.R;
import com.example.arefi.entity.Visita;
import com.example.arefi.repository.TareaRealizadaRepository;
import com.example.arefi.repository.TareaRealizadaRepositoryImpl;
import com.example.arefi.repository.VisitaRepository;
import com.example.arefi.repository.VisitaRepositoryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SeleccionTareasActivity extends AppCompatActivity {

    private ExpandableListView expandableListAreas;
    private Button btnFinalizar;
    private VisitaRepository visitaRepository;
    private TareaRealizadaRepository tareaRealizada;
    private DBHelper gestorBaseDatos;

    private int fichajeId;

    // Estructura de datos
    private List<AreaVisitada> listaAreas;
    private HashMap<Integer, List<TareaDisponible>> tareasPorArea;
    private HashMap<String, Boolean> tareasSeleccionadas; // "idVisita-idTarea" -> checked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_tareas);

        // Obtener ID del fichaje
        fichajeId = getIntent().getIntExtra("fichaje_id", -1);

        if (fichajeId == -1) {
            Toast.makeText(this, "Error: No se recibió ID de fichaje", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar
        expandableListAreas = findViewById(R.id.expandableListAreas);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        gestorBaseDatos = new DBHelper(this);
        visitaRepository = new VisitaRepositoryImpl(this);
        tareaRealizada = new TareaRealizadaRepositoryImpl(this);

        listaAreas = new ArrayList<>();
        tareasPorArea = new HashMap<>();
        tareasSeleccionadas = new HashMap<>();

        // Cargar datos
        cargarAreasYTareas();

        // Configurar adaptador
        AdaptadorAreasExpandible adaptador = new AdaptadorAreasExpandible();
        expandableListAreas.setAdapter(adaptador);

        // Botón finalizar
        btnFinalizar.setOnClickListener(v -> guardarTareasYFinalizar());

        Log.d("SeleccionTareas", "Activity creada para fichaje ID: " + fichajeId);
    }

    // ==================== CARGAR DATOS ====================

    private void cargarAreasYTareas() {
        // Obtener áreas visitadas agrupadas
        Cursor cursorAreas = gestorBaseDatos.obtenerAreasAgrupadasPorFichaje(fichajeId);

        while (cursorAreas.moveToNext()) {
            int idArea = cursorAreas.getInt(cursorAreas.getColumnIndexOrThrow("_id"));
            String nombreArea = cursorAreas.getString(cursorAreas.getColumnIndexOrThrow("nombreArea"));
            int tiempoTotal = cursorAreas.getInt(cursorAreas.getColumnIndexOrThrow("tiempoTotal"));

            AreaVisitada area = new AreaVisitada(idArea, nombreArea, tiempoTotal);
            listaAreas.add(area);

            // Cargar tareas de esta área
            List<TareaDisponible> tareas = cargarTareasDeArea(idArea);
            tareasPorArea.put(idArea, tareas);

            Log.d("SeleccionTareas", "Área cargada: " + nombreArea + " (" + tiempoTotal + " min)");
        }

        cursorAreas.close();

        if (listaAreas.isEmpty()) {
            Toast.makeText(this, "No se visitaron áreas específicas", Toast.LENGTH_SHORT).show();
        }
    }

    private List<TareaDisponible> cargarTareasDeArea(int idArea) {
        List<TareaDisponible> tareas = new ArrayList<>();

        Cursor cursorTareas = gestorBaseDatos.obtenerTareasPorLocalizacion(idArea);

        while (cursorTareas.moveToNext()) {
            int idLocTarea = cursorTareas.getInt(cursorTareas.getColumnIndexOrThrow("_id"));
            String nombreTarea = cursorTareas.getString(cursorTareas.getColumnIndexOrThrow("nombreTarea"));

            TareaDisponible tarea = new TareaDisponible(idLocTarea, nombreTarea);
            tareas.add(tarea);
        }

        cursorTareas.close();

        return tareas;
    }

    // ==================== GUARDAR TAREAS ====================
    private void guardarTareasYFinalizar() {
        int tareasGuardadas = 0;

        // Obtener todas las visitas del fichaje usando repository
        List<Visita> visitas = visitaRepository.obtenerVisitasPorFichaje(fichajeId);

        for (Visita visita : visitas) {
            int idVisita = visita.getIdVisita();
            int idArea = visita.getIdLocalizacion();

            // Verificar qué tareas fueron seleccionadas para esta área
            List<TareaDisponible> tareasDeArea = tareasPorArea.get(idArea);

            if (tareasDeArea != null) {
                for (TareaDisponible tarea : tareasDeArea) {
                    String clave = idArea + "-" + tarea.idLocTarea;

                    if (tareasSeleccionadas.containsKey(clave) && tareasSeleccionadas.get(clave)) {
                        // Guardar usando repository
                        tareaRealizada.guardarTareaRealizada(idVisita, tarea.idLocTarea);
                        tareasGuardadas++;

                        Log.d("SeleccionTareas", "Tarea guardada: " + tarea.nombreTarea + " en visita " + idVisita);
                    }
                }
            }
        }

        Toast.makeText(this,
                "✅ Fichaje completado\n" + tareasGuardadas + " tareas registradas",
                Toast.LENGTH_LONG).show();

        Log.d("SeleccionTareas", "Total tareas guardadas: " + tareasGuardadas);

        finish(); // Volver a MainActivity
    }


    // ==================== ADAPTADOR EXPANDIBLE ====================

    private class AdaptadorAreasExpandible extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return listaAreas.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int idArea = listaAreas.get(groupPosition).idArea;
            List<TareaDisponible> tareas = tareasPorArea.get(idArea);
            return tareas != null ? tareas.size() : 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return listaAreas.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) { // devuelve la tarea
            int idArea = listaAreas.get(groupPosition).idArea;
            return tareasPorArea.get(idArea).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(SeleccionTareasActivity.this);
                convertView = inflater.inflate(R.layout.item_area_grupo, parent, false);
            }

            AreaVisitada area = listaAreas.get(groupPosition);

            TextView tvNombreArea = convertView.findViewById(R.id.tvNombreArea);
            TextView tvTiempoArea = convertView.findViewById(R.id.tvTiempoArea);

            tvNombreArea.setText(area.nombreArea);
            tvTiempoArea.setText(area.tiempoTotal + " min");

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(SeleccionTareasActivity.this);
                convertView = inflater.inflate(R.layout.item_tarea_checkbox, parent, false);
            }

            AreaVisitada area = listaAreas.get(groupPosition);
            TareaDisponible tarea = tareasPorArea.get(area.idArea).get(childPosition);

            CheckBox checkboxTarea = convertView.findViewById(R.id.checkboxTarea);
            checkboxTarea.setText(tarea.nombreTarea);

            String clave = area.idArea + "-" + tarea.idLocTarea;

            // =====  PASO 1: QUITAR listener ANTES de cambiar estado =====
            checkboxTarea.setOnCheckedChangeListener(null);

            // =====  PASO 2: Restaurar estado del checkbox =====
            boolean estaSeleccionada = tareasSeleccionadas.containsKey(clave) && tareasSeleccionadas.get(clave);
            checkboxTarea.setChecked(estaSeleccionada);

            // =====  PASO 3: Poner listener DESPUÉS de restaurar =====
            checkboxTarea.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tareasSeleccionadas.put(clave, isChecked);
                Log.d("SeleccionTareas", "Tarea " + (isChecked ? "marcada" : "desmarcada") + ": " + tarea.nombreTarea);
            });

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    // ==================== CLASES DE DATOS ====================

    private class AreaVisitada {
        int idArea;
        String nombreArea;
        int tiempoTotal;

        AreaVisitada(int idArea, String nombreArea, int tiempoTotal) {
            this.idArea = idArea;
            this.nombreArea = nombreArea;
            this.tiempoTotal = tiempoTotal;
        }
    }

    private class TareaDisponible {
        int idLocTarea;
        String nombreTarea;

        TareaDisponible(int idLocTarea, String nombreTarea) {
            this.idLocTarea = idLocTarea;
            this.nombreTarea = nombreTarea;
        }
    }
}