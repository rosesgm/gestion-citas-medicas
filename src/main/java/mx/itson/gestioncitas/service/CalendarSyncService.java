/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.service;

import mx.itson.gestioncitas.model.*;
import mx.itson.gestioncitas.repository.ICitaSyncGoogleRepository;

import java.io.IOException;
import java.util.Optional;

/**
 * Orquesta la sincronización entre citas locales y Google Calendar.
 *
 * Responsabilidades:
 * - Crear el evento en Google Calendar al agendar una cita.
 * - Eliminar el evento al cancelar una cita.
 * - Registrar el resultado (éxito/error) en la tabla citas_sync_google.
 *
 * Esta clase no lanza excepciones hacia arriba: los errores de Google Calendar
 * se registran en BD como estado ERROR para no bloquear el flujo principal.
 */
public class CalendarSyncService {

    private final GoogleCalendarService      calendarService;
    private final ICitaSyncGoogleRepository  syncRepo;
    private final UsuarioGoogle              usuarioActual;

    public CalendarSyncService(GoogleCalendarService calendarService,
                               ICitaSyncGoogleRepository syncRepo,
                               UsuarioGoogle usuarioActual) {
        this.calendarService = calendarService;
        this.syncRepo        = syncRepo;
        this.usuarioActual   = usuarioActual;
    }

    /**
     * Sincroniza una cita recién agendada con Google Calendar.
     * Crea el registro en citas_sync_google y el evento en Calendar.
     *
     * @param cita cita ya persistida en BD (debe tener ID asignado)
     */
    public void sincronizarNuevaCita(Cita cita) {
        // 1. Registra la intención de sync como PENDIENTE
        CitaSyncGoogle sync = new CitaSyncGoogle(cita.getId(), usuarioActual.getId());
        syncRepo.guardar(sync);

        // 2. Intenta crear el evento en Google Calendar
        try {
            String googleEventId = calendarService.crearEvento(cita);
            syncRepo.actualizarSync(cita.getId(), googleEventId,
                    EstadoSyncGoogle.SINCRONIZADA, null);

            System.out.println("[CalendarSyncService] Cita " + cita.getId()
                    + " sincronizada → evento " + googleEventId);

        } catch (IOException e) {
            // No bloquea el flujo; registra el error para reintento futuro
            syncRepo.actualizarSync(cita.getId(), null,
                    EstadoSyncGoogle.ERROR, truncar(e.getMessage(), 500));

            System.err.println("[CalendarSyncService] Error al sincronizar cita "
                    + cita.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Elimina el evento de Google Calendar al cancelar una cita.
     *
     * @param citaId ID de la cita cancelada
     */
    public void eliminarEventoCancelada(int citaId) {
        Optional<CitaSyncGoogle> syncOpt = syncRepo.buscarPorCita(citaId);

        if (syncOpt.isEmpty()) {
            System.out.println("[CalendarSyncService] No hay sync registrado para cita " + citaId);
            return;
        }

        CitaSyncGoogle sync = syncOpt.get();
        if (sync.getGoogleEventId() == null) {
            // La cita nunca se sincronizó correctamente
            syncRepo.eliminarPorCita(citaId);
            return;
        }

        try {
            calendarService.eliminarEvento(sync.getGoogleEventId());
            syncRepo.eliminarPorCita(citaId);
            System.out.println("[CalendarSyncService] Evento eliminado para cita " + citaId);

        } catch (IOException e) {
            syncRepo.actualizarSync(citaId, sync.getGoogleEventId(),
                    EstadoSyncGoogle.ERROR, truncar(e.getMessage(), 500));
            System.err.println("[CalendarSyncService] Error al eliminar evento de cita "
                    + citaId + ": " + e.getMessage());
        }
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private String truncar(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
