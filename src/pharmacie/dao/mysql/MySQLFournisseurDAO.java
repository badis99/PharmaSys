package pharmacie.dao.mysql;

import pharmacie.dao.interfaces.FournisseurDAO;
import pharmacie.exception.ValidationException;
import pharmacie.model.Fournisseur;
import pharmacie.util.ValidationUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLFournisseurDAO extends AbstractMySQLDAO implements FournisseurDAO {

    private Fournisseur mapResultSet(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getLong("id"));
        f.setNom(rs.getString("nom"));
        f.setAdresse(rs.getString("adresse"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setNotePerformance(rs.getInt("note_performance"));
        return f;
    }

    @Override
    public Optional<Fournisseur> findById(Long id) {
        String sql = "SELECT * FROM fournisseur WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
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
    public List<Fournisseur> findAll() {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur";
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
    public void save(Fournisseur entity) {
        // 1. Validate phone format
        ValidationUtils.validatePhone(entity.getTelephone());

        // 2. Validate phone uniqueness across both client and fournisseur tables
        if (!isPhoneUnique(entity.getTelephone(), entity.getId(), "fournisseur")) {
            throw new ValidationException(
                    "Ce numéro de téléphone est déjà utilisé par un autre client ou fournisseur.");
        }

        if (entity.getId() == null) {
            String sql = "INSERT INTO fournisseur (nom, adresse, telephone, email, note_performance) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getAdresse());
                stmt.setString(3, entity.getTelephone());
                stmt.setString(4, entity.getEmail());
                stmt.setInt(5, entity.getNotePerformance());
                stmt.executeUpdate();
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next())
                        entity.setId(gk.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE fournisseur SET nom=?, adresse=?, telephone=?, email=?, note_performance=? WHERE id=?";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getAdresse());
                stmt.setString(3, entity.getTelephone());
                stmt.setString(4, entity.getEmail());
                stmt.setInt(5, entity.getNotePerformance());
                stmt.setLong(6, entity.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM fournisseur WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
