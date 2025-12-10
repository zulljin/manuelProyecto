package com.example.arefi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.arefi.activity.MainActivity;
import com.example.arefi.activity.SeleccionTareasActivity;
import com.example.arefi.entity.Visita;
import com.example.arefi.repository.FichajeRepository;
import com.example.arefi.repository.FichajeRepositoryImpl;
import com.example.arefi.repository.LocalizacionRepository;
import com.example.arefi.repository.LocalizacionRepositoryImpl;
import com.example.arefi.repository.VisitaRepository;
import com.example.arefi.repository.VisitaRepositoryImpl;

public class TrackingService extends Service {  // es necesario que sea service para que se ejecute en un segundo plano



    private static final String CHANNEL_ID = "TrackingChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int INTERVALO_TRACKING =  60 * 1000; // 5 * 60 * 1000; 5 minutos

    private Handler handlerTracking;
    private Runnable runnableTracking;
    private LocationManager gestorUbicacion;
    private VisitaRepository visitaRepository;
    private LocalizacionRepository localizacionRepository;
    private FichajeRepository fichajeRepository;

    private int fichajeActivoId = -1;


    @Override
    public void onCreate() {
        super.onCreate();
        fichajeRepository = new FichajeRepositoryImpl(this);
        visitaRepository = new VisitaRepositoryImpl(this);
        localizacionRepository = new LocalizacionRepositoryImpl(this);

        gestorUbicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("TrackingService", "Servicio creado");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            fichajeActivoId = intent.getIntExtra("fichaje_id", -1);

            Log.d("TrackingService", "Tracking iniciado - Fichaje ID: " + fichajeActivoId);

            // Crear notificación
            crearCanalNotificacion();
            startForeground(NOTIFICATION_ID, crearNotificacion());

            // Iniciar tracking
            iniciarTracking();
        }

        return START_STICKY; // Se reinicia si el sistema lo mata
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detenerTracking();
        Log.d("TrackingService", "Servicio detenido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking de Fichaje",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Seguimiento de ubicación durante fichaje");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification crearNotificacion() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fichaje en curso")
                .setContentText("Registrando ubicación cada 5 minutos")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    // ----------------------TRACKING --------------------------

    private void iniciarTracking() {
        handlerTracking = new Handler();

        runnableTracking = new Runnable() {
            @Override
            public void run() {
                if (fichajeActivoId == -1) {
                    Log.d("TrackingService", "Fichaje cerrado - Deteniendo servicio");
                    stopSelf();
                    return;
                }

                Log.d("TrackingService", "Verificando área...");
                verificarYActualizarArea();

                handlerTracking.postDelayed(this, INTERVALO_TRACKING);
            }
        };

        handlerTracking.post(runnableTracking);
        Log.d("TrackingService", "Tracking iniciado cada 5 minutos");
    }

    private void detenerTracking() {
        if (handlerTracking != null && runnableTracking != null) {
            handlerTracking.removeCallbacks(runnableTracking);
            Log.d("TrackingService", "Tracking detenido");
        }
    }

    private void verificarYActualizarArea() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("TrackingService", "Sin permisos de ubicación");
            return;
        }

        gestorUbicacion.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location ubicacion) {
                        procesarUbicacionTracking(ubicacion);
                    }
                },
                null
        );
    }
    private void procesarUbicacionTracking(Location ubicacion) {
        double lat = ubicacion.getLatitude();
        double lon = ubicacion.getLongitude();

        int zonaActual = localizacionRepository.detectarZonaDeTrabajo(lat, lon);

        if (zonaActual == -1) {
            // SALIÓ DE ZONA - Cerrar fichaje automáticamente
            Log.d("TrackingService", "Salió de zona - Cerrando fichaje");

            Visita visitaActiva = visitaRepository.obtenerVisitaActiva(fichajeActivoId);
            if (visitaActiva != null) {
                visitaRepository.finalizarVisita(visitaActiva.getIdVisita());
            }

            fichajeRepository.finalizarFichaje(fichajeActivoId);
            mostrarToast("Saliste de la zona de trabajo\nFichaje cerrado automáticamente");
            abrirSeleccionTareas();
            stopSelf();
            return;
        }

        // DETECTAR ÁREA
        int areaActual = localizacionRepository.detectarArea(lat, lon);
        String nombreAreaActual = localizacionRepository.obtenerNombreLocalizacion(areaActual);

        // Obtener visita activa
        Visita visitaActiva = visitaRepository.obtenerVisitaActiva(fichajeActivoId);

        if (visitaActiva != null) {
            int areaVisitaActiva = visitaActiva.getIdLocalizacion();

            // ✅ COMPARAR IDs, NO NOMBRES
            if (areaActual == areaVisitaActiva) {
                // Sigue en la misma área
                Intent intent = new Intent("AREA_ACTUALIZADA");
                intent.putExtra("nombre_area", nombreAreaActual);
                sendBroadcast(intent);

                Log.d("TrackingService", "Sigue en: " + nombreAreaActual);

            } else {
                // CAMBIÓ DE ÁREA
                visitaRepository.finalizarVisita(visitaActiva.getIdVisita());

                if (areaActual != -1) {
                    visitaRepository.crearVisita(fichajeActivoId, areaActual, 0);

                    Intent intent = new Intent("AREA_ACTUALIZADA");
                    intent.putExtra("nombre_area", nombreAreaActual);
                    sendBroadcast(intent);

                    Log.d("TrackingService", "Cambió a: " + nombreAreaActual);
                } else {
                    Intent intent = new Intent("AREA_ACTUALIZADA");
                    intent.putExtra("nombre_area", "En tránsito");
                    sendBroadcast(intent);

                    Log.d("TrackingService", "En tránsito");
                }
            }
        } else {
            // No hay visita activa - crear una
            if (areaActual != -1) {
                visitaRepository.crearVisita(fichajeActivoId, areaActual, 0);

                Intent intent = new Intent("AREA_ACTUALIZADA");
                intent.putExtra("nombre_area", nombreAreaActual);
                sendBroadcast(intent);

                Log.d("TrackingService", "Visita creada: " + nombreAreaActual);
            }
        }
    }

    //cuando terminamos el fichaje es cuando abrimos la pantalla de tareas y como puede finalizar al salir con la app en segundo plano
    // mejor ponemos el metodo en el tracking y no en el main para asegurarnos que se cierra el fichaje y que al volver tenga que seleccionar las tareas
    private void abrirSeleccionTareas() {
        Intent intent = new Intent(this, SeleccionTareasActivity.class);
        intent.putExtra("fichaje_id", fichajeActivoId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void mostrarToast(String mensaje) {     // para mostrar un mensaje en el main
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(() -> Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show());
    }
}