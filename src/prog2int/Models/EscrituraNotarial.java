package prog2int.Models; // Corregido (antes era 'Models')

import java.sql.Date;
import java.util.Objects;

// La clase EntidadBase debe estar en el mismo paquete prog2int.Models
// Asegúrate de que prog2int/src/prog2int/Models/EntidadBase.java tenga "package prog2int.Models;"
public class EscrituraNotarial extends EntidadBase {
    private String nroEscritura;
    private Date fecha;
    private String notaria;
    private String tomo;
    private String folio;
    
    // Relación inversa (para lógica transaccional)
    // Se necesita importar prog2int.Models.Propiedad, lo cual se corrige en el paso 2
    private Propiedad propiedad; 

    // Constructor completo
    public EscrituraNotarial(int id, boolean eliminado, String nroEscritura, Date fecha, String notaria, String tomo, String folio) {
        super(id, eliminado);
        this.nroEscritura = nroEscritura;
        this.fecha = fecha;
        this.notaria = notaria;
        this.tomo = tomo;
        this.folio = folio;
    }

    // Constructor para nuevos objetos (sin ID)
    public EscrituraNotarial(String nroEscritura, Date fecha, String notaria, String tomo, String folio) {
        super(0, false);
        this.nroEscritura = nroEscritura;
        this.fecha = fecha;
        this.notaria = notaria;
        this.tomo = tomo;
        this.folio = folio;
    }

    // Getters y Setters
    public String getNroEscritura() {
        return nroEscritura;
    }

    public void setNroEscritura(String nroEscritura) {
        this.nroEscritura = nroEscritura;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNotaria() {
        return notaria;
    }

    public void setNotaria(String notaria) {
        this.notaria = notaria;
    }

    public String getTomo() {
        return tomo;
    }

    public void setTomo(String tomo) {
        this.tomo = tomo;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
    }

    @Override
    public String toString() {
        return "EscrituraNotarial{" + "id=" + id + ", nro='" + nroEscritura + '\'' +
                ", fecha=" + fecha + ", notaria='" + notaria + '\'' +
                ", eliminado=" + eliminado + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EscrituraNotarial that = (EscrituraNotarial) o;
        return Objects.equals(nroEscritura, that.nroEscritura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroEscritura);
    }

    // Los métodos getId() duplicados y con error han sido eliminados.
    // Se usará el getId() heredado de EntidadBase.
}