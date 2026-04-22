package mx.itson.gestioncitas.model;

public class Medico {

    private int     id;
    private String  nombre;
    private String  especialidad;
    private String  consultorio;
    private boolean disponible;

    public Medico() {}

    public Medico(int id, String nombre, String especialidad, String consultorio, boolean disponible) {
        this.id           = id;
        this.nombre       = nombre;
        this.especialidad = especialidad;
        this.consultorio  = consultorio;
        this.disponible   = disponible;
    }

    public int     getId()                   { return id; }
    public void    setId(int id)             { this.id = id; }

    public String  getNombre()               { return nombre; }
    public void    setNombre(String n)       { this.nombre = n; }

    public String  getEspecialidad()         { return especialidad; }
    public void    setEspecialidad(String e) { this.especialidad = e; }

    public String  getConsultorio()          { return consultorio; }
    public void    setConsultorio(String c)  { this.consultorio = c; }

    public boolean isDisponible()            { return disponible; }
    public void    setDisponible(boolean d)  { this.disponible = d; }

    @Override
    public String toString() {
        return "Medico{id=" + id + ", nombre='" + nombre
                + "', especialidad='" + especialidad + "', disponible=" + disponible + "}";
    }
}