package mx.itson.gestioncitas.model;

public class Paciente {

    private int    id;
    private String nombre;
    private String correo;
    private String telefono;

    public Paciente() {}

    public Paciente(int id, String nombre, String correo, String telefono) {
        this.id       = id;
        this.nombre   = nombre;
        this.correo   = correo;
        this.telefono = telefono;
    }

    public int    getId()                { return id; }
    public void   setId(int id)          { this.id = id; }

    public String getNombre()            { return nombre; }
    public void   setNombre(String n)    { this.nombre = n; }

    public String getCorreo()            { return correo; }
    public void   setCorreo(String c)    { this.correo = c; }

    public String getTelefono()          { return telefono; }
    public void   setTelefono(String t)  { this.telefono = t; }

    @Override
    public String toString() {
        return "Paciente{id=" + id + ", nombre='" + nombre + "', telefono='" + telefono + "'}";
    }
}