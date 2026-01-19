package pharmacie.service;

import java.util.HashMap;
import java.util.Map;

public class ReportService {
    private Map<String, ReportStrategy> strategies;

    public ReportService() {
        strategies = new HashMap<>();
        strategies.put("STOCK", new StockReportStrategy());
        strategies.put("REVENUE", new RevenueReportStrategy());
        strategies.put("EXPENDITURE", new ExpenditureReportStrategy());
        strategies.put("PERFORMANCE", new SupplierPerformanceStrategy());
    }

    public String generateReport(String type) {
        ReportStrategy strategy = strategies.get(type.toUpperCase());
        if (strategy == null) {
            return "Type de rapport inconnu: " + type;
        }
        return strategy.generateReport();
    }

    public Map<String, Object> getReportData(String type) {
        ReportStrategy strategy = strategies.get(type.toUpperCase());
        if (strategy == null) {
            return new HashMap<>();
        }
        return strategy.getData();
    }
}
