/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.GoogleToken;

import java.util.Optional;

/**
 * Contrato para persistencia de tokens OAuth2 de Google.
 */
public interface IGoogleTokenRepository {

    /**
     * Guarda o reemplaza el token de un usuario (upsert por usuario_google_id).
     *
     * @param token token a persistir
     * @return token con su ID asignado
     */
    GoogleToken guardarOActualizar(GoogleToken token);

    /**
     * Busca el token más reciente de un usuario Google.
     *
     * @param usuarioGoogleId ID del usuario en la tabla usuarios_google
     * @return Optional con el token si existe
     */
    Optional<GoogleToken> buscarPorUsuario(int usuarioGoogleId);

    /**
     * Elimina el token de un usuario (logout / revocación).
     *
     * @param usuarioGoogleId ID del usuario
     */
    void eliminarPorUsuario(int usuarioGoogleId);
}