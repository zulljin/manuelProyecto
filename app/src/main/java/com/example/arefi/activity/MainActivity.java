    package com.example.arefi.activity;

    import android.Manifest;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import com.example.arefi.R;
    import com.example.arefi.TrackingService;
    import com.example.arefi.entity.Empleado;
    import com.example.arefi.entity.Fichaje;
    import com.example.arefi.entity.Visita;
    import com.example.arefi.repository.EmpleadoRepository;
    import com.example.arefi.repository.EmpleadoRepositoryImpl;
    import com.example.arefi.repository.FichajeRepository;
    import com.example.arefi.repository.FichajeRepositoryImpl;
    import com.example.arefi.repository.LocalizacionRepository;
    import com.example.arefi.repository.LocalizacionRepositoryImpl;
    import com.example.arefi.repository.VisitaRepository;
    import com.example.arefi.repository.VisitaRepositoryImpl;

    import java.util.List;

    public class MainActivity extends AppCompatActivity {

        private TextView tvNombre, tvEstado, tvUbicacion;
        private Button btnFichar, btnInforme;
        private FichajeRepository fichajeRepository;
        private EmpleadoRepository empleadoRepository;
        private LocalizacionRepository localizacionRepository;
        private VisitaRepository visitaRepository;

        private int empleadoId = -1;

        private static final int CODIGO_PERMISO_UBICACION = 100;

        private LocationManager gestorUbicacion;

        private AlertDialog dialogoCarga;

        // RECEIVER PARA ESCUCHAR AL TRACKING SERVICE
        private final android.content.BroadcastReceiver areaReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String nombreArea = intent.getStringExtra("nombre_area");
                if (nombreArea != null) {
                    tvUbicacion.setText("Área de trabajo: " + nombreArea);
                    Log.d("MainActivity", "UI actualizada: " + nombreArea);
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Inicializar vistas
            tvNombre = findViewById(R.id.tvNombre);
            tvEstado = findViewById(R.id.tvEstado);
            tvUbicacion = findViewById(R.id.tvUbicacion);
            btnFichar = findViewById(R.id.btnFichar);
            btnInforme = findViewById(R.id.btnInforme);
            fichajeRepository = new FichajeRepositoryImpl(this);
            empleadoRepository = new EmpleadoRepositoryImpl(this);
            localizacionRepository = new LocalizacionRepositoryImpl(this);
            visitaRepository = new VisitaRepositoryImpl(this);

            // Obtener ID del empleado desde Bienvenida
            empleadoId = getIntent().getIntExtra("empleado_id", -1);

            if (empleadoId != -1) {
                mostrarDatosEmpleado(empleadoId);
            } else {
                tvNombre.setText("Empleado desconocido");
                Log.e("MainActivity", "No se recibió empleado_id");
            }

            btnFichar.setOnClickListener(v -> procesarFichaje());
            btnInforme.setOnClickListener(v -> abrirInforme());

            solicitarPermisosUbicacion();
        }

        @Override
        protected void onResume() {  // se usa para volver a actualizar la UI cuando se vuelve a la app
            super.onResume();
            if (empleadoId != -1) {
                actualizarEstadoFichaje();
            }
            // Registrar receiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(areaReceiver, new android.content.IntentFilter("AREA_ACTUALIZADA"), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(areaReceiver, new android.content.IntentFilter("AREA_ACTUALIZADA"));
            }
        }

            @Override
            protected void onPause() {
                super.onPause();
                try {
                    unregisterReceiver(areaReceiver);
                } catch (Exception e) {
                    // Ignorar si no estaba registrado
                }
            }
        //------------------------LÓGICA PRINCIPAL --------------------------------

        private void mostrarDatosEmpleado(int id) {
            Empleado empleado = empleadoRepository.obtenerEmpleadoPorId(id);

            if (empleado != null) {
                tvNombre.setText("👤 " + empleado.getNombre());
            } else {
                tvNombre.setText("👤 Desconocido");
            }

            actualizarEstadoFichaje();
        }


        private void actualizarEstadoFichaje() {
            // Obtener el fichaje activo del empleado
            Fichaje fichajeActivo = fichajeRepository.obtenerFichajeActivo(empleadoId);

            if (fichajeActivo != null) {
                // ESTÁ TRABAJANDO
                int idFichaje = fichajeActivo.getIdFichaje();

                tvEstado.setText("🟢 TRABAJANDO");
                tvEstado.setTextColor(getColor(android.R.color.holo_green_dark));
                btnFichar.setText("🔴 FICHAR SALIDA");
                btnFichar.setEnabled(true);

                // Obtener la visita activa de ese fichaje
                List<Visita> visitas = visitaRepository.obtenerVisitasPorFichaje(idFichaje);
                Visita visitaActiva = null;
                for (Visita v : visitas) {
                    if (v.getHoraSalida() == null) { // significa que sigue activa
                        visitaActiva = v;
                        break;
                    }
                }

                if (visitaActiva != null) {
                    String nombreArea = localizacionRepository.obtenerNombreSegunVisita(visitaActiva.getIdVisita());
                    tvUbicacion.setText("Trabajando en: " + nombreArea);
                    Log.d("MainActivity", "Área actual: " + nombreArea);
                } else {
                    tvUbicacion.setText("Detectando ubicación...");
                }

            } else {
                // NO ESTÁ TRABAJANDO
                tvEstado.setText("⭕ NO TRABAJANDO");
                tvEstado.setTextColor(getColor(android.R.color.darker_gray));
                tvUbicacion.setText("");
                btnFichar.setText("🟢 FICHAR ENTRADA");
                btnFichar.setEnabled(true);
            }
        }




        private void procesarFichaje() {
            Fichaje fichajeActivo = fichajeRepository.obtenerFichajeActivo(empleadoId);

            if (fichajeActivo != null) {
                // TIENE FICHAJE ACTIVO → SALIDA
                int idFichaje = fichajeActivo.getIdFichaje();
                long horaEntrada = fichajeActivo.getHoraEntrada();

                long ahora = System.currentTimeMillis();
                int minutosTrabajados = (int) ((ahora - horaEntrada) / 60000);
                int horas = minutosTrabajados / 60;
                int mins = minutosTrabajados % 60;

                // Finalizar fichaje usando el repository
                fichajeRepository.finalizarFichaje(idFichaje);

                // Calcular total hoy
                int minutosHoy = fichajeRepository.calcularHorasTrabajadasHoy(empleadoId);
                int horasHoy = minutosHoy / 60;
                int minsHoy = minutosHoy % 60;

                detenerTracking();

                // Cerrar última visita activa usando VisitaRepository
                Visita visitaActiva = visitaRepository.obtenerVisitaActiva(idFichaje);
                if (visitaActiva != null) {
                    visitaRepository.finalizarVisita(visitaActiva.getIdVisita());
                    Log.d("MainActivity", "Última visita cerrada al fichar salida");
                }

                String mensaje = "Salida registrada\n" +
                        "Este fichaje: " + horas + "h " + mins + "m\n" +
                        "Total hoy: " + horasHoy + "h " + minsHoy + "m";

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

                actualizarEstadoFichaje();

                Intent intent = new Intent(this, SeleccionTareasActivity.class);
                intent.putExtra("fichaje_id", idFichaje);
                startActivity(intent);

            } else {
                validarZonaYFichar();
            }
        }


        private void abrirInforme() {
            Intent intent = new Intent(this, InformeActivity.class);
            intent.putExtra("empleado_id", empleadoId);
            startActivity(intent);
        }

        //----------------------- PERMISOS -----------------------------

        private void solicitarPermisosUbicacion() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permiso de ubicación ya concedido");
                iniciarGPS();
            } else {
                Log.d("MainActivity", "Solicitando permiso de ubicación...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        CODIGO_PERMISO_UBICACION);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == CODIGO_PERMISO_UBICACION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permiso concedido por el usuario");
                    Toast.makeText(this, "Ubicación activada", Toast.LENGTH_SHORT).show();
                    iniciarGPS();
                } else {
                    Log.d("MainActivity", "Permiso denegado por el usuario");
                    Toast.makeText(this, "Se necesita ubicación para registrar visitas", Toast.LENGTH_LONG).show();
                }
            }
        }

        //----------------------------metodos para el  GPS --------------------------

        private void iniciarGPS() {
            gestorUbicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "No hay permisos de ubicación");
                return;
            }

            Log.d("MainActivity", "GPS iniciado, esperando señal...");
        }

        private void mostrarDialogoCarga() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Verificando ubicación...");
            builder.setCancelable(false);
            dialogoCarga = builder.create();
            dialogoCarga.show();
        }

        private void ocultarDialogoCarga() {
            if (dialogoCarga != null && dialogoCarga.isShowing()) {
                dialogoCarga.dismiss();
            }
        }
        private void validarZonaYFichar() {

            // 1️⃣ Comprobación de permisos de ubicación
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Sin permisos de ubicación", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2️⃣ Comprobación de GPS activado
            if (!gestorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(this)
                        .setTitle("GPS Desactivado")
                        .setMessage("Activa el GPS en Ajustes para poder fichar.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }

            // 3️⃣ Mostrar diálogo de carga mientras se obtiene ubicación
            mostrarDialogoCarga();

            // 4️⃣ Solicitar ubicación única
            gestorUbicacion.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    new LocationListener() {

                        @Override
                        public void onLocationChanged(Location ubicacion) {
                            ocultarDialogoCarga();

                            double lat = ubicacion.getLatitude();
                            double lon = ubicacion.getLongitude();

                            // 5️⃣ Detectar zona usando repository
                            int idZona = localizacionRepository.detectarZonaDeTrabajo(lat, lon);

                            if (idZona != -1) {
                                // Zona válida
                                String nombreZona = localizacionRepository.obtenerNombreLocalizacion(idZona);
                                Toast.makeText(MainActivity.this,
                                        "Fichaje correcto en zona: " + nombreZona,
                                        Toast.LENGTH_LONG).show();

                                // 6️⃣ Crear fichaje usando repository
                                fichajeRepository.crearFichaje(empleadoId);

                                // 7️⃣ Obtener fichaje activo
                                Fichaje fichaje = fichajeRepository.obtenerFichajeActivo(empleadoId);
                                int idFichaje = fichaje != null ? fichaje.getIdFichaje() : -1;
                                Log.d("MainActivity", "Fichaje creado ID (repo): " + idFichaje);

                                // 8️⃣ Detectar área usando repository
                                int areaActual = localizacionRepository.detectarArea(lat, lon);

                                if (areaActual != -1) {
                                    // Crear visita usando repository
                                    visitaRepository.crearVisita(idFichaje, areaActual, 0);

                                    String nombreArea = localizacionRepository.obtenerNombreLocalizacion(areaActual);
                                    Log.d("MainActivity", "Primera visita creada: " + nombreArea);
                                }

                                // 9️⃣ Actualizar UI y tracking
                                actualizarEstadoFichaje();
                                iniciarTracking(idFichaje);

                            } else {
                                // Fuera de zona de trabajo
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Fuera de zona de trabajo")
                                        .setMessage("No puedes fichar desde tu ubicación actual.\n\nDebes estar en el trabajo para poder fichar.")
                                        .setPositiveButton("Entendido", null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }

                        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override public void onProviderEnabled(String provider) {}
                        @Override public void onProviderDisabled(String provider) {}
                    },
                    null
            );
        }




        // -------------------- TRACKING -------------------------
        private void iniciarTracking(int idFichaje) {
            Intent serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.putExtra("fichaje_id", idFichaje);
            serviceIntent.putExtra("empleado_id", empleadoId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d("MainActivity", "Servicio de tracking iniciado");
        }

        private void detenerTracking() {
            Intent serviceIntent = new Intent(this, TrackingService.class);
            stopService(serviceIntent);
            Log.d("MainActivity", "Servicio de tracking detenido");
        }
    }