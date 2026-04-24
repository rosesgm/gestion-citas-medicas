/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import java.sql.*;
import mx.itson.gestioncitas.config.ConexionDB;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import java.util.Optional;


/**
 * Implementación JDBC de IUsuarioGoogleRepository.
 * Maneja la tabla `usuarios_google`.
 */

public class UsuarioGoogleRepository implements IUsuarioGoogleRepository {

    @Override
    public UsuarioGoogle guardarOActualizar(UsuarioGoogle usuario) {
        // Upsert: si ya existe el googleSub, actualiza; si no, inserta
        String sql = """
                INSERT INTO usuarios_google (google_sub, nombre, correo, foto_url)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    nombre   = VALUES(nombre),
                    correo   = VALUES(correo),
                    foto_url = VALUES(foto_url)
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getGoogleSub());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getFotoUrl());
            ps.executeUpdate();

            // Si fue INSERT, asigna el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }

            // Si fue UPDATE (ID no generado), lo recuperamos buscando por sub
            if (usuario.getId() == 0) {
                return buscarPorGoogleSub(usuario.getGoogleSub()).orElse(usuario);
            }

            return usuario;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar/actualizar UsuarioGoogle: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UsuarioGoogle> buscarPorGoogleSub(String googleSub) {
        String sql = "SELECT id, google_sub, nombre, correo, foto_url, creado_en FROM usuarios_google WHERE google_sub = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, googleSub);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar UsuarioGoogle por sub: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UsuarioGoogle> buscarPorCorreo(String correo) {
        String sql = "SELECT id, google_sub, nombre, correo, foto_url, creado_en FROM usuarios_google WHERE correo = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar UsuarioGoogle por correo: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UsuarioGoogle mapear(ResultSet rs) throws SQLException {
        return new UsuarioGoogle(
                rs.getInt("id"),
                rs.getString("google_sub"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("foto_url"),
                rs.getTimestamp("creado_en") != null
                        ? rs.getTimestamp("creado_en").toLocalDateTime()
                        : null
        );
    }
}