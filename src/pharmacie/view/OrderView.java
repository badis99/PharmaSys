package pharmacie.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pharmacie.controller.OrderController;
import pharmacie.model.Commande;
import pharmacie.model.Fournisseur;
import pharmacie.model.LigneCommande;
import pharmacie.model.Produit;

import java.util.Optional;
import java.time.format.DateTimeFormatter;

public class OrderView {
    private BorderPane layout;
    private OrderController controller;
    private ComboBox<Fournisseur> supplierCombo;
    private TableView<Produit> productTable;
    private TableView<LigneCommande> orderTable;
    private TableView<Commande> historyTable;

    public OrderView() {
        this.controller = new OrderController(this);
        createView();
        refreshTables();
    }

    private void createView() {
        layout = new BorderPane();

        TabPane tabPane = new TabPane();

        // Tab 1: New Order
        Tab newOrderTab = new Tab("Nouvelle Commande");
        newOrderTab.setClosable(false);
        newOrderTab.setContent(createNewOrderView());

        // Tab 2: History
        Tab historyTab = new Tab("Historique");
        historyTab.setClosable(false);
        historyTab.setContent(createHistoryView());

        tabPane.getTabs().addAll(newOrderTab, historyTab);
        layout.setCenter(tabPane);
    }

    private BorderPane createNewOrderView() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        // TOP: Supplier Selection
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        topBar.setStyle("-fx-background-color: #eee; -fx-padding: 10px;");

