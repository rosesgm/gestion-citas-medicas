/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.CitaSyncGoogle;
import mx.itson.gestioncitas.model.EstadoSyncGoogle;

import java.sql.*;
import java.util.Optional;

/**
 * Implementación JDBC de ICitaSyncGoogleRepository.
 * Maneja la tabla `citas_sync_google`.
 */
public class CitaSyncGoogleRepository implements ICitaSyncGoogleRepository {

    @Override
    public CitaSyncGoogle guardar(CitaSyncGoogle sync) {
        String sql = """
                INSERT INTO citas_sync_google (cita_id, usuario_google_id, estado_sync)
                VALUES (?, ?, ?)
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sync.getCitaId());
            ps.setInt(2, sync.getUsuarioGoogleId());
            ps.setString(3, sync.getEstadoSync().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sync.setId(rs.getInt(1));
                }
            }
            return sync;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar CitaSyncGoogle: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarSync(int citaId, String googleEventId,
                               EstadoSyncGoogle estado, String error) {
        String sql = """
                UPDATE citas_sync_google
                SET google_event_id = ?,
                    estado_sync     = ?,
                    ultimo_error    = ?,
                    actualizado_en  = CURRENT_TIMESTAMP
                WHERE cita_id = ?
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, googleEventId);
            ps.setString(2, estado.name());
            ps.setString(3, error);
            ps.setInt(4, citaId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar CitaSyncGoogle: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CitaSyncGoogle> buscarPorCita(int citaId) {
        String sql = """
                SELECT id, cita_id, usuario_google_id, google_event_id,
                       estado_sync, ultimo_error, actualizado_en
                FROM citas_sync_google
                WHERE cita_id = ?
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, citaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar CitaSyncGoogle: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void eliminarPorCita(int citaId) {
        String sql = "DELETE FROM citas_sync_google WHERE cita_id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, citaId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar CitaSyncGoogle: " + e.getMessage(), e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CitaSyncGoogle mapear(ResultSet rs) throws SQLException {
        return new CitaSyncGoogle(
                rs.getInt("id"),
                rs.getInt("cita_id"),
                rs.getInt("usuario_google_id"),
                rs.getString("google_event_id"),
                EstadoSyncGoogle.valueOf(rs.getString("estado_sync")),
                rs.getString("ultimo_error"),
                rs.getTimestamp("actualizado_en") != null
                        ? rs.getTimestamp("actualizado_en").toLocalDateTime() : null
        );
    }
}
