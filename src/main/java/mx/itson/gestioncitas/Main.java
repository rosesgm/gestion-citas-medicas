package mx.itson.gestioncitas;


import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.*;
import mx.itson.gestioncitas.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ── Repositories ────────────────────────────────────────────────────
        ICitaRepository          citaRepo    = new CitaRepository();
        IUsuarioGoogleRepository  usuarioRepo = new UsuarioGoogleRepository();
        IGoogleTokenRepository    tokenRepo   = new GoogleTokenRepository();
        ICitaSyncGoogleRepository syncRepo    = new CitaSyncGoogleRepository();

        // ── Services base ───────────────────────────────────────────────────
        DisponibilidadService disponibilidadSvc = new DisponibilidadService(citaRepo);

        // ── Autenticación Google ─────────────────────────────────────────────
        // GoogleOAuthService maneja el flujo OAuth2 (abre el navegador)
        GoogleOAuthService oauthSvc = new GoogleOAuthService(usuarioRepo, tokenRepo);

        // GoogleAuthService decide si reutiliza el token guardado o pide login
        GoogleAuthService authSvc = new GoogleAuthService(oauthSvc, usuarioRepo, tokenRepo);

        UsuarioGoogle usuarioGoogle = null;
        CalendarSyncService calendarSyncSvc = null;

        separador("INICIO DE SESIÓN CON GOOGLE");
        try {
            // Intenta reutilizar sesión existente; si no, abre el navegador
            // Cambia null por el correo del usuario para reutilizar tokens: "tucorreo@gmail.com"
            usuarioGoogle = authSvc.autenticar(null);
            System.out.println("✔ Sesión activa: " + usuarioGoogle.getNombre()
                    + " <" + usuarioGoogle.getCorreo() + ">");

            // Construye los services de Calendar con la sesión activa
            GoogleCalendarService calendarSvc = new GoogleCalendarService(authSvc);
            calendarSyncSvc = new CalendarSyncService(calendarSvc, syncRepo, usuarioGoogle);

        } catch (Exception e) {
            System.out.println("✘ Login con Google falló: " + e.getMessage());
            System.out.println("  → Las citas se guardarán solo en BD local.");
        }

        // ── AgendarCitaService ───────────────────────────────────────────────
        // Pasa el calendarSyncSvc (puede ser null si el login falló)
        AgendarCitaService agendarSvc = new AgendarCitaService(
                citaRepo, disponibilidadSvc, calendarSyncSvc);

        // ── Datos de prueba ──────────────────────────────────────────────────
        Paciente paciente = new Paciente(1, "Ana García", "ana.garcia@correo.com", "6221001001");
        Medico   medico   = new Medico(1, "Dr. Carlos Ramírez", "Cardiología", "Consultorio 101", true);
        LocalDate fecha   = LocalDate.now().plusDays(3);
        LocalTime hora    = LocalTime.of(10, 30);

        separador("AGENDAR CITA + SYNC A GOOGLE CALENDAR");
        try {
            Cita cita = agendarSvc.agendarCita(paciente, medico, fecha, hora, "Revisión general");
            System.out.println("✔ Cita agendada: " + cita);
        } catch (Exception e) {
            System.out.println("✘ Error al agendar: " + e.getMessage());
        }

        separador("LISTAR CITAS DEL PACIENTE");
        List<Cita> citas = agendarSvc.listarCitasPorPaciente(paciente.getId());
        if (citas.isEmpty()) {
            System.out.println("Sin citas registradas.");
        } else {
            citas.forEach(c -> System.out.println("  → " + c));
        }

        separador("CANCELAR CITA + ELIMINAR EVENTO DE CALENDAR");
        if (!citas.isEmpty()) {
            int idACancelar = citas.get(0).getId();
            try {
                agendarSvc.cancelarCita(idACancelar);
                System.out.println("✔ Cita " + idACancelar + " cancelada.");
            } catch (Exception e) {
                System.out.println("✘ Error al cancelar: " + e.getMessage());
            }
        }

        separador("FIN");
    }

    private static void separador(String titulo) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  " + titulo);
        System.out.println("══════════════════════════════════════════");
    }
}
