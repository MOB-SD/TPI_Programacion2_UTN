package prog2int.Models;

/**
 * Clase base abstracta para todas las entidades del sistema (adaptada de 'Base').
 * Contiene los atributos comunes: id y eliminado.
 */
public abstract class EntidadBase {
    protected int id;             // Identificador único (cambiado a int para ser consistente)
    protected boolean eliminado;    // Marca lógica de eliminación

    public EntidadBase(int id, boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }

    protected EntidadBase() {
        this.eliminado = false;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}
