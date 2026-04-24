package mx.itson.gestioncitas.ui;

import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.service.AgendarCitaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Menú interactivo de consola para el admin/recepcionista.
 * Toda la lógica de negocio se delega a AgendarCitaService.
 */
public class Menu {

    private final AgendarCitaService agendarService;
    private final UsuarioGoogle      adminActual;
    private final Scanner            sc = new Scanner(System.in);

    public Menu(AgendarCitaService agendarService, UsuarioGoogle adminActual) {
        this.agendarService = agendarService;
        this.adminActual    = adminActual;
    }

    /** Inicia el loop principal del menú. */
    public void iniciar() {
        boolean activo = true;
        while (activo) {
            imprimirMenu();
            String opcion = sc.nextLine().trim();
            System.out.println();
            switch (opcion) {
                case "1" -> agendarCita();
                case "2" -> listarTodasLasCitas();
                case "3" -> cancelarCita();
                case "4" -> listarCitasPorPaciente();
                case "0" -> activo = false;
                default  -> System.out.println("  Opción no válida. Intenta de nuevo.");
            }
        }
        System.out.println("\n  Sesión cerrada. ¡Hasta luego!");
    }

    // ── opciones del menú ─────────────────────────────────────────────────────

    private void agendarCita() {
        separador("AGENDAR NUEVA CITA");
        try {
            System.out.print("  ID del paciente   : "); int pacId   = leerEntero();
            System.out.print("  Nombre del paciente: "); String pacNombre = sc.nextLine().trim();
            System.out.print("  Correo del paciente: "); String pacCorreo = sc.nextLine().trim();
            System.out.print("  Teléfono del paciente: "); String pacTel = sc.nextLine().trim();

            System.out.print("  ID del médico     : "); int medId   = leerEntero();
            System.out.print("  Nombre del médico : "); String medNombre = sc.nextLine().trim();
            System.out.print("  Especialidad      : "); String medEsp    = sc.nextLine().trim();
            System.out.print("  Consultorio       : "); String medCons   = sc.nextLine().trim();

            System.out.print("  Fecha (YYYY-MM-DD): "); LocalDate fecha = leerFecha();
            System.out.print("  Hora  (HH:MM)     : "); LocalTime hora  = leerHora();
            System.out.print("  Motivo            : "); String motivo   = sc.nextLine().trim();

            Paciente paciente = new Paciente(pacId, pacNombre, pacCorreo, pacTel);
            Medico   medico   = new Medico(medId, medNombre, medEsp, medCons, true);

            Cita cita = agendarService.agendarCita(paciente, medico, fecha, hora, motivo);
            System.out.println("\n  ✔ Cita agendada exitosamente.");
            imprimirCita(cita);

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  ✘ No se pudo agendar: " + e.getMessage());
        }
    }

    private void listarTodasLasCitas() {
        separador("TODAS LAS CITAS");
        List<Cita> citas = agendarService.listarTodas();
        if (citas.isEmpty()) {
            System.out.println("  No hay citas registradas.");
        } else {
            citas.forEach(this::imprimirCita);
        }
    }

    private void cancelarCita() {
        separador("CANCELAR CITA");
        System.out.print("  ID de la cita a cancelar: ");
        try {
            int id = leerEntero();
            agendarService.cancelarCita(id);
            System.out.println("  ✔ Cita " + id + " cancelada.");
        } catch (IllegalStateException e) {
            System.out.println("  ✘ No se pudo cancelar: " + e.getMessage());
        }
    }

    private void listarCitasPorPaciente() {
        separador("CITAS POR PACIENTE");
        System.out.print("  ID del paciente: ");
        try {
            int id = leerEntero();
            List<Cita> citas = agendarService.listarPorPaciente(id);
            if (citas.isEmpty()) {
                System.out.println("  No hay citas para ese paciente.");
            } else {
                citas.forEach(this::imprimirCita);
            }
        } catch (NumberFormatException e) {
            System.out.println("  ID inválido.");
        }
    }

    // ── helpers de consola ────────────────────────────────────────────────────

    private void imprimirMenu() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  Gestión de Citas Médicas");
        if (adminActual != null)
            System.out.println("  Admin: " + adminActual.getNombre());
        System.out.println("══════════════════════════════════════════");
        System.out.println("  1. Agendar cita");
        System.out.println("  2. Ver todas las citas");
        System.out.println("  3. Cancelar cita");
        System.out.println("  4. Ver citas por paciente");
        System.out.println("  0. Salir");
        System.out.print("\n  Opción: ");
    }

    private void imprimirCita(Cita c) {
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  ID      : " + c.getId());
        System.out.println("  Paciente: " + (c.getPaciente() != null ? c.getPaciente().getNombre() : "—"));
        System.out.println("  Médico  : " + (c.getMedico()   != null ? c.getMedico().getNombre()   : "—"));
        System.out.println("  Fecha   : " + c.getFecha() + "  Hora: " + c.getHora());
        System.out.println("  Estado  : " + c.getEstado());
        System.out.println("  Motivo  : " + c.getMotivo());
    }

    private void separador(String titulo) {
        System.out.println("──────────────────────────────────────────");
        System.out.println("  " + titulo);
        System.out.println("──────────────────────────────────────────");
    }

    private int leerEntero() {
        int valor = Integer.parseInt(sc.nextLine().trim());
        return valor;
    }

    private LocalDate leerFecha() {
        try {
            return LocalDate.parse(sc.nextLine().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Usa YYYY-MM-DD.");
        }
    }

    private LocalTime leerHora() {
        try {
            return LocalTime.parse(sc.nextLine().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora inválido. Usa HH:MM.");
        }
    }
}