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
        layout.setStyle("-fx-background-color: #f4f4f4;");

        // --- Header ---
        Label title = new Label("Tableau de Bord Analytique");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button refreshBtn = new Button("Actualiser les données");
        refreshBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refresh());

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(title);
        headerPane.setRight(refreshBtn);
        headerPane.setPadding(new Insets(0, 0, 30, 0));
        layout.setTop(headerPane);

        // --- Main Content ---
        VBox mainContent = new VBox(30);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(10));

        // 1. Top Section: Key Performance Indicators (Cards)
        mainContent.getChildren().add(createRevenueCards());

        // 2. Middle Section: Stock and Supplier Charts
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(30);
        chartsGrid.setVgap(30);
        chartsGrid.setAlignment(Pos.CENTER);

        // Stock Section (Left)
        VBox stockBox = new VBox(15);
        stockBox.setStyle(
                "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        stockBox.getChildren().add(createStockChart());
        chartsGrid.add(stockBox, 0, 0);

        // Supplier Section (Right)
        VBox supplierBox = new VBox(15);
        supplierBox.setStyle(
                "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        supplierBox.getChildren().add(createSupplierCharts());
        chartsGrid.add(supplierBox, 1, 0);

        mainContent.getChildren().add(chartsGrid);

        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        layout.setCenter(scroll);
    }

    private VBox createCard(String title, String initialValue, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 5 0; " +
                        "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(280);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label valueLbl = new Label(initialValue);
        valueLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private Node createRevenueCards() {
        HBox cards = new HBox(25);
        cards.setAlignment(Pos.CENTER);

        VBox revenueCard = createCard("CHIFFRE D'AFFAIRES", "0.00 €", "#2ecc71");
        totalRevenueLabel = (Label) revenueCard.getChildren().get(1);

        VBox salesCard = createCard("NOMBRE DE VENTES", "0", "#3498db");
        totalSalesLabel = (Label) salesCard.getChildren().get(1);

        VBox expenditureCard = createCard("DÉPENSES TOTALES", "0.00 €", "#e74c3c");
        totalExpenditureLabel = (Label) expenditureCard.getChildren().get(1);

        cards.getChildren().addAll(revenueCard, salesCard, expenditureCard);
        return cards;
    }

    private Node createStockChart() {
        stockChart = new PieChart();
        stockChart.setTitle("État du Stock");
        return stockChart;
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
        VBox charts = new VBox(20);

        // Volume Chart
        CategoryAxis vXAxis = new CategoryAxis();
        vXAxis.setLabel("Fournisseur");
        NumberAxis vYAxis = new NumberAxis();
        vYAxis.setLabel("Montant (€)");
        supplierChart = new BarChart<>(vXAxis, vYAxis);
        supplierChart.setTitle("Volume d'Achats");
        supplierChart.setPrefHeight(300);

        // Performance Chart
        CategoryAxis pXAxis = new CategoryAxis();
        pXAxis.setLabel("Fournisseur");
        NumberAxis pYAxis = new NumberAxis(0, 100, 10);
        pYAxis.setLabel("Score");
        performanceChart = new BarChart<>(pXAxis, pYAxis);
        performanceChart.setTitle("Performance Fournisseurs (Score)");
        performanceChart.setPrefHeight(300);

        charts.getChildren().addAll(supplierChart, performanceChart);

        // Initial load
        updateSupplierCharts();

        return charts;
    }

    public Parent getView() {
        return layout;
    }
}
