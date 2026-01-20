package pharmacie.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import pharmacie.model.Produit;
import pharmacie.service.ReportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportView {
    private BorderPane layout;
    private ReportService reportService;
    private TableView<Produit> stockTable;
    private TableView<Map<String, Object>> supplierPerformanceTable;
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

        // 2. Row Panels (Tables)
        VBox rowPanels = new VBox(25);

        // a. État des Stocks Table
        stockTable = new TableView<>();
        setupStockTable();
        Node stockPane = createTableContainer("📊 État Détaillé des Stocks", stockTable);

        // b. Performance Fournisseurs Table
        supplierPerformanceTable = new TableView<>();
        setupSupplierPerformanceTable();
        Node perfPane = createTableContainer("⭐ Performance et Dépenses par Fournisseur", supplierPerformanceTable);

        rowPanels.getChildren().addAll(stockPane, perfPane);
        content.getChildren().addAll(kpiRow, rowPanels);
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

    private void setupStockTable() {
        TableColumn<Produit, String> nameCol = new TableColumn<>("Produit");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock Actuel");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));

        TableColumn<Produit, Integer> thresholdCol = new TableColumn<>("Seuil Min");
        thresholdCol.setCellValueFactory(new PropertyValueFactory<>("seuilMin"));

        TableColumn<Produit, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cell -> {
            Produit p = cell.getValue();
            String status = p.isStockLow() ? "CRITIQUE" : "NORMAL";
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        // Cell factory for coloring status
        statusCol.setCellFactory(column -> new TableCell<Produit, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("CRITIQUE")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    }
                }
            }
        });

        stockTable.getColumns().addAll(nameCol, stockCol, thresholdCol, statusCol);
        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stockTable.setPrefHeight(250);
    }

    private void setupSupplierPerformanceTable() {
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Fournisseur");
        nameCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty((String) cell.getValue().get("name")));

        TableColumn<Map<String, Object>, Integer> scoreCol = new TableColumn<>("Score Performance");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<Map<String, Object>, String> expCol = new TableColumn<>("Total Achats (€)");
        expCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().get("total") + " €"));

        supplierPerformanceTable.getColumns().addAll(nameCol, scoreCol, expCol);
        supplierPerformanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        supplierPerformanceTable.setPrefHeight(250);
    }

    private VBox createTableContainer(String title, Node table) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        container.getChildren().addAll(titleLbl, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    public void refresh() {
        // 1. Stock Data
        Map<String, Object> stockData = reportService.getReportData("STOCK");
        if (stockData.get("produits") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Produit> produits = (List<Produit>) stockData.get("produits");
            stockTable.setItems(FXCollections.observableArrayList(produits));
        }

        // 2. Revenue Data
        Map<String, Object> revenueData = reportService.getReportData("REVENUE");
        if (revenueData.containsKey("totalRevenue")) {
            BigDecimal rev = (BigDecimal) revenueData.get("totalRevenue");
            totalRevenueLabel.setText(String.format("%.2f €", rev));
        }
        if (revenueData.containsKey("transactionCount")) {
            Integer count = (Integer) revenueData.get("transactionCount");
            totalSalesLabel.setText(count.toString());
        }

        // 3. Expenditure & Performance Data merged for table
        Map<String, Object> expData = reportService.getReportData("EXPENDITURE");
        Map<String, Object> perfData = reportService.getReportData("PERFORMANCE");

        if (expData.containsKey("totalExpenditure")) {
            BigDecimal totalExp = (BigDecimal) expData.get("totalExpenditure");
            totalExpenditureLabel.setText(String.format("%.2f €", totalExp));
        }

        List<Map<String, Object>> combinedSupplierData = new ArrayList<>();
        if (expData.containsKey("supplierBreakdown") && perfData.containsKey("performanceBreakdown")) {
            @SuppressWarnings("unchecked")
            Map<String, BigDecimal> expenditures = (Map<String, BigDecimal>) expData.get("supplierBreakdown");
            @SuppressWarnings("unchecked")
            Map<String, Integer> performances = (Map<String, Integer>) perfData.get("performanceBreakdown");

            for (String supplierName : expenditures.keySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", supplierName);
                row.put("total", expenditures.get(supplierName));
                row.put("score", performances.getOrDefault(supplierName, 0));
                combinedSupplierData.add(row);
            }
        }
        supplierPerformanceTable.setItems(FXCollections.observableArrayList(combinedSupplierData));
    }

    public Parent getView() {
        return layout;
    }
}
