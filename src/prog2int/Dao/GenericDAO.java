package prog2int.Dao;

import java.sql.Connection;
import java.util.List;

/**
 * Interfaz genérica que define métodos CRUD comunes.
 * Incluye métodos transaccionales (...Tx) que reciben una conexión externa.
 */
public interface GenericDAO<T> {
    
    // Métodos no transaccionales (crean su propia conexión)
    void insertar(T entidad) throws Exception;
    void actualizar(T entidad) throws Exception;
    void eliminar(int id) throws Exception;
    T getById(int id) throws Exception;
    List<T> getAll() throws Exception;

    // Métodos transaccionales (usan conexión externa)
    void insertTx(T entidad, Connection conn) throws Exception;
    void updateTx(T entidad, Connection conn) throws Exception;
    void deleteTx(int id, Connection conn) throws Exception;
}
