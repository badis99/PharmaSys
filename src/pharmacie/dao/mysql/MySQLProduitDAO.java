package pharmacie.dao.mysql;

import pharmacie.dao.interfaces.ProduitDAO;
import pharmacie.model.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLProduitDAO extends AbstractMySQLDAO implements ProduitDAO {

    private Produit mapResultSet(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getLong("id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setPrixAchat(rs.getBigDecimal("prix_achat"));
        p.setPrixVente(rs.getBigDecimal("prix_vente"));
        p.setStockActuel(rs.getInt("stock_actuel"));
        p.setSeuilMin(rs.getInt("seuil_min"));
        p.setCodeBarre(rs.getString("code_barre"));
        return p;
    }

    @Override
    public Optional<Produit> findById(Long id) {
        String sql = "SELECT * FROM produit WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Produit> findAll() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(Produit entity) {
        if (entity.getId() == null) {
            String sql = "INSERT INTO produit (nom, description, prix_achat, prix_vente, stock_actuel, seuil_min, code_barre) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getDescription());
                stmt.setBigDecimal(3, entity.getPrixAchat());
                stmt.setBigDecimal(4, entity.getPrixVente());
                stmt.setInt(5, entity.getStockActuel());
                stmt.setInt(6, entity.getSeuilMin());
                stmt.setString(7, entity.getCodeBarre());
                stmt.executeUpdate();
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next())
                        entity.setId(gk.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE produit SET nom=?, description=?, prix_achat=?, prix_vente=?, stock_actuel=?, seuil_min=?, code_barre=? WHERE id=?";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getDescription());
                stmt.setBigDecimal(3, entity.getPrixAchat());
                stmt.setBigDecimal(4, entity.getPrixVente());
                stmt.setInt(5, entity.getStockActuel());
                stmt.setInt(6, entity.getSeuilMin());
                stmt.setString(7, entity.getCodeBarre());
                stmt.setLong(8, entity.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(Long id) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Check current stock
            String checkSql = "SELECT stock_actuel, nom FROM produit WHERE id = ?";
            String productName = "";
            int stock = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setLong(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        stock = rs.getInt("stock_actuel");
                        productName = rs.getString("nom");
                    }
                }
            }

            if (stock > 0) {
                throw new SQLException("Impossible de supprimer '" + productName + "' car il reste " + stock
                        + " unités en stock. Veuillez vider le stock d'abord.");
            }

            // 2. Cascade delete from history
            String delLinesVente = "DELETE FROM ligne_vente WHERE produit_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(delLinesVente)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            String delLinesCmd = "DELETE FROM ligne_commande WHERE produit_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(delLinesCmd)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            // 3. Delete the product itself
            String delSql = "DELETE FROM produit WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(delSql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Optional<Produit> findByCodeBarre(String codeBarre) {
        String sql = "SELECT * FROM produit WHERE code_barre = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeBarre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Produit> findLowStock() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produit WHERE stock_actuel < seuil_min";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
