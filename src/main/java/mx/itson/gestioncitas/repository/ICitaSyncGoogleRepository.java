/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.CitaSyncGoogle;
import mx.itson.gestioncitas.model.EstadoSyncGoogle;

import java.util.Optional;

/**
 * Contrato para persistencia del registro de sincronización
 * entre citas locales y eventos de Google Calendar.
 */
public interface ICitaSyncGoogleRepository {

    /**
     * Persiste un nuevo registro de sincronización.
     *
     * @param sync objeto a guardar
     * @return sync con su ID asignado
     */
    CitaSyncGoogle guardar(CitaSyncGoogle sync);

    /**
     * Actualiza el estado de sincronización y el ID del evento en Google Calendar.
     *
     * @param citaId       ID de la cita local
     * @param googleEventId ID del evento en Google Calendar (puede ser null si hubo error)
     * @param estado        nuevo estado de sync
     * @param error         mensaje de error (null si fue exitoso)
     */
    void actualizarSync(int citaId, String googleEventId, EstadoSyncGoogle estado, String error);

    /**
     * Busca el registro de sync de una cita.
     *
     * @param citaId ID de la cita local
     * @return Optional con el registro si existe
     */
    Optional<CitaSyncGoogle> buscarPorCita(int citaId);

    /**
     * Elimina el registro de sync de una cita (al cancelarla).
     *
     * @param citaId ID de la cita local
     */
    void eliminarPorCita(int citaId);
}

