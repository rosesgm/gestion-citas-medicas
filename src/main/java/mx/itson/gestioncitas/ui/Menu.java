package mx.itson.gestioncitas.ui;

import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.IMedicoRepository;
import mx.itson.gestioncitas.repository.IPacienteRepository;
import mx.itson.gestioncitas.service.AgendarCitaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Menú interactivo de consola para el recepcionista/admin. */
public class Menu {

    private final AgendarCitaService  agendarService;
    private final IPacienteRepository pacienteRepo;
    private final IMedicoRepository   medicoRepo;
    private final UsuarioGoogle       adminActual;
    private final Scanner             sc = new Scanner(System.in);

    public Menu(AgendarCitaService agendarService,
                IPacienteRepository pacienteRepo,
                IMedicoRepository medicoRepo,
                UsuarioGoogle adminActual) {
        this.agendarService = agendarService;
        this.pacienteRepo   = pacienteRepo;
        this.medicoRepo     = medicoRepo;
        this.adminActual    = adminActual;
    }

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

    private void agendarCita() {
        separador("AGENDAR NUEVA CITA");
        try {
            List<Paciente> pacientes = pacienteRepo.listarTodos();
            if (pacientes.isEmpty()) {
                System.out.println("  ✘ No hay pacientes registrados en el sistema.");
                return;
            }
            System.out.println("  Pacientes registrados:");
            System.out.printf("  %-4s  %-25s  %-30s  %s%n", "ID", "Nombre", "Correo", "Teléfono");
            System.out.println("  " + "─".repeat(75));
            pacientes.forEach(p -> System.out.printf(
                    "  %-4d  %-25s  %-30s  %s%n",
                    p.getId(), p.getNombre(), p.getCorreo(), p.getTelefono()));

            System.out.print("\n  ID del paciente: ");
            int pacienteId = leerEntero();

            Optional<Paciente> pacienteOpt = pacienteRepo.buscarPorId(pacienteId);
            if (pacienteOpt.isEmpty()) {
                System.out.println("  ✘ No existe un paciente con ID " + pacienteId + ".");
                return;
            }
            Paciente paciente = pacienteOpt.get();
            System.out.println("  ✔ Paciente seleccionado: " + paciente.getNombre());

            List<Medico> medicos = medicoRepo.listarDisponibles();
            if (medicos.isEmpty()) {
                System.out.println("  ✘ No hay médicos disponibles en este momento.");
                return;
            }
            System.out.println("\n  Médicos disponibles:");
            System.out.printf("  %-4s  %-25s  %-20s  %s%n", "ID", "Nombre", "Especialidad", "Consultorio");
            System.out.println("  " + "─".repeat(70));
            medicos.forEach(m -> System.out.printf(
                    "  %-4d  %-25s  %-20s  %s%n",
                    m.getId(), m.getNombre(), m.getEspecialidad(), m.getConsultorio()));

            System.out.print("\n  ID del médico: ");
            int medicoId = leerEntero();

            Optional<Medico> medicoOpt = medicoRepo.buscarPorId(medicoId);
            if (medicoOpt.isEmpty()) {
                System.out.println("  ✘ No existe un médico con ID " + medicoId + ".");
                return;
            }
            Medico medico = medicoOpt.get();
            System.out.println("  ✔ Médico seleccionado: " + medico.getNombre()
                    + "  —  " + medico.getEspecialidad());

            System.out.print("\n  Fecha (YYYY-MM-DD): ");
            LocalDate fecha = leerFecha();

            System.out.print("  Hora  (HH:MM)     : ");
            LocalTime hora  = leerHora();

            System.out.print("  Motivo            : ");
            String motivo   = sc.nextLine().trim();

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
        List<Cita> citas = agendarService.listarTodas();
        if (citas.isEmpty()) {
            System.out.println("  No hay citas registradas.");
            return;
        }
        System.out.println("  Citas activas:");
        citas.stream()
                .filter(c -> !c.getEstado().name().equals("CANCELADA")
                        && !c.getEstado().name().equals("FINALIZADA"))
                .forEach(this::imprimirCita);

        System.out.print("\n  ID de la cita a cancelar: ");
        try {
            int id = leerEntero();
            agendarService.cancelarCita(id);
            System.out.println("  ✔ Cita " + id + " cancelada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  ✘ No se pudo cancelar: " + e.getMessage());
        }
    }

    private void listarCitasPorPaciente() {
        separador("CITAS POR PACIENTE");
        List<Paciente> pacientes = pacienteRepo.listarTodos();
        if (pacientes.isEmpty()) {
            System.out.println("  No hay pacientes registrados.");
            return;
        }
        System.out.printf("  %-4s  %-25s  %-30s%n", "ID", "Nombre", "Correo");
        System.out.println("  " + "─".repeat(62));
        pacientes.forEach(p -> System.out.printf(
                "  %-4d  %-25s  %-30s%n",
                p.getId(), p.getNombre(), p.getCorreo()));

        System.out.print("\n  ID del paciente: ");
        try {
            int id = leerEntero();
            List<Cita> citas = agendarService.listarPorPaciente(id);
            if (citas.isEmpty()) {
                System.out.println("  No hay citas para ese paciente.");
            } else {
                System.out.println();
                citas.forEach(this::imprimirCita);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  ID inválido.");
        }
    }

    private void imprimirMenu() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  Gestión de Citas Médicas — ITSON");
        if (adminActual != null)
            System.out.println("  Admin: " + adminActual.getNombre()
                    + "  <" + adminActual.getCorreo() + ">");
        System.out.println("══════════════════════════════════════════");
        System.out.println("  1. Agendar cita");
        System.out.println("  2. Ver todas las citas");
        System.out.println("  3. Cancelar cita");
        System.out.println("  4. Ver citas por paciente");
        System.out.println("  0. Salir");
        System.out.print("\n  Opción: ");
    }

    private void imprimirCita(Cita c) {
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  ID      : " + c.getId());
        System.out.println("  Paciente: " + (c.getPaciente() != null
                ? c.getPaciente().getNombre() + "  <" + c.getPaciente().getCorreo() + ">"
                : "—"));
        System.out.println("  Médico  : " + (c.getMedico() != null
                ? c.getMedico().getNombre() + "  —  " + c.getMedico().getEspecialidad()
                : "—"));
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
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debes ingresar un número entero válido.");
        }
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