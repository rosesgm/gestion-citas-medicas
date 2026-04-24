package mx.itson.gestioncitas;

import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.*;
import mx.itson.gestioncitas.service.*;
import mx.itson.gestioncitas.ui.Menu;

import java.util.Scanner;

/**
 * Punto de entrada y Composition Root.
 *
 * Flujo de inicio:
 *  1. El admin elige iniciar sesión con Google o continuar sin Calendar.
 *  2. Si inicia sesión, las citas se sincronizan automáticamente.
 *  3. El menú de consola queda activo hasta que el admin elija salir.
 */
public class Main {

    public static void main(String[] args) {

        // ── Repositorios ──────────────────────────────────────────────────────
        ICitaRepository          citaRepo    = new CitaRepository();
        IUsuarioGoogleRepository  usuarioRepo = new UsuarioGoogleRepository();
        IGoogleTokenRepository    tokenRepo   = new GoogleTokenRepository();
        ICitaSyncGoogleRepository syncRepo    = new CitaSyncGoogleRepository();

        // ── Servicios base ────────────────────────────────────────────────────
        DisponibilidadService disponibilidadSvc = new DisponibilidadService(citaRepo);
        GoogleOAuthService    oauthSvc          = new GoogleOAuthService(usuarioRepo, tokenRepo);
        GoogleAuthService     authSvc           = new GoogleAuthService(oauthSvc, usuarioRepo, tokenRepo);

        // ── Autenticación opcional con Google ─────────────────────────────────
        UsuarioGoogle      adminActual     = null;
        CalendarSyncService calendarSyncSvc = null;

        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  Gestión de Citas Médicas — ITSON");
        System.out.println("══════════════════════════════════════════");
        System.out.print("  ¿Iniciar sesión con Google? (s/n): ");

        Scanner sc = new Scanner(System.in);
        String respuesta = sc.nextLine().trim().toLowerCase();

        if (respuesta.equals("s")) {
            System.out.print("  Correo (Enter para sesión nueva): ");
            String correo = sc.nextLine().trim();

            try {
                adminActual = authSvc.autenticar(correo.isEmpty() ? null : correo);
                System.out.println("  ✔ Sesión activa: " + adminActual.getNombre()
                        + " <" + adminActual.getCorreo() + ">");

                GoogleCalendarService calendarSvc = new GoogleCalendarService(authSvc);
                calendarSyncSvc = new CalendarSyncService(calendarSvc, syncRepo, adminActual);
                System.out.println("  ✔ Google Calendar conectado. Las citas se sincronizarán automáticamente.");

            } catch (Exception e) {
                System.out.println("  ✘ Login con Google falló: " + e.getMessage());
                System.out.println("  → Continuando sin sincronización con Calendar.");
            }
        } else {
            System.out.println("  → Continuando sin Google Calendar.");
        }

        // ── AgendarCitaService con o sin Calendar ─────────────────────────────
        AgendarCitaService agendarSvc = new AgendarCitaService(
                citaRepo, disponibilidadSvc, calendarSyncSvc);

        // ── Menú interactivo ──────────────────────────────────────────────────
        new Menu(agendarSvc, adminActual).iniciar();
    }
}