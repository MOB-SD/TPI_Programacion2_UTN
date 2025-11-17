package prog2int.Service;

import prog2int.Dao.PropiedadDAO;
import prog2int.Models.Propiedad;
import prog2int.Config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de negocio para Propiedad.
 * Adaptado de PersonaServiceImpl.
 * Maneja la lógica de negocio y las transacciones que coordinan
 * Propiedad y EscrituraNotarial.
 */
public class PropiedadServiceImpl implements GenericService<Propiedad> {

    private final PropiedadDAO propiedadDAO;
    private final EscrituraServiceImpl escrituraService;

    public PropiedadServiceImpl(PropiedadDAO propiedadDAO, EscrituraServiceImpl escrituraService) {
        if (propiedadDAO == null) {
            throw new IllegalArgumentException("PropiedadDAO no puede ser null");
        }
        if (escrituraService == null) {
            throw new IllegalArgumentException("EscrituraService no puede ser null");
        }
        this.propiedadDAO = propiedadDAO;
        this.escrituraService = escrituraService;
    }

    /**
     * Inserta una Propiedad y su Escritura asociada en una sola transacción.
     * Esta es la lógica central de tu 'RegistroService' original,
     * pero adaptada a la nueva arquitectura.
     */
    @Override
    public void insertar(Propiedad propiedad) throws Exception {
        validatePropiedad(propiedad);
        validatePadronUnique(propiedad.getPadronCatastral(), null);

        // Gestión de transacción manual (como en tu RegistroService)
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insertar la Propiedad
                propiedadDAO.insertTx(propiedad, conn);

                // 2. Si tiene escritura, vincularla e insertarla
                if (propiedad.getEscritura() != null) {
                    // Vinculamos la escritura a la propiedad (para el FK)
                    propiedad.getEscritura().setPropiedad(propiedad);
                    escrituraService.insertarTx(propiedad.getEscritura(), conn);
                }
                
                conn.commit(); // Confirmar transacción
                
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException sup) { e.addSuppressed(sup); }
                throw new Exception("Error en la transacción de alta: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void actualizar(Propiedad propiedad) throws Exception {
        validatePropiedad(propiedad);
        if (propiedad.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la propiedad debe ser mayor a 0 para actualizar");
        }
        validatePadronUnique(propiedad.getPadronCatastral(), propiedad.getId());

        // Actualizar la propiedad y su escritura asociada es una transacción
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Actualizar Propiedad
                propiedadDAO.updateTx(propiedad, conn);

                // 2. Actualizar Escritura
                if (propiedad.getEscritura() != null) {
                    // Asumimos que la escritura también se quiere actualizar
                    // (En un sistema real, esto podría ser más complejo)
                    propiedad.getEscritura().setPropiedad(propiedad);
                    escrituraService.actualizar(propiedad.getEscritura()); // actualizar usa su propia conex/tx
                }
                
                conn.commit();
                
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException sup) { e.addSuppressed(sup); }
                throw new Exception("Error en la transacción de actualización: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Eliminación lógica (soft delete) de la Propiedad y su Escritura asociada.
     * Ambas operaciones ocurren en una sola transacción.
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }

        Propiedad p = propiedadDAO.getById(id);
        if (p == null) {
            throw new IllegalArgumentException("Propiedad no encontrada con ID: " + id);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Eliminar Escritura (si existe)
                if (p.getEscritura() != null) {
                    escrituraService.eliminarTx(p.getEscritura().getId(), conn);
                }
                
                // 2. Eliminar Propiedad
                propiedadDAO.deleteTx(id, conn);
                
                conn.commit();
                
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException sup) { e.addSuppressed(sup); }
                throw new Exception("Error en la transacción de eliminación: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Propiedad getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return propiedadDAO.getById(id);
    }

    @Override
    public List<Propiedad> getAll() throws Exception {
        return propiedadDAO.getAll();
    }
    
    // --- Métodos Específicos ---

    public EscrituraServiceImpl getEscrituraService() {
        return this.escrituraService;
    }

    public List<Propiedad> buscarPorPadron(String filtro) throws Exception {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        return propiedadDAO.buscarPorPadron(filtro);
    }

    // --- Métodos de Validación ---

    private void validatePropiedad(Propiedad p) {
        if (p == null) {
            throw new IllegalArgumentException("La propiedad no puede ser null");
        }
        if (p.getPadronCatastral() == null || p.getPadronCatastral().trim().isEmpty()) {
            throw new IllegalArgumentException("El padrón catastral no puede estar vacío");
        }
        if (p.getDireccion() == null || p.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }
        if (p.getDestino() == null || p.getDestino().trim().isEmpty()) {
            throw new IllegalArgumentException("El destino no puede estar vacío");
        }
    }

    private void validatePadronUnique(String padron, Integer propiedadId) throws Exception {
        Propiedad existente = propiedadDAO.buscarPorPadronExacto(padron);
        if (existente != null) {
            if (propiedadId == null || existente.getId() != propiedadId) {
                throw new IllegalArgumentException("Ya existe una propiedad con el padrón: " + padron);
            }
        }
    }
}