package mx.itson.gestioncitas.model;

import java.time.LocalDateTime;

public class UsuarioGoogle {

    private int           id;
    private String        googleSub;
    private String        nombre;
    private String        correo;
    private String        fotoUrl;
    private LocalDateTime creadoEn;

    public UsuarioGoogle() {}

    public UsuarioGoogle(String googleSub, String nombre, String correo, String fotoUrl) {
        this.googleSub = googleSub;
        this.nombre    = nombre;
        this.correo    = correo;
        this.fotoUrl   = fotoUrl;
    }

    public UsuarioGoogle(int id, String googleSub, String nombre, String correo,
                         String fotoUrl, LocalDateTime creadoEn) {
        this.id        = id;
        this.googleSub = googleSub;
        this.nombre    = nombre;
        this.correo    = correo;
        this.fotoUrl   = fotoUrl;
        this.creadoEn  = creadoEn;
    }

    public int           getId()                   { return id; }
    public void          setId(int id)             { this.id = id; }
    public String        getGoogleSub()            { return googleSub; }
    public void          setGoogleSub(String s)    { this.googleSub = s; }
    public String        getNombre()               { return nombre; }
    public void          setNombre(String n)       { this.nombre = n; }
    public String        getCorreo()               { return correo; }
    public void          setCorreo(String c)       { this.correo = c; }
    public String        getFotoUrl()              { return fotoUrl; }
    public void          setFotoUrl(String f)      { this.fotoUrl = f; }
    public LocalDateTime getCreadoEn()             { return creadoEn; }
    public void          setCreadoEn(LocalDateTime d) { this.creadoEn = d; }

    @Override
    public String toString() {
        return "UsuarioGoogle{id=" + id + ", nombre='" + nombre + "', correo='" + correo + "'}";
    }
}