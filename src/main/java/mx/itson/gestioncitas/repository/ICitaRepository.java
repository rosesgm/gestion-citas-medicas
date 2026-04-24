package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ICitaRepository {

    /**
     * Persiste una nueva cita y asigna el ID generado por la BD.
     *
     * @param cita objeto a guardar
     * @return la misma cita con su ID asignado
     */
    Cita guardar(Cita cita);

    /**
     * @param id identificador de la cita
     * @return Optional con la cita si existe
     */
    Optional<Cita> buscarPorId(int id);

    /**
     * @param pacienteId ID del paciente
     * @return lista de citas, puede estar vacía
     */
    List<Cita> listarPorPaciente(int pacienteId);

    /**
     * Actualiza el estado de una cita existente.
     *
     * @param citaId      ID de la cita
     * @param nuevoEstado estado destino
     */
    void actualizarEstado(int citaId, EstadoCita nuevoEstado);

    /**
     * Verifica si un médico ya tiene una cita activa en la fecha y hora indicadas.
     *
     * @param medicoId ID del médico
     * @param fecha    fecha a verificar
     * @param hora     hora a verificar
     * @return true si el horario está ocupado
     */
    boolean existeCitaEnHorario(int medicoId, LocalDate fecha, LocalTime hora);

    List<Cita> listarTodas();
}