package mx.itson.gestioncitas.model;

import java.time.LocalDateTime;

public class GoogleToken {

    private int           id;
    private int           usuarioGoogleId;
    private String        accessToken;
    private String        refreshToken;
    private String        tokenType;
    private LocalDateTime expiresAt;
    private String        scope;
    private LocalDateTime actualizadoEn;

    public GoogleToken() {}

    public GoogleToken(int usuarioGoogleId, String accessToken, String refreshToken,
                       String tokenType, LocalDateTime expiresAt, String scope) {
        this.usuarioGoogleId = usuarioGoogleId;
        this.accessToken     = accessToken;
        this.refreshToken    = refreshToken;
        this.tokenType       = tokenType;
        this.expiresAt       = expiresAt;
        this.scope           = scope;
    }

    public GoogleToken(int id, int usuarioGoogleId, String accessToken, String refreshToken,
                       String tokenType, LocalDateTime expiresAt, String scope,
                       LocalDateTime actualizadoEn) {
        this(usuarioGoogleId, accessToken, refreshToken, tokenType, expiresAt, scope);
        this.id             = id;
        this.actualizadoEn  = actualizadoEn;
    }

    public int           getId()                      { return id; }
    public void          setId(int id)                { this.id = id; }
    public int           getUsuarioGoogleId()         { return usuarioGoogleId; }
    public void          setUsuarioGoogleId(int v)    { this.usuarioGoogleId = v; }
    public String        getAccessToken()             { return accessToken; }
    public void          setAccessToken(String v)     { this.accessToken = v; }
    public String        getRefreshToken()            { return refreshToken; }
    public void          setRefreshToken(String v)    { this.refreshToken = v; }
    public String        getTokenType()               { return tokenType; }
    public void          setTokenType(String v)       { this.tokenType = v; }
    public LocalDateTime getExpiresAt()               { return expiresAt; }
    public void          setExpiresAt(LocalDateTime v){ this.expiresAt = v; }
    public String        getScope()                   { return scope; }
    public void          setScope(String v)           { this.scope = v; }
    public LocalDateTime getActualizadoEn()           { return actualizadoEn; }
    public void          setActualizadoEn(LocalDateTime v) { this.actualizadoEn = v; }

    @Override
    public String toString() {
        return "GoogleToken{usuarioGoogleId=" + usuarioGoogleId
                + ", tokenType='" + tokenType + "', expiresAt=" + expiresAt + "}";
    }
}