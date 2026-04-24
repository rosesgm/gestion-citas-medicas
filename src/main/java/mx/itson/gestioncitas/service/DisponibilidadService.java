package mx.itson.gestioncitas.service;

import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.repository.ICitaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public class DisponibilidadService {

    private final ICitaRepository citaRepository;

    public DisponibilidadService(ICitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    /**
     * Determina si un médico está disponible en la fecha y hora indicadas.
     * Retorna false si el médico está inactivo o ya tiene una cita activa en ese horario.
     *
     * @param medico médico a evaluar
     * @param fecha  fecha deseada
     * @param hora   hora deseada
     * @return true si el horario está libre
     */
    public boolean estaDisponible(Medico medico, LocalDate fecha, LocalTime hora) {
        if (!medico.isDisponible()) {
            System.out.println("[DisponibilidadService] Médico inactivo: " + medico.getNombre());
            return false;
        }
        boolean ocupado = citaRepository.existeCitaEnHorario(medico.getId(), fecha, hora);
        if (ocupado)
            System.out.println("[DisponibilidadService] Horario ocupado: "
                    + medico.getNombre() + " el " + fecha + " a las " + hora);
        return !ocupado;
    }
}