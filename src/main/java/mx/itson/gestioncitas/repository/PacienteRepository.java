package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PacienteRepository implements IPacienteRepository {

    private static final String SELECT_BASE =
            "SELECT id, nombre, correo, telefono FROM pacientes";

    @Override
    public List<Paciente> listarTodos() {
        List<Paciente> resultado = new ArrayList<>();
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY id");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) resultado.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pacientes: " + e.getMessage(), e);
        }
        return resultado;
    }

    @Override
    public Optional<Paciente> buscarPorId(int id) {
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE id = ?")) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar paciente: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("telefono")
        );
    }
}