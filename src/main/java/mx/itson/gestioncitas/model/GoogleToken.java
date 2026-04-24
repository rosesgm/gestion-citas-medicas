/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.model;

import java.time.LocalDateTime;

/**
 *
 * @author rosagabriela
 */
public class GoogleToken {

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the usuarioGoogleId
     */
    public int getUsuarioGoogleId() {
        return usuarioGoogleId;
    }

    /**
     * @param usuarioGoogleId the usuarioGoogleId to set
     */
    public void setUsuarioGoogleId(int usuarioGoogleId) {
        this.usuarioGoogleId = usuarioGoogleId;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return the refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * @param refreshToken the refreshToken to set
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return the tokenType
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * @param tokenType the tokenType to set
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * @return the expiresAt
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return the actualizadoEn
     */
    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    /**
     * @param actualizadoEn the actualizadoEn to set
     */
    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
    private int id;
    private int usuarioGoogleId;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private LocalDateTime expiresAt;
    private String scope;
    private LocalDateTime actualizadoEn;

    public GoogleToken() {
    }
    public GoogleToken(int usuarioGoogleId, String accessToken, String refreshToken, String tokenType,
                       LocalDateTime expiresAt, String scope) {
        this.usuarioGoogleId = usuarioGoogleId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.scope = scope;
    }

    public GoogleToken(int id, int usuarioGoogleId, String accessToken, String refreshToken, String tokenType,
                       LocalDateTime expiresAt, String scope, LocalDateTime actualizadoEn) {
        this.id = id;
        this.usuarioGoogleId = usuarioGoogleId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.scope = scope;
        this.actualizadoEn = actualizadoEn;
    }
    @Override
    public String toString() {
        return "GoogleToken{" +
                "id=" + id +
                ", usuarioGoogleId=" + usuarioGoogleId +
                ", tokenType='" + tokenType + '\'' +
                ", expiresAt=" + expiresAt +
                ", scope='" + scope + '\'' +
                ", actualizadoEn=" + actualizadoEn +
                '}';
    }
}
