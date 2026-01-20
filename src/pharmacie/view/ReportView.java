package pharmacie.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pharmacie.model.Produit;
import pharmacie.service.ReportService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ReportView {
    private BorderPane layout;
    private ReportService reportService;
    private PieChart stockChart;
    private BarChart<String, Number> supplierChart; // Volume
    private BarChart<String, Number> performanceChart; // Scores
    private Label totalRevenueLabel;
    private Label totalSalesLabel;
    private Label totalExpenditureLabel;

    public ReportView() {
        this.reportService = new ReportService();
        createView();
        refresh();
    }

    private void createView() {
        layout = new BorderPane();
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f7f6;");

        // --- Clean Header ---
        VBox header = new VBox(5);
        header.setPadding(new Insets(0, 0, 20, 0));
        Label title = new Label("Tableau de Bord Analytique");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subtitle = new Label("Aperçu en temps réel de la performance de la pharmacie");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button refreshBtn = new Button("Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> refresh());

        HBox topBox = new HBox();
        topBox.getChildren().add(header);
        HBox.setHgrow(header, Priority.ALWAYS);
        topBox.getChildren().add(refreshBtn);
        layout.setTop(topBox);

        // --- Main Content (Scrollable) ---
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox content = new VBox(25);
        content.setPadding(new Insets(10));

        // 1. KPI Cards Row
        HBox kpiRow = new HBox(20);
        kpiRow.setAlignment(Pos.CENTER);

        VBox revCard = createKPICard("REVENU TOTAL", "0.00 €", "#2ecc71", "💰");
        totalRevenueLabel = (Label) revCard.getChildren().get(1);

        VBox salesCard = createKPICard("VENTES RÉALISÉES", "0", "#3498db", "🛒");
        totalSalesLabel = (Label) salesCard.getChildren().get(1);

        VBox expCard = createKPICard("DÉPENSES (ACHATS)", "0.00 €", "#e74c3c", "📉");
        totalExpenditureLabel = (Label) expCard.getChildren().get(1);

        kpiRow.getChildren().addAll(revCard, salesCard, expCard);

        // 2. Charts Section (Grid)
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(25);
        chartsGrid.setVgap(25);

        // Stock Chart
        stockChart = new PieChart();
        stockChart.setTitle(null); // Clean
        Node stockPane = createChartContainer("Répartition des Stocks", stockChart);
        chartsGrid.add(stockPane, 0, 0);

        // Supplier Charts
        Node supplierCharts = createSupplierCharts();
        chartsGrid.add(supplierCharts, 1, 0);

        content.getChildren().addAll(kpiRow, chartsGrid);
        scrollPane.setContent(content);
        layout.setCenter(scrollPane);
    }

    private VBox createKPICard(String title, String value, String color, String icon) {
        VBox card = new VBox(10);
        card.setPrefSize(280, 120);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");

        Label titleLbl = new Label(icon + " " + title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private VBox createChartContainer(String title, Node chart) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        container.getChildren().addAll(titleLbl, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return container;
    }

    public void refresh() {
        // Stock Data
        Map<String, Object> stockData = reportService.getReportData("STOCK");
        if (stockData.get("produits") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Produit> produits = (List<Produit>) stockData.get("produits");

            int lowStock = 0;
            int normalStock = 0;

            for (Produit p : produits) {
                if (p.isStockLow()) {
                    lowStock++;
                } else {
                    normalStock++;
                }
            }

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Stock Faible", lowStock),
                    new PieChart.Data("Stock Normal", normalStock));
            stockChart.setData(pieData);
        }

        // Revenue Data
        Map<String, Object> revenueData = reportService.getReportData("REVENUE");
        if (revenueData.containsKey("totalRevenue")) {
            BigDecimal rev = (BigDecimal) revenueData.get("totalRevenue");
            totalRevenueLabel.setText(rev.toString() + " €");
        }
        if (revenueData.containsKey("transactionCount")) {
            Integer count = (Integer) revenueData.get("transactionCount");
            totalSalesLabel.setText(count.toString());
        }

        // Expenditure Data (for chart or cards if added)
        Map<String, Object> expData = reportService.getReportData("EXPENDITURE");
        if (expData.containsKey("totalExpenditure")) {
            BigDecimal exp = (BigDecimal) expData.get("totalExpenditure");
            totalExpenditureLabel.setText(exp.toString() + " €");
        }

        updateSupplierCharts();
    }

    private void updateSupplierCharts() {
        if (supplierChart != null) {
            XYChart.Series<String, Number> seriesVol = new XYChart.Series<>();
            seriesVol.setName("Volume d'Achats (€)");
            Map<String, Object> expData = reportService.getReportData("EXPENDITURE");
            if (expData.containsKey("supplierBreakdown")) {
                @SuppressWarnings("unchecked")
                Map<String, BigDecimal> breakdown = (Map<String, BigDecimal>) expData.get("supplierBreakdown");
                for (Map.Entry<String, BigDecimal> entry : breakdown.entrySet()) {
                    seriesVol.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }
            supplierChart.getData().setAll(seriesVol);
        }

        if (performanceChart != null) {
            XYChart.Series<String, Number> seriesPerf = new XYChart.Series<>();
            seriesPerf.setName("Score de Performance (0-100)");
            Map<String, Object> perfData = reportService.getReportData("PERFORMANCE");
            if (perfData.containsKey("performanceBreakdown")) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> breakdown = (Map<String, Integer>) perfData.get("performanceBreakdown");
                for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                    seriesPerf.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }
            performanceChart.getData().setAll(seriesPerf);
        }
    }

    private Node createSupplierCharts() {
        VBox charts = new VBox(25);

        // Volume Chart
        CategoryAxis vXAxis = new CategoryAxis();
        vXAxis.setLabel("Nom du Fournisseur");
        NumberAxis vYAxis = new NumberAxis();
        vYAxis.setLabel("Achats (€)");

        supplierChart = new BarChart<>(vXAxis, vYAxis);
        supplierChart.setLegendVisible(false);
        supplierChart.setPrefHeight(350);
        Node volPane = createChartContainer("Dépenses par Fournisseur (€)", supplierChart);

        // Performance Chart
        CategoryAxis pXAxis = new CategoryAxis();
        pXAxis.setLabel("Nom du Fournisseur");
        NumberAxis pYAxis = new NumberAxis(0, 100, 10);
        pYAxis.setLabel("Score (0-100)");

        performanceChart = new BarChart<>(pXAxis, pYAxis);
        performanceChart.setLegendVisible(false);
        performanceChart.setPrefHeight(350);
        Node perfPane = createChartContainer("Performance des Fournisseurs", performanceChart);

        charts.getChildren().addAll(volPane, perfPane);

        // Initial load
        updateSupplierCharts();

        return charts;
    }

    public Parent getView() {
        return layout;
    }
}
