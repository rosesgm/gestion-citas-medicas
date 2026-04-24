package mx.itson.gestioncitas.service;

import mx.itson.gestioncitas.model.CitaSyncGoogle;
import mx.itson.gestioncitas.model.EstadoSyncGoogle;
import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.ICitaSyncGoogleRepository;

import java.io.IOException;
import java.util.Optional;

/**
 * Orquesta la sincronización entre citas locales y Google Calendar.
 * Los errores de Calendar se registran en BD como ERROR sin bloquear el flujo principal.
 */
public class CalendarSyncService {

    private final GoogleCalendarService     calendarService;
    private final ICitaSyncGoogleRepository syncRepo;
    private final UsuarioGoogle             usuarioActual;

    public CalendarSyncService(GoogleCalendarService calendarService,
                               ICitaSyncGoogleRepository syncRepo,
                               UsuarioGoogle usuarioActual) {
        this.calendarService = calendarService;
        this.syncRepo        = syncRepo;
        this.usuarioActual   = usuarioActual;
    }

    /**
     * Sincroniza una cita recién agendada con Google Calendar.
     * Registra el intento como PENDIENTE antes de llamar a la API;
     * actualiza a SINCRONIZADA o ERROR según el resultado.
     *
     * @param cita cita ya persistida en BD (debe tener ID asignado)
     */
    public void sincronizarNuevaCita(Cita cita) {
        CitaSyncGoogle sync = new CitaSyncGoogle(cita.getId(), usuarioActual.getId());
        syncRepo.guardar(sync);

        try {
            String googleEventId = calendarService.crearEvento(cita);
            syncRepo.actualizarSync(cita.getId(), googleEventId, EstadoSyncGoogle.SINCRONIZADA, null);
            System.out.println("[CalendarSyncService] Cita " + cita.getId()
                    + " → evento " + googleEventId);
        } catch (IOException e) {
            syncRepo.actualizarSync(cita.getId(), null, EstadoSyncGoogle.ERROR, truncar(e.getMessage(), 500));
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
        if (syncOpt.isEmpty()) return;

        CitaSyncGoogle sync = syncOpt.get();
        if (sync.getGoogleEventId() == null) {
            syncRepo.eliminarPorCita(citaId);
            return;
        }

        try {
            calendarService.eliminarEvento(sync.getGoogleEventId());
            syncRepo.eliminarPorCita(citaId);
        } catch (IOException e) {
            syncRepo.actualizarSync(citaId, sync.getGoogleEventId(),
                    EstadoSyncGoogle.ERROR, truncar(e.getMessage(), 500));
            System.err.println("[CalendarSyncService] Error al eliminar evento de cita "
                    + citaId + ": " + e.getMessage());
        }
    }

    private String truncar(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) : s;
    }
}