package mx.itson.gestioncitas.service;

import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.EstadoCita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;
import mx.itson.gestioncitas.repository.ICitaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AgendarCitaService {

    private final ICitaRepository      citaRepository;
    private final DisponibilidadService disponibilidadService;

    public AgendarCitaService(ICitaRepository citaRepository,
                              DisponibilidadService disponibilidadService) {
        this.citaRepository        = citaRepository;
        this.disponibilidadService = disponibilidadService;
    }

    /**
     * Agenda y confirma una cita médica.
     *
     * @param paciente paciente que agenda
     * @param medico   médico asignado
     * @param fecha    fecha de la cita (no puede ser pasada)
     * @param hora     hora de la cita
     * @param motivo   motivo de la consulta
     * @return la cita persistida en estado CONFIRMADA
     * @throws IllegalArgumentException si algún parámetro es inválido o la fecha es pasada
     * @throws IllegalStateException    si el médico no está disponible en ese horario
     */
    public Cita agendarCita(Paciente paciente, Medico medico,
                            LocalDate fecha, LocalTime hora, String motivo) {

        if (paciente == null) throw new IllegalArgumentException("El paciente no puede ser nulo.");
        if (medico   == null) throw new IllegalArgumentException("El médico no puede ser nulo.");
        if (fecha    == null) throw new IllegalArgumentException("La fecha no puede ser nula.");
        if (hora     == null) throw new IllegalArgumentException("La hora no puede ser nula.");
        if (fecha.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("No se puede agendar una cita en fecha pasada.");

        if (!disponibilidadService.estaDisponible(medico, fecha, hora)) {
            throw new IllegalStateException(
                    "El médico " + medico.getNombre()
                            + " no está disponible el " + fecha + " a las " + hora
            );
        }

        Cita cita = new Cita(paciente, medico, fecha, hora, motivo);
        cita = citaRepository.guardar(cita);
        cita.confirmar();
        citaRepository.actualizarEstado(cita.getId(), EstadoCita.CONFIRMADA);

        System.out.println("[AgendarCitaService] Cita agendada exitosamente: " + cita);
        return cita;
    }

    /**
     * Lista todas las citas registradas de un paciente.
     *
     * @param pacienteId ID del paciente
     * @return lista de citas, puede estar vacía
     */
    public List<Cita> listarCitasPorPaciente(int pacienteId) {
        return citaRepository.listarPorPaciente(pacienteId);
    }

    /**
     * Cancela una cita existente.
     *
     * @param citaId ID de la cita a cancelar
     * @throws IllegalStateException si la cita no existe o su estado no permite cancelación
     */
    public void cancelarCita(int citaId) {
        Optional<Cita> optional = citaRepository.buscarPorId(citaId);

        Cita cita = optional.orElseThrow(() ->
                new IllegalStateException("Cita con ID " + citaId + " no encontrada.")
        );

        cita.cancelar();
        citaRepository.actualizarEstado(citaId, EstadoCita.CANCELADA);
        System.out.println("[AgendarCitaService] Cita " + citaId + " cancelada.");
    }
}