package prog2int.Dao;

import prog2int.Models.EscrituraNotarial;
import prog2int.Config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad EscrituraNotarial.
 * Adaptado de DomicilioDAO.
 */
public class EscrituraDAO implements GenericDAO<EscrituraNotarial> {

    // El FK propiedad_id se inserta aquí
    private static final String INSERT_SQL = "INSERT INTO EscrituraNotarial (id, nroEscritura, fecha, notaria, tomo, folio, propiedad_id, eliminado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = "UPDATE EscrituraNotarial SET nroEscritura = ?, fecha = ?, notaria = ?, tomo = ?, folio = ? WHERE id = ?";
    
    private static final String DELETE_SQL = "UPDATE EscrituraNotarial SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM EscrituraNotarial WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = "SELECT * FROM EscrituraNotarial WHERE eliminado = FALSE";

    
    // --- Métodos Públicos (No transaccionales) ---

    @Override
    public void insertar(EscrituraNotarial entidad) throws Exception {
        // Una escritura no debe insertarse sola, siempre dentro de una transacción
        throw new UnsupportedOperationException("Usar PropiedadServiceImpl.insertar() para crear propiedad y escritura juntas.");
    }

    @Override
    public void actualizar(EscrituraNotarial entidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateTx(entidad, conn);
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        // Una escritura no debe eliminarse sola, siempre con su propiedad
        throw new UnsupportedOperationException("Usar PropiedadServiceImpl.eliminar() para eliminar propiedad y escritura juntas.");
    }

    @Override
    public EscrituraNotarial getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEscritura(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<EscrituraNotarial> getAll() throws Exception {
        List<EscrituraNotarial> escrituras = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                escrituras.add(mapResultSetToEscritura(rs));
            }
        }
        return escrituras;
    }

    // --- Métodos Transaccionales (Tx) ---

    @Override
    public void insertTx(EscrituraNotarial entidad, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            setEscrituraParameters(stmt, entidad);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateTx(EscrituraNotarial entidad, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, entidad.getNroEscritura());
            stmt.setDate(2, entidad.getFecha());
            stmt.setString(3, entidad.getNotaria());
            stmt.setString(4, entidad.getTomo());
            stmt.setString(5, entidad.getFolio());
            stmt.setInt(6, entidad.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar la escritura con ID: " + entidad.getId());
            }
        }
    }

    @Override
    public void deleteTx(int id, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró escritura con ID: " + id);
            }
        }
    }

    // --- Métodos Auxiliares ---

    private void setEscrituraParameters(PreparedStatement stmt, EscrituraNotarial e) throws SQLException {
        if (e.getPropiedad() == null || e.getPropiedad().getId() <= 0) {
            throw new SQLException("No se puede guardar una escritura sin una Propiedad válida.");
        }
        stmt.setLong(1, e.getId()); // Asumimos ID del Gestor
        stmt.setString(2, e.getNroEscritura());
        stmt.setDate(3, e.getFecha());
        stmt.setString(4, e.getNotaria());
        stmt.setString(5, e.getTomo());
        stmt.setString(6, e.getFolio());
        stmt.setInt(7, e.getPropiedad().getId()); // El FK
        stmt.setBoolean(8, e.isEliminado());
    }

    private EscrituraNotarial mapResultSetToEscritura(ResultSet rs) throws SQLException {
        return new EscrituraNotarial(
            rs.getInt("id"),
            rs.getBoolean("eliminado"),
            rs.getString("nroEscritura"),
            rs.getDate("fecha"),
            rs.getString("notaria"),
            rs.getString("tomo"),
            rs.getString("folio")
        );
        // Nota: No cargamos el objeto Propiedad aquí para evitar ciclos
    }
}