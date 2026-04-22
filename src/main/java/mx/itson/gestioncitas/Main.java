package mx.itson.gestioncitas;

import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;
import mx.itson.gestioncitas.repository.CitaRepository;
import mx.itson.gestioncitas.repository.ICitaRepository;
import mx.itson.gestioncitas.service.AgendarCitaService;
import mx.itson.gestioncitas.service.DisponibilidadService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Composition Root — único lugar donde se instancian implementaciones concretas
        ICitaRepository       repo               = new CitaRepository();
        DisponibilidadService disponibilidadSvc  = new DisponibilidadService(repo);
        AgendarCitaService    agendarSvc         = new AgendarCitaService(repo, disponibilidadSvc);

        // Datos de prueba — deben existir en la BD (ver schema.sql)
        Paciente paciente = new Paciente(1, "Ana García",    "ana.garcia@correo.com", "6221001001");
        Medico   medico   = new Medico  (1, "Dr. Carlos Ramírez", "Cardiología", "Consultorio 101", true);
        LocalDate fecha   = LocalDate.now().plusDays(3);
        LocalTime hora    = LocalTime.of(10, 30);

        separador("CASO DE USO: AGENDAR CITA");
        try {
            Cita cita = agendarSvc.agendarCita(paciente, medico, fecha, hora, "Revisión general");
            System.out.println("✔ Cita agendada: " + cita);
        } catch (Exception e) {
            System.out.println("✘ Error al agendar: " + e.getMessage());
        }

        separador("INTENTO DE DOBLE RESERVA EN EL MISMO HORARIO");
        try {
            Paciente otroPaciente = new Paciente(2, "Luis Martínez", "luis@correo.com", "6221002002");
            agendarSvc.agendarCita(otroPaciente, medico, fecha, hora, "Chequeo");
            System.out.println("✔ Segunda cita agendada (esto no debería pasar)");
        } catch (Exception e) {
            System.out.println("✔ Conflicto detectado correctamente: " + e.getMessage());
        }

        separador("CITAS DEL PACIENTE: " + paciente.getNombre());
        List<Cita> citas = agendarSvc.listarCitasPorPaciente(paciente.getId());
        if (citas.isEmpty()) {
            System.out.println("Sin citas registradas.");
        } else {
            citas.forEach(c -> System.out.println("  → " + c));
        }

        separador("CANCELAR CITA");
        if (!citas.isEmpty()) {
            int idACancelar = citas.get(0).getId();
            try {
                agendarSvc.cancelarCita(idACancelar);
                System.out.println("✔ Cita " + idACancelar + " cancelada.");
            } catch (Exception e) {
                System.out.println("✘ Error al cancelar: " + e.getMessage());
            }
        }

        separador("INTENTO DE CANCELAR UNA CITA YA CANCELADA");
        if (!citas.isEmpty()) {
            try {
                agendarSvc.cancelarCita(citas.get(0).getId());
            } catch (Exception e) {
                System.out.println("✔ Transición bloqueada correctamente: " + e.getMessage());
            }
        }

        separador("FIN DE DEMOSTRACIÓN");
    }

    private static void separador(String titulo) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  " + titulo);
        System.out.println("══════════════════════════════════════════");
    }
}