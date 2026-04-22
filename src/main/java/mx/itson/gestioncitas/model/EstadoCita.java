package mx.itson.gestioncitas.model;

/**
 * Estados del ciclo de vida de una Cita.
 * PROGRAMADA → CONFIRMADA → EN_CURSO → FINALIZADA
 *                         ↘ CANCELADA
 */
public enum EstadoCita {
    PROGRAMADA,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA;

    /**
     * Valida que la transición de estado sea legal según el diagrama de estado.
     *
     * @param siguiente estado destino
     * @return true si la transición es permitida
     */
    public boolean puedeTransicionarA(EstadoCita siguiente) {
        return switch (this) {
            case PROGRAMADA -> siguiente == CONFIRMADA || siguiente == CANCELADA;
            case CONFIRMADA -> siguiente == EN_CURSO   || siguiente == CANCELADA;
            case EN_CURSO   -> siguiente == FINALIZADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }
}