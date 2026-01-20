package pharmacie.dao.mysql;

import pharmacie.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractMySQLDAO {
    protected Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    protected boolean isPhoneUnique(String phone, Long currentId, String currentTable) {
        if (phone == null || phone.trim().isEmpty())
            return true;

        String[] tables = { "client", "fournisseur" };
        for (String table : tables) {
            String sql = "SELECT id FROM " + table + " WHERE telephone = ?";
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long foundId = rs.getLong("id");
                        // If it's the same record we are updating, it's fine
                        if (table.equalsIgnoreCase(currentTable) && currentId != null && foundId == currentId) {
                            continue;
                        }
                        return false;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
