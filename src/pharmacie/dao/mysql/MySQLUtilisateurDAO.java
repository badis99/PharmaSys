package pharmacie.dao.mysql;

import pharmacie.dao.interfaces.UtilisateurDAO;
import pharmacie.model.Role;
import pharmacie.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLUtilisateurDAO extends AbstractMySQLDAO implements UtilisateurDAO {

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return u;
    }

    @Override
    public Optional<Utilisateur> findById(Long id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
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
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
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
    public void save(Utilisateur entity) {
        if (entity.getId() == null) {
            // Insert
            String sql = "INSERT INTO utilisateur (nom, prenom, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getPrenom());
                stmt.setString(3, entity.getEmail());
                stmt.setString(4, entity.getPasswordHash());
                stmt.setString(5, entity.getRole().name());
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de l'ajout: " + e.getMessage());
            }
        } else {
            // Update
            String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, password_hash=?, role=? WHERE id=?";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.getNom());
                stmt.setString(2, entity.getPrenom());
                stmt.setString(3, entity.getEmail());
                stmt.setString(4, entity.getPasswordHash());
                stmt.setString(5, entity.getRole().name());
                stmt.setLong(6, entity.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la mise à jour: " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
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
}
