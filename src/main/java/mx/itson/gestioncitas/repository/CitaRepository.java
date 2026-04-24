package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.Cita;
import mx.itson.gestioncitas.model.EstadoCita;
import mx.itson.gestioncitas.model.Medico;
import mx.itson.gestioncitas.model.Paciente;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CitaRepository implements ICitaRepository {

    @Override
    public Cita guardar(Cita cita) {
        final String sql = """
                INSERT INTO citas (paciente_id, medico_id, fecha, hora, estado, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cita.getPaciente().getId());
                ps.setInt(2, cita.getMedico().getId());
                ps.setDate(3, Date.valueOf(cita.getFecha()));
                ps.setTime(4, Time.valueOf(cita.getHora()));
                ps.setString(5, cita.getEstado().name());
                ps.setString(6, cita.getMotivo());

                if (ps.executeUpdate() == 0) { conn.rollback(); throw new SQLException("No se insertó la cita."); }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cita.setId(keys.getInt(1));
                }
                conn.commit();
                return cita;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la cita: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cita> buscarPorId(int id) {
        final String sql = BASE_SELECT + " WHERE c.id = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearCita(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cita por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Cita> listarPorPaciente(int pacienteId) {
        final String sql = BASE_SELECT + " WHERE c.paciente_id = ? ORDER BY c.fecha, c.hora";
        return ejecutarListado(sql, pacienteId);
    }

    @Override
    public List<Cita> listarTodas() {
        final String sql = BASE_SELECT + " ORDER BY c.fecha, c.hora";
        List<Cita> resultado = new ArrayList<>();
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(mapearCita(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar citas: " + e.getMessage(), e);
        }
        return resultado;
    }

    @Override
    public void actualizarEstado(int citaId, EstadoCita nuevoEstado) {
        final String sql = "UPDATE citas SET estado = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado.name());
                ps.setInt(2, citaId);
                if (ps.executeUpdate() == 0) { conn.rollback(); throw new SQLException("Cita ID " + citaId + " no encontrada."); }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeCitaEnHorario(int medicoId, LocalDate fecha, LocalTime hora) {
        final String sql = """
                SELECT COUNT(*) FROM citas
                WHERE medico_id = ? AND fecha = ? AND hora = ?
                  AND estado NOT IN ('CANCELADA','FINALIZADA')
                """;
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicoId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar horario: " + e.getMessage(), e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static final String BASE_SELECT = """
            SELECT c.id, c.fecha, c.hora, c.estado, c.motivo,
                   p.id AS pac_id, p.nombre AS pac_nombre, p.correo, p.telefono,
                   m.id AS med_id, m.nombre AS med_nombre,
                   m.especialidad_id, m.consultorio, m.disponible
            FROM citas c
            JOIN pacientes p ON c.paciente_id = p.id
            JOIN medicos   m ON c.medico_id   = m.id
            """;

    private List<Cita> ejecutarListado(String sql, int parametro) {
        List<Cita> resultado = new ArrayList<>();
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar citas: " + e.getMessage(), e);
        }
        return resultado;
    }

    private Cita mapearCita(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente(
                rs.getInt("pac_id"), rs.getString("pac_nombre"),
                rs.getString("correo"), rs.getString("telefono"));
        Medico medico = new Medico(
                rs.getInt("med_id"), rs.getString("med_nombre"),
                String.valueOf(rs.getInt("especialidad_id")),
                rs.getString("consultorio"), rs.getBoolean("disponible"));
        Cita cita = new Cita(paciente, medico,
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora").toLocalTime(),
                rs.getString("motivo"));
        cita.setId(rs.getInt("id"));
        cita.setEstado(EstadoCita.valueOf(rs.getString("estado")));
        return cita;
    }
}