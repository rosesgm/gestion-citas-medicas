package mx.itson.gestioncitas.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import mx.itson.gestioncitas.model.Cita;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Crea, actualiza y elimina eventos en Google Calendar del admin autenticado. */
public class GoogleCalendarService {

    private static final String ZONA_HORARIA = "America/Hermosillo";

    private final Calendar calendarApi;

    public GoogleCalendarService(GoogleAuthService auth) throws IOException {
        calendarApi = new Calendar.Builder(
                auth.getHttpTransport(),
                auth.getJsonFactory(),
                auth.getCredential())
                .setApplicationName(auth.getAppName())
                .build();
    }

    /**
     * Crea un evento en el calendario del admin e invita al paciente por correo.
     *
     * @param cita cita médica a sincronizar
     * @return ID del evento creado en Google Calendar
     * @throws IOException si falla la llamada a la API
     */
    public String crearEvento(Cita cita) throws IOException {
        Event evento = construirEvento(cita);
        Event creado = calendarApi.events()
                .insert("primary", evento)
                .setSendUpdates("all") // envía invitación por correo al paciente
                .execute();
        System.out.println("[GoogleCalendarService] Evento creado: " + creado.getHtmlLink());
        return creado.getId();
    }

    /**
     * Elimina un evento de Google Calendar por su ID.
     *
     * @param googleEventId ID del evento a eliminar
     * @throws IOException si falla la llamada a la API
     */
    public void eliminarEvento(String googleEventId) throws IOException {
        calendarApi.events().delete("primary", googleEventId).execute();
        System.out.println("[GoogleCalendarService] Evento eliminado: " + googleEventId);
    }

    /**
     * Actualiza un evento existente con los datos actuales de la cita.
     *
     * @param googleEventId ID del evento en Google Calendar
     * @param cita          cita con los datos actualizados
     * @throws IOException si falla la llamada a la API
     */
    public void actualizarEvento(String googleEventId, Cita cita) throws IOException {
        calendarApi.events().update("primary", googleEventId, construirEvento(cita)).execute();
        System.out.println("[GoogleCalendarService] Evento actualizado: " + googleEventId);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Event construirEvento(Cita cita) {
        LocalDateTime inicio = LocalDateTime.of(cita.getFecha(), cita.getHora());
        LocalDateTime fin    = inicio.plusMinutes(30);

        String titulo = "Cita médica: " + cita.getMedico().getNombre()
                + " — " + cita.getMedico().getEspecialidad();
        String descripcion = String.format(
                "Paciente: %s\nMédico: %s\nConsultorio: %s\nMotivo: %s",
                cita.getPaciente().getNombre(),
                cita.getMedico().getNombre(),
                cita.getMedico().getConsultorio(),
                cita.getMotivo() != null ? cita.getMotivo() : "—");

        Event evento = new Event()
                .setSummary(titulo)
                .setDescription(descripcion)
                .setLocation(cita.getMedico().getConsultorio())
                .setStart(toEventDateTime(inicio))
                .setEnd(toEventDateTime(fin));

        // Invitar al paciente si tiene correo registrado
        if (cita.getPaciente().getCorreo() != null
                && !cita.getPaciente().getCorreo().isBlank()) {
            List<EventAttendee> asistentes = new ArrayList<>();
            asistentes.add(new EventAttendee()
                    .setEmail(cita.getPaciente().getCorreo())
                    .setDisplayName(cita.getPaciente().getNombre()));
            evento.setAttendees(asistentes);
        }

        return evento;
    }

    private EventDateTime toEventDateTime(LocalDateTime ldt) {
        ZoneId zona      = ZoneId.of(ZONA_HORARIA);
        String offsetStr = zona.getRules().getOffset(ldt.atZone(zona).toInstant()).toString();
        String formatted = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                + offsetStr;
        return new EventDateTime().setDateTime(new DateTime(formatted)).setTimeZone(ZONA_HORARIA);
    }
}