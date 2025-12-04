package com.example.arefi.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arefi.R;
import com.example.arefi.entity.Empleado;
import com.example.arefi.entity.Fichaje;
import com.example.arefi.repository.EmpleadoRepository;
import com.example.arefi.repository.EmpleadoRepositoryImpl;
import com.example.arefi.repository.FichajeRepository;
import com.example.arefi.repository.FichajeRepositoryImpl;
import com.example.arefi.repository.TareaRealizadaRepository;
import com.example.arefi.repository.TareaRealizadaRepositoryImpl;
import com.example.arefi.repository.VisitaRepository;
import com.example.arefi.repository.VisitaRepositoryImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformeActivity extends AppCompatActivity {

    private TextView tvNombreEmpleado, tvFechaHoy, tvTotalHoy, tvTotalMes, tvSinFichajes;
    private LinearLayout layoutFichajes;
    private Button btnVolver;
    private EmpleadoRepository empleadoRepository;
    private FichajeRepository fichajeRepository;
    private VisitaRepository visitaRepository;
    private TareaRealizadaRepository tareaRealizadaRepository;
    private int empleadoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe);

        // Obtener ID del empleado
        empleadoId = getIntent().getIntExtra("empleado_id", -1);
        if (empleadoId == -1) {
            finish();
            return;
        }

        // Inicializar repository y DBHelper
        empleadoRepository = new EmpleadoRepositoryImpl(this);
        fichajeRepository = new FichajeRepositoryImpl(this);
        visitaRepository = new VisitaRepositoryImpl(this);
        tareaRealizadaRepository = new TareaRealizadaRepositoryImpl(this);


        // Inicializar vistas
        tvNombreEmpleado = findViewById(R.id.tvNombreEmpleado);
        tvFechaHoy = findViewById(R.id.tvFechaHoy);
        tvTotalHoy = findViewById(R.id.tvTotalHoy);
        tvTotalMes = findViewById(R.id.tvTotalMes);
        tvSinFichajes = findViewById(R.id.tvSinFichajes);
        layoutFichajes = findViewById(R.id.layoutFichajes);
        btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());

        cargarInforme();
    }

    // ==================== CARGAR DATOS ====================
    private void cargarInforme() {
        // Nombre del empleado
        cargarNombreEmpleado();

        // Fecha actual
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvFechaHoy.setText("Hoy: " + formatoFecha.format(new Date()));

        // Total horas hoy
        int minutosHoy = fichajeRepository.calcularHorasTrabajadasHoy(empleadoId);
        int horasHoy = minutosHoy / 60;
        int minsHoy = minutosHoy % 60;
        tvTotalHoy.setText("Total hoy: " + horasHoy + "h " + minsHoy + "m");

        // Total horas mes
        int minutosMes = fichajeRepository.calcularHorasTrabajadasMes(empleadoId);
        int horasMes = minutosMes / 60;
        int minsMes = minutosMes % 60;
        tvTotalMes.setText("Total mes: " + horasMes + "h " + minsMes + "m");

        // Fichajes del día
        cargarFichajesDelDia();
    }


    private void cargarNombreEmpleado() {
        Empleado empleado = empleadoRepository.obtenerEmpleadoPorId(empleadoId);
        if (empleado != null) {
            tvNombreEmpleado.setText("Usuario:  " + empleado.getNombre());
        } else {
            tvNombreEmpleado.setText("Usuario:  Desconocido");
        }
    }

    private void cargarFichajesDelDia() {
        List<Fichaje> fichajesHoy = fichajeRepository.obtenerFichajesHoy(empleadoId);

        if (fichajesHoy.isEmpty()) {
            tvSinFichajes.setVisibility(View.VISIBLE);
            layoutFichajes.setVisibility(View.GONE);
        } else {
            tvSinFichajes.setVisibility(View.GONE);
            layoutFichajes.setVisibility(View.VISIBLE);

            int numeroFichaje = 1;
            for (Fichaje fichaje : fichajesHoy) {
                crearCardFichaje(fichaje.getIdFichaje(), numeroFichaje, fichaje.getHoraEntrada(),
                        fichaje.getHoraSalida(), fichaje.getEstado());
                numeroFichaje++;
            }
        }
    }


    // ------------------- CREAR VISTAS DINÁMICAS ---------------------

    private void crearCardFichaje(int idFichaje, int numero, long entrada, long salida, String estado) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardFichaje = inflater.inflate(R.layout.item_fichaje_informe, layoutFichajes, false);

        TextView tvCabecera = cardFichaje.findViewById(R.id.tvCabeceraFichaje);
        LinearLayout layoutVisitas = cardFichaje.findViewById(R.id.layoutVisitas);

        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String horaEntrada = formatoHora.format(new Date(entrada));
        String horaSalida = estado.equals("FINALIZADO") ? formatoHora.format(new Date(salida)) : "En curso";

        String duracion = "";
        if (estado.equals("FINALIZADO")) {
            int minutos = (int) ((salida - entrada) / 60000);
            int horas = minutos / 60;
            int mins = minutos % 60;
            duracion = " (" + horas + "h " + mins + "m)";
        }

        tvCabecera.setText("Fichaje #" + numero + ": " + horaEntrada + " - " + horaSalida + duracion);

        // Cargar visitas de este fichaje
        cargarVisitasDeFichaje(idFichaje, layoutVisitas);

        layoutFichajes.addView(cardFichaje);
    }

    private void cargarVisitasDeFichaje(int idFichaje, LinearLayout containerVisitas) {

        // ANTES → Cursor cursorAreas = gestorBaseDatos.obtenerAreasAgrupadasPorFichajeConTiempoTotal(idFichaje);
        Cursor cursorAreas = visitaRepository.obtenerAreasAgrupadasPorFichajeConTiempoTotal(idFichaje);

        if (cursorAreas.getCount() == 0) {
            TextView tvSinVisitas = new TextView(this);
            tvSinVisitas.setText("  Sin áreas visitadas");
            tvSinVisitas.setTextSize(12);
            tvSinVisitas.setPadding(16, 8, 0, 0);
            containerVisitas.addView(tvSinVisitas);
        } else {
            while (cursorAreas.moveToNext()) {
                int idArea = cursorAreas.getInt(cursorAreas.getColumnIndexOrThrow("_id"));
                String nombreArea = cursorAreas.getString(cursorAreas.getColumnIndexOrThrow("nombreArea"));
                int tiempoTotalArea = cursorAreas.getInt(cursorAreas.getColumnIndexOrThrow("tiempoTotalArea"));

                crearVistaAreaAgrupada(idFichaje, idArea, nombreArea, tiempoTotalArea, containerVisitas);
            }
        }

        cursorAreas.close();
    }



    private void crearVistaAreaAgrupada(int idFichaje, int idArea, String nombreArea, int tiempoTotalMinutos, LinearLayout containerVisitas) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View vistaVisita = inflater.inflate(R.layout.item_visita_informe, containerVisitas, false);

        TextView tvArea = vistaVisita.findViewById(R.id.tvAreaVisita);
        LinearLayout layoutTareas = vistaVisita.findViewById(R.id.layoutTareas);

        String tiempoFormateado;
        if (tiempoTotalMinutos >= 60) {
            int horas = tiempoTotalMinutos / 60;
            int mins = tiempoTotalMinutos % 60;
            tiempoFormateado = horas + "h " + mins + "m";
        } else {
            tiempoFormateado = tiempoTotalMinutos + " min";
        }

        tvArea.setText("Área: " + nombreArea + " - " + tiempoFormateado);

        cargarTareasUnicasDeArea(idFichaje, idArea, layoutTareas);
        containerVisitas.addView(vistaVisita);
    }
    private void cargarTareasUnicasDeArea(int idFichaje, int idArea, LinearLayout containerTareas) {
        // Usamos el repository en vez de DBHelper
        List<String> tareas = tareaRealizadaRepository.obtenerTareasUnicasDeAreaEnFichaje(idFichaje, idArea);

        if (tareas.isEmpty()) {
            TextView tvSinTareas = new TextView(this);
            tvSinTareas.setText("Sin tareas registradas");
            tvSinTareas.setTextSize(12);
            tvSinTareas.setPadding(0, 4, 0, 0);
            containerTareas.addView(tvSinTareas);
        } else {
            for (String nombreTarea : tareas) {
                TextView tvTarea = new TextView(this);
                tvTarea.setText("✓ " + nombreTarea);
                tvTarea.setTextSize(12);
                tvTarea.setPadding(0, 2, 0, 2);
                containerTareas.addView(tvTarea);
            }
        }
    }

}
