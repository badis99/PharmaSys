package pharmacie.service;

import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.dao.interfaces.FournisseurDAO;
import pharmacie.model.Fournisseur;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierPerformanceStrategy implements ReportStrategy {
    private FournisseurDAO fournisseurDAO;

    public SupplierPerformanceStrategy() {
        this.fournisseurDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getFournisseurDAO();
    }

    @Override
    public String generateReport() {
        return "Rapport de Performance Fournisseurs";
    }

    @Override
    public Map<String, Object> getData() {
        List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
        Map<String, Integer> performanceMap = new HashMap<>();

        for (Fournisseur f : fournisseurs) {
            performanceMap.put(f.getNom(), f.getNotePerformance());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("performanceBreakdown", performanceMap);
        return result;
    }
}
