package mx.itson.gestioncitas.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {

    private int        id;
    private Paciente   paciente;
    private Medico     medico;
    private LocalDate  fecha;
    private LocalTime  hora;
    private EstadoCita estado;
    private String     motivo;

    public Cita() {
        this.estado = EstadoCita.PROGRAMADA;
    }

    public Cita(Paciente paciente, Medico medico, LocalDate fecha, LocalTime hora, String motivo) {
        this();
        this.paciente = paciente;
        this.medico   = medico;
        this.fecha    = fecha;
        this.hora     = hora;
        this.motivo   = motivo;
    }

    /** Transiciona a CONFIRMADA.
     * @throws IllegalStateException si la transición no es válida */
    public void confirmar() {
        transicionarA(EstadoCita.CONFIRMADA);
    }

    /** Transiciona a EN_CURSO.
     * @throws IllegalStateException si la transición no es válida */
    public void iniciarConsulta() {
        transicionarA(EstadoCita.EN_CURSO);
    }

    /** Transiciona a FINALIZADA.
     * @throws IllegalStateException si la transición no es válida */
    public void finalizarConsulta() {
        transicionarA(EstadoCita.FINALIZADA);
    }

    /** Transiciona a CANCELADA.
     * @throws IllegalStateException si la transición no es válida */
    public void cancelar() {
        transicionarA(EstadoCita.CANCELADA);
    }

    private void transicionarA(EstadoCita siguiente) {
        if (!estado.puedeTransicionarA(siguiente)) {
            throw new IllegalStateException("Transición inválida: " + estado + " → " + siguiente);
        }
        this.estado = siguiente;
    }

    public int        getId()                 { return id; }
    public void       setId(int id)           { this.id = id; }

    public Paciente   getPaciente()           { return paciente; }
    public void       setPaciente(Paciente p) { this.paciente = p; }

    public Medico     getMedico()             { return medico; }
    public void       setMedico(Medico m)     { this.medico = m; }

    public LocalDate  getFecha()              { return fecha; }
    public void       setFecha(LocalDate f)   { this.fecha = f; }

    public LocalTime  getHora()               { return hora; }
    public void       setHora(LocalTime h)    { this.hora = h; }

    public EstadoCita getEstado()             { return estado; }
    public void       setEstado(EstadoCita e) { this.estado = e; }

    public String     getMotivo()             { return motivo; }
    public void       setMotivo(String m)     { this.motivo = m; }

    @Override
    public String toString() {
        return "Cita{id=" + id
                + ", paciente=" + (paciente != null ? paciente.getNombre() : "null")
                + ", medico="   + (medico   != null ? medico.getNombre()   : "null")
                + ", fecha="    + fecha
                + ", hora="     + hora
                + ", estado="   + estado
                + ", motivo='"  + motivo + "'}";
    }
}