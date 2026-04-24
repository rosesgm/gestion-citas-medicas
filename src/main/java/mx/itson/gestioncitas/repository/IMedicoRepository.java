package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.Medico;

import java.util.List;
import java.util.Optional;

public interface IMedicoRepository {

    /** @return el médico con ese ID, o vacío si no existe. */
    Optional<Medico> buscarPorId(int id);

    /** @return médicos con disponible = true, ordenados por nombre. */
    List<Medico> listarDisponibles();
}