package prog2int.Service;

import java.util.List;

/**
 * Interfaz genérica para los servicios de negocio.
 * Define las operaciones CRUD estándar que debe proveer un servicio.
 */
public interface GenericService<T> {
    void insertar(T entidad) throws Exception;
    void actualizar(T entidad) throws Exception;
    void eliminar(int id) throws Exception;
    T getById(int id) throws Exception;
    List<T> getAll() throws Exception;
}