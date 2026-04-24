/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.GoogleToken;

import java.sql.*;
import java.util.Optional;

/**
 * Implementación JDBC de IGoogleTokenRepository.
 * Maneja la tabla `google_tokens`.
 */
public class GoogleTokenRepository implements IGoogleTokenRepository {

    @Override
    public GoogleToken guardarOActualizar(GoogleToken token) {
        String sql = """
                INSERT INTO google_tokens
                    (usuario_google_id, access_token, refresh_token, token_type, expires_at, scope)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    access_token  = VALUES(access_token),
                    refresh_token = IF(VALUES(refresh_token) IS NOT NULL, VALUES(refresh_token), refresh_token),
                    token_type    = VALUES(token_type),
                    expires_at    = VALUES(expires_at),
                    scope         = VALUES(scope),
                    actualizado_en = CURRENT_TIMESTAMP
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, token.getUsuarioGoogleId());
            ps.setString(2, token.getAccessToken());
            ps.setString(3, token.getRefreshToken());
            ps.setString(4, token.getTokenType() != null ? token.getTokenType() : "Bearer");
            ps.setTimestamp(5, token.getExpiresAt() != null
                    ? Timestamp.valueOf(token.getExpiresAt()) : null);
            ps.setString(6, token.getScope());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    token.setId(rs.getInt(1));
                }
            }

            if (token.getId() == 0) {
                return buscarPorUsuario(token.getUsuarioGoogleId()).orElse(token);
            }

            return token;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar/actualizar GoogleToken: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<GoogleToken> buscarPorUsuario(int usuarioGoogleId) {
        String sql = """
                SELECT id, usuario_google_id, access_token, refresh_token,
                       token_type, expires_at, scope, actualizado_en
                FROM google_tokens
                WHERE usuario_google_id = ?
                ORDER BY actualizado_en DESC
                LIMIT 1
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioGoogleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar GoogleToken: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void eliminarPorUsuario(int usuarioGoogleId) {
        String sql = "DELETE FROM google_tokens WHERE usuario_google_id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioGoogleId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar GoogleToken: " + e.getMessage(), e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GoogleToken mapear(ResultSet rs) throws SQLException {
        return new GoogleToken(
                rs.getInt("id"),
                rs.getInt("usuario_google_id"),
                rs.getString("access_token"),
                rs.getString("refresh_token"),
                rs.getString("token_type"),
                rs.getTimestamp("expires_at") != null
                        ? rs.getTimestamp("expires_at").toLocalDateTime() : null,
                rs.getString("scope"),
                rs.getTimestamp("actualizado_en") != null
                        ? rs.getTimestamp("actualizado_en").toLocalDateTime() : null
        );
    }
}