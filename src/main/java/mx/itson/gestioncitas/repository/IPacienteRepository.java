package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.Paciente;

import java.util.List;
import java.util.Optional;

public interface IPacienteRepository {

    /** @return todos los pacientes registrados, ordenados por ID. */
    List<Paciente> listarTodos();

    /** @return el paciente con ese ID, o vacío si no existe. */
    Optional<Paciente> buscarPorId(int id);
}