package prog2int.Models;

import prog2int.Models.EscrituraNotarial;
import java.util.Objects;

public class Propiedad extends EntidadBase {
    private String padronCatastral;
    private String direccion;
    private double superficieM2;
    private String destino; // 'RES' o 'COM'
    private int antiguedad;

    // Relación 1:1 (igual que Persona -> Domicilio)
    private EscrituraNotarial escritura;

    // Constructor completo
    public Propiedad(int id, boolean eliminado, String padronCatastral, String direccion, double superficieM2, String destino, int antiguedad) {
        super(id, eliminado);
        this.padronCatastral = padronCatastral;
        this.direccion = direccion;
        this.superficieM2 = superficieM2;
        this.destino = destino;
        this.antiguedad = antiguedad;
    }
    
    // Constructor para nuevos objetos (sin ID)
    public Propiedad(String padronCatastral, String direccion, double superficieM2, String destino, int antiguedad) {
        super(0, false);
        this.padronCatastral = padronCatastral;
        this.direccion = direccion;
        this.superficieM2 = superficieM2;
        this.destino = destino;
        this.antiguedad = antiguedad;
    }

    // Getters y Setters
    public String getPadronCatastral() {
        return padronCatastral;
    }

    public void setPadronCatastral(String padronCatastral) {
        this.padronCatastral = padronCatastral;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public double getSuperficieM2() {
        return superficieM2;
    }

    public void setSuperficieM2(double superficieM2) {
        this.superficieM2 = superficieM2;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public int getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(int antiguedad) {
        this.antiguedad = antiguedad;
    }

    public EscrituraNotarial getEscritura() {
        return escritura;
    }

    public void setEscritura(EscrituraNotarial escritura) {
        this.escritura = escritura;
    }

    @Override
    public String toString() {
        String escrStr = (escritura != null) ? escritura.getNroEscritura() : "N/A";
        return "Propiedad{" + "id=" + id + ", padron='" + padronCatastral + '\'' +
                ", direccion='" + direccion + '\'' + ", escritura=" + escrStr +
                ", eliminado=" + eliminado + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propiedad propiedad = (Propiedad) o;
        return Objects.equals(padronCatastral, propiedad.padronCatastral);
    }

    @Override
    public int hashCode() {
        return Objects.hash(padronCatastral);
    }
}