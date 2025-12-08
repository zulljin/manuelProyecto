package com.example.arefi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.arefi.R;
import com.example.arefi.repository.EmpleadoRepository;
import com.example.arefi.repository.EmpleadoRepositoryImpl;

public class BienvenidaActivity extends AppCompatActivity {

    private EditText etDNI;
    private Button btnEntrar;
    private EmpleadoRepository empleadoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bienvenida);

        etDNI = findViewById(R.id.etDni);
        btnEntrar = findViewById(R.id.btnEntrar);

        // Inicializamos el Repository
        empleadoRepository = new EmpleadoRepositoryImpl(this);

        btnEntrar.setOnClickListener(v -> {
            String dniIngresado = etDNI.getText().toString().trim().toUpperCase();

            if (dniIngresado.isEmpty()) {
                Toast.makeText(this, "Introduce tu DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buscamos al empleado por DNI
            int empleadoId = empleadoRepository.obtenerIdEmpleadoPorDni(dniIngresado);

            if (empleadoId != -1) {
                // DNI correcto → Pasar a MainActivity
                Intent intent = new Intent(BienvenidaActivity.this, MainActivity.class);
                intent.putExtra("empleado_id", empleadoId);
                startActivity(intent);
                finish();
            } else {
                mostrarErrorDNI();
            }
        });
    }

    private void mostrarErrorDNI() {
        new AlertDialog.Builder(this)
                .setTitle("Error en DNI")
                .setMessage("El DNI introducido no está registrado.\n\nDNI de prueba: 12345678A")
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
