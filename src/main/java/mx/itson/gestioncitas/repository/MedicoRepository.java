package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.Medico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicoRepository implements IMedicoRepository {

    // JOIN con especialidades para mostrar el nombre en lugar del ID numérico
    private static final String SELECT_BASE = """
            SELECT m.id, m.nombre, e.nombre AS especialidad,
                   m.consultorio, m.disponible
            FROM medicos m
            JOIN especialidades e ON m.especialidad_id = e.id
            """;

    @Override
    public Optional<Medico> buscarPorId(int id) {
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE m.id = ?")) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar médico: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Medico> listarDisponibles() {
        List<Medico> resultado = new ArrayList<>();
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + " WHERE m.disponible = 1 ORDER BY m.nombre");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) resultado.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar médicos: " + e.getMessage(), e);
        }
        return resultado;
    }

    private Medico mapear(ResultSet rs) throws SQLException {
        return new Medico(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("especialidad"),
                rs.getString("consultorio"),
                rs.getBoolean("disponible")
        );
    }
}