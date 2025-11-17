package prog2int.Service;

import prog2int.Models.EscrituraNotarial;
import prog2int.Dao.GenericDAO;
import java.util.List;

/**
 * Servicio de negocio para EscrituraNotarial.
 * Adaptado de DomicilioServiceImpl.
 */
public class EscrituraServiceImpl implements GenericService<EscrituraNotarial> {

    private final GenericDAO<EscrituraNotarial> escrituraDAO;

    public EscrituraServiceImpl(GenericDAO<EscrituraNotarial> escrituraDAO) {
        if (escrituraDAO == null) {
            throw new IllegalArgumentException("EscrituraDAO no puede ser null");
        }
        this.escrituraDAO = escrituraDAO;
    }

    @Override
    public void insertar(EscrituraNotarial entidad) throws Exception {
        // Lógica de negocio: una escritura no se crea sola.
        throw new UnsupportedOperationException("Usar PropiedadServiceImpl.insertar() para crear propiedad y escritura juntas.");
    }

    // Método transaccional para ser llamado por PropiedadServiceImpl
    public void insertarTx(EscrituraNotarial entidad, java.sql.Connection conn) throws Exception {
        validateEscritura(entidad);
        escrituraDAO.insertTx(entidad, conn);
    }
    
    @Override
    public void actualizar(EscrituraNotarial entidad) throws Exception {
        validateEscritura(entidad);
        if (entidad.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la escritura debe ser mayor a 0 para actualizar");
        }
        escrituraDAO.actualizar(entidad);
    }

    @Override
    public void eliminar(int id) throws Exception {
        // Lógica de negocio: una escritura no se elimina sola.
        throw new UnsupportedOperationException("Usar PropiedadServiceImpl.eliminar() para eliminar propiedad y escritura juntas.");
    }
    
    // Método transaccional para ser llamado por PropiedadServiceImpl
    public void eliminarTx(int id, java.sql.Connection conn) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        escrituraDAO.deleteTx(id, conn);
    }

    @Override
    public EscrituraNotarial getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return escrituraDAO.getById(id);
    }

    @Override
    public List<EscrituraNotarial> getAll() throws Exception {
        return escrituraDAO.getAll();
    }

    private void validateEscritura(EscrituraNotarial e) {
        if (e == null) {
            throw new IllegalArgumentException("La escritura no puede ser null");
        }
        if (e.getNroEscritura() == null || e.getNroEscritura().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de escritura no puede estar vacío");
        }
        if (e.getFecha() == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }
        if (e.getNotaria() == null || e.getNotaria().trim().isEmpty()) {
            throw new IllegalArgumentException("La notaría no puede estar vacía");
        }
    }
}