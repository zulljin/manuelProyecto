package com.example.arefi.repository;

import com.example.arefi.entity.Empleado;

import java.util.List;

public interface EmpleadoRepository {

    Empleado obtenerEmpleadoPorDni(String dni);

    int obtenerIdEmpleadoPorDni(String dni);

    boolean existeDni(String dni);

    Empleado obtenerEmpleadoPorId(int id);

    List<Empleado> obtenerTodosLosEmpleados();

    long insertarEmpleado(Empleado empleado);
}