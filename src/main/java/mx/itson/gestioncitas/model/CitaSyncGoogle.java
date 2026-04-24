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
public class CitaSyncGoogle {

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
     * @return the citaId
     */
    public int getCitaId() {
        return citaId;
    }

    /**
     * @param citaId the citaId to set
     */
    public void setCitaId(int citaId) {
        this.citaId = citaId;
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
     * @return the googleEventId
     */
    public String getGoogleEventId() {
        return googleEventId;
    }

    /**
     * @param googleEventId the googleEventId to set
     */
    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
    }

    /**
     * @return the estadoSync
     */
    public EstadoSyncGoogle getEstadoSync() {
        return estadoSync;
    }

    /**
     * @param estadoSync the estadoSync to set
     */
    public void setEstadoSync(EstadoSyncGoogle estadoSync) {
        this.estadoSync = estadoSync;
    }

    /**
     * @return the ultimoError
     */
    public String getUltimoError() {
        return ultimoError;
    }

    /**
     * @param ultimoError the ultimoError to set
     */
    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
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
    private int citaId;
    private int usuarioGoogleId;
    private String googleEventId;
    private EstadoSyncGoogle estadoSync;
    private String ultimoError;
    private LocalDateTime actualizadoEn;

    public CitaSyncGoogle() {
    }
    public CitaSyncGoogle(int citaId, int usuarioGoogleId) {
        this.citaId = citaId;
        this.usuarioGoogleId = usuarioGoogleId;
        this.estadoSync = EstadoSyncGoogle.PENDIENTE;
    }

    public CitaSyncGoogle(int id, int citaId, int usuarioGoogleId, String googleEventId,
                          EstadoSyncGoogle estadoSync, String ultimoError, LocalDateTime actualizadoEn) {
        this.id = id;
        this.citaId = citaId;
        this.usuarioGoogleId = usuarioGoogleId;
        this.googleEventId = googleEventId;
        this.estadoSync = estadoSync;
        this.ultimoError = ultimoError;
        this.actualizadoEn = actualizadoEn;
    }

}
