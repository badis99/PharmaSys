package pharmacie.service;

import pharmacie.dao.interfaces.CommandeDAO;
import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.model.Commande;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenditureReportStrategy implements ReportStrategy {
    private CommandeDAO commandeDAO;

    public ExpenditureReportStrategy() {
        this.commandeDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getCommandeDAO();
    }

    @Override
    public String generateReport() {
        Map<String, Object> data = getData();
        BigDecimal total = (BigDecimal) data.get("totalExpenditure");
        return "Total Dépenses Commande: " + total + " €";
    }

    @Override
    public Map<String, Object> getData() {
        List<Commande> commandes = commandeDAO.findAll();
        BigDecimal totalExpenditure = BigDecimal.ZERO;
        Map<String, BigDecimal> supplierTotals = new HashMap<>();

        for (Commande c : commandes) {
            BigDecimal cmdTotal = c.getTotalMontant();
            totalExpenditure = totalExpenditure.add(cmdTotal);

            if (c.getFournisseur() != null) {
                String fName = c.getFournisseur().getNom();
                supplierTotals.put(fName, supplierTotals.getOrDefault(fName, BigDecimal.ZERO).add(cmdTotal));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalExpenditure", totalExpenditure);
        result.put("supplierBreakdown", supplierTotals);
        return result;
    }
}
