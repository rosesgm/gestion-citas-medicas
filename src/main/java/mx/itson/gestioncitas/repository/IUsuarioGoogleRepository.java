/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.repository;

import mx.itson.gestioncitas.model.UsuarioGoogle;

import java.util.Optional;

/**
 * Contrato para persistencia de usuarios autenticados con Google.
 */
public interface IUsuarioGoogleRepository {

    /**
     * Guarda un nuevo usuario Google o actualiza sus datos si ya existe (upsert por googleSub).
     *
     * @param usuario datos del usuario
     * @return usuario con su ID asignado
     */
    UsuarioGoogle guardarOActualizar(UsuarioGoogle usuario);

    /**
     * Busca un usuario por su identificador único de Google (campo 'sub' del token).
     *
     * @param googleSub identificador único de Google
     * @return Optional con el usuario si existe
     */
    Optional<UsuarioGoogle> buscarPorGoogleSub(String googleSub);

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo correo del usuario
     * @return Optional con el usuario si existe
     */
    Optional<UsuarioGoogle> buscarPorCorreo(String correo);
}
