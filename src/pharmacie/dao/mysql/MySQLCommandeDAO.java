package pharmacie.dao.mysql;

import pharmacie.dao.interfaces.CommandeDAO;
import pharmacie.model.Commande;
import pharmacie.model.Fournisseur;
import pharmacie.model.LigneCommande;
import pharmacie.model.Produit;
import pharmacie.model.StatutCommande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCommandeDAO extends AbstractMySQLDAO implements CommandeDAO {

    // Helper to fetch lines
    private void loadLignes(Commande c, Connection conn) throws SQLException {
        String sql = "SELECT lc.*, p.nom, p.stock_actuel, p.prix_achat, p.prix_vente, p.seuil_min, p.code_barre, p.description "
                +
                "FROM ligne_commande lc " +
                "JOIN produit p ON lc.produit_id = p.id " +
                "WHERE lc.commande_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, c.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produit p = new Produit();
                    p.setId(rs.getLong("produit_id"));
                    p.setNom(rs.getString("nom"));
                    p.setStockActuel(rs.getInt("stock_actuel"));
                    p.setPrixAchat(rs.getBigDecimal("prix_achat"));
                    p.setPrixVente(rs.getBigDecimal("prix_vente"));
                    p.setSeuilMin(rs.getInt("seuil_min"));
                    p.setCodeBarre(rs.getString("code_barre"));
                    p.setDescription(rs.getString("description"));

                    LigneCommande lc = new LigneCommande(p, rs.getInt("quantite"));
                    lc.setId(rs.getLong("id"));
                    c.ajouterLigne(lc);
                }
            }
        }
    }

    private Commande mapResultSet(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getLong("id"));
        c.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        c.setStatut(StatutCommande.valueOf(rs.getString("statut")));

        // Map Fournisseur (partial)
        Fournisseur f = new Fournisseur();
        f.setId(rs.getLong("fournisseur_id"));
        // Ideally we join to get supplier name, but for now we might leave it partial
        // or join in query.
        // Let's assume we want at least the name.
        if (hasColumn(rs, "fournisseur_nom")) {
            f.setNom(rs.getString("fournisseur_nom"));
        }
        c.setFournisseur(f);

        return c;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Commande> findById(Long id) {
        String sql = "SELECT c.*, f.nom as fournisseur_nom FROM commande c " +
                "JOIN fournisseur f ON c.fournisseur_id = f.id WHERE c.id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Commande c = mapResultSet(rs);
                    loadLignes(c, conn);
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Commande> findAll() {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT c.*, f.nom as fournisseur_nom FROM commande c " +
                "JOIN fournisseur f ON c.fournisseur_id = f.id ORDER BY c.date_creation DESC";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Commande c = mapResultSet(rs);
                // Potential N+1 problem, but acceptable for this scale
                loadLignes(c, conn);
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(Commande entity) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Transaction start

            if (entity.getId() == null) {
                // Insert Commande
                String sql = "INSERT INTO commande (fournisseur_id, date_creation, statut) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, entity.getFournisseur().getId());
                    stmt.setTimestamp(2, Timestamp.valueOf(entity.getDateCreation()));
                    stmt.setString(3, entity.getStatut().name());
                    stmt.executeUpdate();
                    try (ResultSet gk = stmt.getGeneratedKeys()) {
                        if (gk.next())
                            entity.setId(gk.getLong(1));
                    }
                }
            } else {
                // Update Commande
                String sql = "UPDATE commande SET fournisseur_id=?, date_creation=?, statut=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, entity.getFournisseur().getId());
                    stmt.setTimestamp(2, Timestamp.valueOf(entity.getDateCreation()));
                    stmt.setString(3, entity.getStatut().name());
                    stmt.setLong(4, entity.getId());
                    stmt.executeUpdate();
                }
                // Delete old lines (simplest strategy)
                String delSql = "DELETE FROM ligne_commande WHERE commande_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(delSql)) {
                    stmt.setLong(1, entity.getId());
                    stmt.executeUpdate();
                }
            }

            // Insert Lines
            String lineSql = "INSERT INTO ligne_commande (commande_id, produit_id, quantite) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(lineSql)) {
                for (LigneCommande lc : entity.getLignes()) {
                    stmt.setLong(1, entity.getId());
                    stmt.setLong(2, lc.getProduit().getId());
                    stmt.setInt(3, lc.getQuantite());
                    stmt.addBatch();
                }
                stmt.executeBatch();
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
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void delete(Long id) {
        // Cascade delete should handle lines if schema set up right, but explicit
        // transaction is safer
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Lines are deleted via foreign key cascade usually, but if not:
            String delLines = "DELETE FROM ligne_commande WHERE commande_id=?";
            try (PreparedStatement ps = conn.prepareStatement(delLines)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            String delCmd = "DELETE FROM commande WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(delCmd)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            e.printStackTrace();
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
        }
    }

    @Override
    public List<Commande> findByStatut(StatutCommande statut) {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT c.*, f.nom as fournisseur_nom FROM commande c " +
                "JOIN fournisseur f ON c.fournisseur_id = f.id WHERE c.statut = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commande c = mapResultSet(rs);
                    loadLignes(c, conn);
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
