package prog2int.Dao;

import prog2int.Models.EscrituraNotarial;
import prog2int.Models.Propiedad;
import prog2int.Config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Propiedad.
 * Adaptado de PersonaDAO.
 * Maneja el LEFT JOIN con Escrituras.
 */
public class PropiedadDAO implements GenericDAO<Propiedad> {

    // El FK está en la otra tabla, así que el INSERT es simple
    private static final String INSERT_SQL = "INSERT INTO Propiedades (id, padronCatastral, direccion, superficieM2, destino, antiguedad, eliminado) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    // El UPDATE también es simple
    private static final String UPDATE_SQL = "UPDATE Propiedades SET padronCatastral = ?, direccion = ?, superficieM2 = ?, destino = ?, antiguedad = ? WHERE id = ?";
    
    // Soft delete
    private static final String DELETE_SQL = "UPDATE Propiedades SET eliminado = TRUE WHERE id = ?";

    // SELECT con LEFT JOIN (invertido respecto a PersonaDAO)
    private static final String SELECT_BASE = "SELECT p.id, p.padronCatastral, p.direccion, p.superficieM2, p.destino, p.antiguedad, p.eliminado, " +
            "e.id AS esc_id, e.nroEscritura, e.fecha, e.notaria, e.tomo, e.folio, e.eliminado AS esc_eliminado " +
            "FROM Propiedades p LEFT JOIN EscrituraNotarial e ON p.id = e.propiedad_id ";

    private static final String SELECT_BY_ID_SQL = SELECT_BASE + "WHERE p.id = ? AND p.eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = SELECT_BASE + "WHERE p.eliminado = FALSE";
    
    private static final String SEARCH_BY_PADRON_SQL = SELECT_BASE + "WHERE p.eliminado = FALSE AND p.padronCatastral LIKE ?";
    
    private static final String SEARCH_BY_PADRON_EXACT_SQL = SELECT_BASE + "WHERE p.eliminado = FALSE AND p.padronCatastral = ?";

    
    // --- Métodos Públicos (No transaccionales) ---

    @Override
    public void insertar(Propiedad entidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // El insert normal debe ser transaccional en este caso
            insertTx(entidad, conn);
        }
    }

    @Override
    public void actualizar(Propiedad entidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateTx(entidad, conn);
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            deleteTx(id, conn);
        }
    }

    @Override
    public Propiedad getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (Propiedad) mapResultSetToPropiedad(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener propiedad por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Propiedad> getAll() throws Exception {
        List<Propiedad> propiedades = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                propiedades.add(mapResultSetToPropiedad(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener todas las propiedades: " + e.getMessage(), e);
        }
        return propiedades;
    }

    // --- Métodos de Búsqueda Específicos ---

    public List<Propiedad> buscarPorPadron(String filtro) throws Exception {
        List<Propiedad> propiedades = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_PADRON_SQL)) {
            String searchPattern = "%" + filtro.trim() + "%";
            stmt.setString(1, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    propiedades.add(mapResultSetToPropiedad(rs));
                }
            }
        }
        return propiedades;
    }

    public Propiedad buscarPorPadronExacto(String padron) throws Exception {
        if (padron == null || padron.trim().isEmpty()) {
            throw new IllegalArgumentException("El padrón no puede estar vacío");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_PADRON_EXACT_SQL)) {
            stmt.setString(1, padron.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPropiedad(rs);
                }
            }
        }
        return null;
    }


    // --- Métodos Transaccionales (Tx) ---

    @Override
    public void insertTx(Propiedad entidad, Connection conn) throws Exception {
        // Usamos el ID del modelo, como en el proyecto original
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            setPropiedadParameters(stmt, entidad);
            stmt.executeUpdate();
            // No obtenemos ID generado, usamos el que vino por parámetro.
        }
    }

    @Override
    public void updateTx(Propiedad entidad, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, entidad.getPadronCatastral());
            stmt.setString(2, entidad.getDireccion());
            stmt.setDouble(3, entidad.getSuperficieM2());
            stmt.setString(4, entidad.getDestino());
            stmt.setInt(5, entidad.getAntiguedad());
            stmt.setInt(6, entidad.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar la propiedad con ID: " + entidad.getId());
            }
        }
    }

    @Override
    public void deleteTx(int id, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró propiedad con ID: " + id);
            }
        }
    }

    // --- Métodos Auxiliares ---

    private void setPropiedadParameters(PreparedStatement stmt, Propiedad p) throws SQLException {
        stmt.setLong(1, p.getId()); // Asumimos que el ID viene del Gestor
        stmt.setString(2, p.getPadronCatastral());
        stmt.setString(3, p.getDireccion());
        stmt.setDouble(4, p.getSuperficieM2());
        stmt.setString(5, p.getDestino());
        stmt.setInt(6, p.getAntiguedad());
        stmt.setBoolean(7, p.isEliminado());
    }

    private Propiedad mapResultSetToPropiedad(ResultSet rs) throws SQLException {
        Propiedad p = new Propiedad(
            rs.getInt("id"),
            rs.getBoolean("eliminado"),
            rs.getString("padronCatastral"),
            rs.getString("direccion"),
            rs.getDouble("superficieM2"),
            rs.getString("destino"),
            rs.getInt("antiguedad")
        );

        // Manejo del LEFT JOIN
        int escrituraId = rs.getInt("esc_id");
        if (escrituraId > 0 && !rs.wasNull() && !rs.getBoolean("esc_eliminado")) {
            EscrituraNotarial e = new EscrituraNotarial(
                escrituraId,
                false, // Ya filtramos por esc_eliminado
                rs.getString("nroEscritura"),
                rs.getDate("fecha"),
                rs.getString("notaria"),
                rs.getString("tomo"),
                rs.getString("folio")
            );
            e.setPropiedad(p); // Vinculamos la escritura a su propiedad
            p.setEscritura(e); // Vinculamos la propiedad a su escritura
        }
        return p;
    }
}