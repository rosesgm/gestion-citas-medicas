package mx.itson.gestioncitas.model;

import java.time.LocalDateTime;

public class CitaSyncGoogle {

    private int             id;
    private int             citaId;
    private int             usuarioGoogleId;
    private String          googleEventId;
    private EstadoSyncGoogle estadoSync;
    private String          ultimoError;
    private LocalDateTime   actualizadoEn;

    public CitaSyncGoogle() {}

    public CitaSyncGoogle(int citaId, int usuarioGoogleId) {
        this.citaId          = citaId;
        this.usuarioGoogleId = usuarioGoogleId;
        this.estadoSync      = EstadoSyncGoogle.PENDIENTE;
    }

    public CitaSyncGoogle(int id, int citaId, int usuarioGoogleId, String googleEventId,
                          EstadoSyncGoogle estadoSync, String ultimoError,
                          LocalDateTime actualizadoEn) {
        this.id              = id;
        this.citaId          = citaId;
        this.usuarioGoogleId = usuarioGoogleId;
        this.googleEventId   = googleEventId;
        this.estadoSync      = estadoSync;
        this.ultimoError     = ultimoError;
        this.actualizadoEn   = actualizadoEn;
    }

    public int              getId()                      { return id; }
    public void             setId(int id)                { this.id = id; }
    public int              getCitaId()                  { return citaId; }
    public void             setCitaId(int v)             { this.citaId = v; }
    public int              getUsuarioGoogleId()         { return usuarioGoogleId; }
    public void             setUsuarioGoogleId(int v)    { this.usuarioGoogleId = v; }
    public String           getGoogleEventId()           { return googleEventId; }
    public void             setGoogleEventId(String v)   { this.googleEventId = v; }
    public EstadoSyncGoogle getEstadoSync()              { return estadoSync; }
    public void             setEstadoSync(EstadoSyncGoogle v) { this.estadoSync = v; }
    public String           getUltimoError()             { return ultimoError; }
    public void             setUltimoError(String v)     { this.ultimoError = v; }
    public LocalDateTime    getActualizadoEn()           { return actualizadoEn; }
    public void             setActualizadoEn(LocalDateTime v) { this.actualizadoEn = v; }
}