        Label supplierLbl = new Label("Fournisseur:");
        supplierCombo = new ComboBox<>();
        supplierCombo.getItems().addAll(controller.getAllSuppliers());
        supplierCombo.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur f) {
                return f != null ? f.getNom() : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return null;
            }
        });

        topBar.getChildren().addAll(supplierLbl, supplierCombo);
        pane.setTop(topBar);

        // SPLIT: Products vs Order Items
        SplitPane split = new SplitPane();

        // Left: Products
        VBox left = new VBox(10);
        left.setPadding(new Insets(10));
        left.getChildren().add(new Label("Catalogue Produits"));

        productTable = new TableView<>();
        setupProductTable();
        controller.loadProducts(productTable);

        Button addBtn = new Button("Ajouter à la commande");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            Produit p = productTable.getSelectionModel().getSelectedItem();
            if (p != null)
                askQtyAndAdd(p);
        });

        left.getChildren().addAll(productTable, addBtn);

        Button newProdBtn = new Button("Nouveau Produit");
        newProdBtn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        newProdBtn.setMaxWidth(Double.MAX_VALUE);
        newProdBtn.setOnAction(e -> showNewProductDialog());

        left.getChildren().add(newProdBtn);

        // Right: Order Items
        VBox right = new VBox(10);
        right.setPadding(new Insets(10));
        right.getChildren().add(new Label("Contenu de la commande"));

        orderTable = new TableView<>();
        setupOrderTable();
        orderTable.setItems(controller.getOrderItems());

        Button sendBtn = new Button("Envoyer Commande");
        sendBtn.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setOnAction(e -> controller.createOrder(supplierCombo.getValue()));

        right.getChildren().addAll(orderTable, sendBtn);

        split.getItems().addAll(left, right);
        pane.setCenter(split);
        return pane;
    }

    private VBox createHistoryView() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        HBox actions = new HBox(10);
        Button refreshBtn = new Button("Actualiser");
        refreshBtn.setOnAction(e -> refreshTables());

        Button receiveBtn = new Button("Réceptionner (Mise en stock)");
        receiveBtn.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
        receiveBtn.setOnAction(e -> {
            Commande c = historyTable.getSelectionModel().getSelectedItem();
            if (c != null)
                controller.receiveOrder(c);
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        cancelBtn.setOnAction(e -> {
            Commande c = historyTable.getSelectionModel().getSelectedItem();
            if (c != null)
                controller.cancelOrder(c);
        });

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");
        editBtn.setOnAction(e -> {
            Commande c = historyTable.getSelectionModel().getSelectedItem();
            if (c != null) {
                if (c.getStatut() == pharmacie.model.StatutCommande.EN_ATTENTE) {
                    showEditOrderDialog(c);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Seules les commandes EN ATTENTE peuvent être modifiées.")
                            .show();
                }
            }
        });

        actions.getChildren().addAll(refreshBtn, editBtn, receiveBtn, cancelBtn);

        historyTable = new TableView<>();
        setupHistoryTable();

        pane.getChildren().addAll(actions, historyTable);
        return pane;
    }

    private void setupProductTable() {
        TableColumn<Produit, String> nameCol = new TableColumn<>("Produit");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock Actuel");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));
        productTable.getColumns().addAll(nameCol, stockCol);
    }

    private void setupOrderTable() {
        TableColumn<LigneCommande, String> pCol = new TableColumn<>("Produit");
        pCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduit().getNom()));

        TableColumn<LigneCommande, Integer> qCol = new TableColumn<>("Quantité");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        orderTable.getColumns().addAll(pCol, qCol);
    }

    private void setupHistoryTable() {
        TableColumn<Commande, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Commande, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateCreation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Commande, String> fourCol = new TableColumn<>("Fournisseur");
        fourCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getFournisseur() != null ? cell.getValue().getFournisseur().getNom() : "?"));

        TableColumn<Commande, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        historyTable.getColumns().addAll(idCol, dateCol, fourCol, statusCol);
    }

    private void askQtyAndAdd(Produit p) {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Quantité");
        dialog.setHeaderText("Commander: " + p.getNom());
        dialog.setContentText("Quantité:");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent(s -> {
            try {
                int q = Integer.parseInt(s);
                if (q > 0)
                    controller.addItem(p, q);
            } catch (NumberFormatException e) {
            }
        });
    }

    public void refreshTables() {
        if (orderTable != null)
            orderTable.refresh();
        if (historyTable != null) {
            historyTable.setItems(FXCollections.observableArrayList(controller.getOrderHistory()));
            historyTable.refresh();
        }
        if (productTable != null && controller != null) {
            controller.loadProducts(productTable);
        }
    }

    private void showNewProductDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Produit Simple");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField nom = new TextField();
        TextField priceBuy = new TextField();
        TextField priceSell = new TextField();
        TextField stock = new TextField("0");
        TextField bar = new TextField();

        grid.addRow(0, new Label("Nom:"), nom);
        grid.addRow(1, new Label("Prix Achat:"), priceBuy);
        grid.addRow(2, new Label("Prix Vente:"), priceSell);
        grid.addRow(3, new Label("Stock Init:"), stock);
        grid.addRow(4, new Label("Code Barre:"), bar);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                Produit p = new Produit();
                p.setNom(nom.getText());
                p.setPrixAchat(new java.math.BigDecimal(priceBuy.getText()));
                p.setPrixVente(new java.math.BigDecimal(priceSell.getText()));
                p.setStockActuel(Integer.parseInt(stock.getText()));
                p.setSeuilMin(5); // Default
                p.setCodeBarre(bar.getText());

                new pharmacie.service.StockService().saveProduit(p);
                refreshTables();
                new Alert(Alert.AlertType.INFORMATION, "Produit créé.").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).show();
            }
        }
    }

    public void clearSelection() {
        supplierCombo.getSelectionModel().clearSelection();
    }

    private void showEditOrderDialog(Commande c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Commande #" + c.getId());
        dialog.setHeaderText("Ajuster les quantités pour : " + c.getFournisseur().getNom());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(400);

        TableView<LigneCommande> editTable = new TableView<>();
        TableColumn<LigneCommande, String> pCol = new TableColumn<>("Produit");
        pCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduit().getNom()));
        TableColumn<LigneCommande, Integer> qCol = new TableColumn<>("Quantité");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        editTable.getColumns().addAll(pCol, qCol);
        editTable.setItems(FXCollections.observableArrayList(c.getLignes()));

        Button editQtyBtn = new Button("Changer Quantité");
        editQtyBtn.setOnAction(e -> {
            LigneCommande selected = editTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog qtyDialog = new TextInputDialog(String.valueOf(selected.getQuantite()));
                qtyDialog.setTitle("Quantité");
                qtyDialog.setHeaderText("Produit: " + selected.getProduit().getNom());
                Optional<String> res = qtyDialog.showAndWait();
                res.ifPresent(s -> {
                    try {
                        int q = Integer.parseInt(s);
                        if (q > 0) {
                            selected.setQuantite(q);
                            editTable.refresh();
                        }
                    } catch (NumberFormatException ex) {
                    }
                });
            }
        });

        content.getChildren().addAll(new Label("Articles:"), editTable, editQtyBtn);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.updateOrder(c);
        }
    }

    public Parent getView() {
        return layout;
    }
}
