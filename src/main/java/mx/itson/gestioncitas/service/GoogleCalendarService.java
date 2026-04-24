/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import mx.itson.gestioncitas.model.Cita;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Crea, actualiza y elimina eventos en Google Calendar.
 * Depende de GoogleAuthService para obtener la Credential activa.
 */
public class GoogleCalendarService {

    private static final String ZONA_HORARIA = "America/Hermosillo"; // Sonora, sin cambio de horario

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
     * Crea un evento en el calendario primario del usuario autenticado.
     *
     * @param cita cita médica a sincronizar
     * @return ID del evento creado en Google Calendar
     * @throws IOException si falla la llamada a la API
     */
    public String crearEvento(Cita cita) throws IOException {
        Event evento = construirEvento(cita);
        Event creado = calendarApi.events().insert("primary", evento).execute();

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
     * Actualiza el título/descripción de un evento existente.
     * Útil si cambia el motivo o el médico de la cita.
     *
     * @param googleEventId ID del evento en Google Calendar
     * @param cita          cita con los datos actualizados
     * @throws IOException si falla la llamada a la API
     */
    public void actualizarEvento(String googleEventId, Cita cita) throws IOException {
        Event evento = construirEvento(cita);
        calendarApi.events().update("primary", googleEventId, evento).execute();
        System.out.println("[GoogleCalendarService] Evento actualizado: " + googleEventId);
    }

    // ── privados ─────────────────────────────────────────────────────────────

    private Event construirEvento(Cita cita) {
        // Inicio: fecha + hora de la cita
        LocalDateTime inicio = LocalDateTime.of(cita.getFecha(), cita.getHora());
        // Fin: 30 minutos después (duración estándar de consulta)
        LocalDateTime fin = inicio.plusMinutes(30);

        String titulo = "Cita médica: " + cita.getMedico().getNombre()
                + " — " + cita.getMedico().getEspecialidad();

        String descripcion = String.format(
                "Paciente: %s\nMédico: %s\nConsultorio: %s\nMotivo: %s",
                cita.getPaciente().getNombre(),
                cita.getMedico().getNombre(),
                cita.getMedico().getConsultorio(),
                cita.getMotivo() != null ? cita.getMotivo() : "—"
        );

        return new Event()
                .setSummary(titulo)
                .setDescription(descripcion)
                .setLocation(cita.getMedico().getConsultorio())
                .setStart(toEventDateTime(inicio))
                .setEnd(toEventDateTime(fin));
    }

    private EventDateTime toEventDateTime(LocalDateTime ldt) {
        // Convierte LocalDateTime a RFC3339 con offset de zona horaria
        ZoneId zona = ZoneId.of(ZONA_HORARIA);
        String offsetStr = zona.getRules()
                .getOffset(ldt.atZone(zona).toInstant())
                .toString();

        // Google necesita formato: 2025-06-15T10:30:00-07:00
        String formatted = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                + offsetStr;

        return new EventDateTime()
                .setDateTime(new DateTime(formatted))
                .setTimeZone(ZONA_HORARIA);
    }
}
