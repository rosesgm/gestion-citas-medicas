package mx.itson.gestioncitas;

import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.*;
import mx.itson.gestioncitas.service.*;
import mx.itson.gestioncitas.ui.Menu;

import java.util.Scanner;

/**
 * Punto de entrada y Composition Root.
 * Único lugar donde se instancian las implementaciones concretas;
 * el resto del sistema depende solo de interfaces.
 */
public class Main {

    public static void main(String[] args) {

        ICitaRepository           citaRepo     = new CitaRepository();
        IPacienteRepository       pacienteRepo = new PacienteRepository();
        IMedicoRepository         medicoRepo   = new MedicoRepository();
        IUsuarioGoogleRepository  usuarioRepo  = new UsuarioGoogleRepository();
        IGoogleTokenRepository    tokenRepo    = new GoogleTokenRepository();
        ICitaSyncGoogleRepository syncRepo     = new CitaSyncGoogleRepository();

        DisponibilidadService disponibilidadSvc = new DisponibilidadService(citaRepo);
        GoogleOAuthService    oauthSvc          = new GoogleOAuthService(usuarioRepo, tokenRepo);
        GoogleAuthService     authSvc           = new GoogleAuthService(oauthSvc, usuarioRepo, tokenRepo);

        UsuarioGoogle       adminActual     = null;
        CalendarSyncService calendarSyncSvc = null;

        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  Gestión de Citas Médicas — ITSON");
        System.out.println("══════════════════════════════════════════");
        System.out.print("  ¿Iniciar sesión con Google Calendar? (s/n): ");

        Scanner sc = new Scanner(System.in);
        String respuesta = sc.nextLine().trim().toLowerCase();

        if (respuesta.equals("s")) {
            // Correo vacío → abre navegador para autenticación nueva (OAuth)
            System.out.print("  Correo admin (Enter para sesión nueva): ");
            String correo = sc.nextLine().trim();

            try {
                adminActual = authSvc.autenticar(correo.isEmpty() ? null : correo);
                System.out.println("  ✔ Sesión activa: " + adminActual.getNombre()
                        + "  <" + adminActual.getCorreo() + ">");

                GoogleCalendarService calendarSvc = new GoogleCalendarService(authSvc);
                calendarSyncSvc = new CalendarSyncService(calendarSvc, syncRepo, adminActual);
                System.out.println("  ✔ Google Calendar conectado. Las citas se sincronizarán con "
                        + adminActual.getCorreo() + ".");

            } catch (Exception e) {
                System.out.println("  ✘ Login con Google falló: " + e.getMessage());
                System.out.println("  → Continuando sin sincronización con Calendar.");
            }
        } else {
            System.out.println("  → Continuando sin Google Calendar.");
        }

        // calendarSyncSvc puede ser null; AgendarCitaService lo maneja sin problema
        AgendarCitaService agendarSvc = new AgendarCitaService(
                citaRepo, disponibilidadSvc, calendarSyncSvc);

        new Menu(agendarSvc, pacienteRepo, medicoRepo, adminActual).iniciar();
    }
}