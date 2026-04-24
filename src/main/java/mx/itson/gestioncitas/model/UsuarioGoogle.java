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
public class UsuarioGoogle {

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
     * @return the googleSub
     */
    public String getGoogleSub() {
        return googleSub;
    }

    /**
     * @param googleSub the googleSub to set
     */
    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the correo
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * @param correo the correo to set
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * @return the fotoUrl
     */
    public String getFotoUrl() {
        return fotoUrl;
    }

    /**
     * @param fotoUrl the fotoUrl to set
     */
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    /**
     * @return the creadoEn
     */
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    /**
     * @param creadoEn the creadoEn to set
     */
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    private int id;
    private String googleSub;
    private String nombre;
    private String correo;
    private String fotoUrl;
    private LocalDateTime creadoEn;
    
    public UsuarioGoogle(){
    }
    public UsuarioGoogle(String googleSub, String nombre, String correo, String fotoUrl) {
        this.googleSub = googleSub;
        this.nombre = nombre;
        this.correo = correo;
        this.fotoUrl = fotoUrl;
    }

    public UsuarioGoogle(int id, String googleSub, String nombre, String correo, String fotoUrl, LocalDateTime creadoEn) {
        this.id = id;
        this.googleSub = googleSub;
        this.nombre = nombre;
        this.correo = correo;
        this.fotoUrl = fotoUrl;
        this.creadoEn = creadoEn;
    }
    @Override
    public String toString() {
        return "UsuarioGoogle{" +
                "id=" + id +
                ", googleSub='" + googleSub + '\'' +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", fotoUrl='" + fotoUrl + '\'' +
                ", creadoEn=" + creadoEn +
                '}';
    }

}
