package pharmacie.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pharmacie.controller.SaleController;
import pharmacie.model.*;

import java.math.BigDecimal;
import java.util.Optional;

public class SaleView {
    private BorderPane layout;
    private SaleController controller;
    private TableView<Produit> productTable;
    private TableView<LigneVente> cartTable;
    private TableView<Vente> historyTable;
    private Label totalLabel;
    private ComboBox<Client> clientComboBox;

    public SaleView(Utilisateur user) {
        this.controller = new SaleController(this, user);
        createView();
        controller.loadProducts(productTable);
    }

    private void createView() {
        layout = new BorderPane();

        TabPane tabs = new TabPane();

        Tab posTab = new Tab("Caisse (Vente Directe)");
        posTab.setClosable(false);
        posTab.setContent(createPOSPane());

        Tab historyTab = new Tab("Historique Global des Ventes");
        historyTab.setClosable(false);
        historyTab.setContent(createHistoryPane());

        tabs.getTabs().addAll(posTab, historyTab);
        layout.setCenter(tabs);
    }

    private Node createPOSPane() {
        BorderPane posLayout = new BorderPane();
        posLayout.setPadding(new Insets(10));

        // Split Pane
        SplitPane splitPane = new SplitPane();

        // LEFT: Product Catalog
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        Label catalogLabel = new Label("Catalogue Produits");
        catalogLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        productTable = new TableView<>();
        setupProductTable();

        Button addToCartBtn = new Button("Ajouter au Panier");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(e -> {
            Produit selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                askQuantityAndAdd(selected);
            }
        });

        leftPane.getChildren().addAll(catalogLabel, productTable, addToCartBtn);

        // RIGHT: Cart
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));

        // Client Selection
        VBox clientBox = new VBox(5);
        clientBox.setPadding(new Insets(0, 0, 10, 0));
        Label clientLbl = new Label("Client (Optionnel):");

        HBox clientSelectionRow = new HBox(5);
        clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Sélectionnez un client...");
        clientComboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(clientComboBox, Priority.ALWAYS);
        refreshClientList();

        Button newClientBtn = new Button("Nouveau");
        newClientBtn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        newClientBtn.setOnAction(e -> showNewClientDialog());

        clientSelectionRow.getChildren().addAll(clientComboBox, newClientBtn);
        clientBox.getChildren().addAll(clientLbl, clientSelectionRow);

        Label cartLabel = new Label("Panier en cours");
        cartLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        cartTable = new TableView<>();
        setupCartTable();

        totalLabel = new Label("Total: 0.00 €");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button checkoutBtn = new Button("Valider la Vente");
        checkoutBtn.setStyle(
                "-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> doCheckout());

        rightPane.getChildren().addAll(clientBox, cartLabel, cartTable, totalLabel, checkoutBtn);

        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.6);

        posLayout.setCenter(splitPane);
        return posLayout;
    }

    private Node createHistoryPane() {
        VBox historyLayout = new VBox(10);
        historyLayout.setPadding(new Insets(15));

        Label title = new Label("Historique de toutes les ventes");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        historyTable = new TableView<>();
        setupHistoryTable();

        Button refreshBtn = new Button("Actualiser l'historique");
        refreshBtn.setOnAction(e -> controller.loadSalesHistory(historyTable));

        historyLayout.getChildren().addAll(title, historyTable, refreshBtn);

        // Initial load
        controller.loadSalesHistory(historyTable);

        return historyLayout;
    }

    private void setupHistoryTable() {
        TableColumn<Vente, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateVente()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        TableColumn<Vente, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(cell -> {
            Client c = cell.getValue().getClient();
            return new javafx.beans.property.SimpleStringProperty(
                    c != null ? c.getNom() + " " + c.getPrenom() : "Anonyme");
        });

        TableColumn<Vente, String> userCol = new TableColumn<>("Vendeur");
        userCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getUtilisateur() != null ? cell.getValue().getUtilisateur().getNom() : "-"));

        TableColumn<Vente, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        historyTable.getColumns().addAll(dateCol, clientCol, userCol, totalCol);
    }

    public void refreshClientList() {
        clientComboBox.setItems(javafx.collections.FXCollections.observableArrayList(controller.getAllClients()));
    }

    private void setupProductTable() {
        TableColumn<Produit, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));

        TableColumn<Produit, BigDecimal> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixVente"));

        productTable.getColumns().addAll(nomCol, stockCol, prixCol);
    }

    private void setupCartTable() {
        TableColumn<LigneVente, String> nomCol = new TableColumn<>("Produit");
        nomCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduit().getNom()));

        TableColumn<LigneVente, Integer> qtyCol = new TableColumn<>("Qte");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        TableColumn<LigneVente, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        cartTable.getColumns().addAll(nomCol, qtyCol, totalCol);
        cartTable.setItems(controller.getCartItems());
    }

    private void askQuantityAndAdd(Produit p) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Quantité");
        dialog.setHeaderText("Combien d'unités de " + p.getNom() + " ?");
        dialog.setContentText("Quantité:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    controller.addToCart(p, qty);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        });
    }

    private void showNewClientDialog() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Client");
        dialog.setHeaderText("Saisissez les informations du client");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField();
        nom.setPromptText("Nom");
        TextField prenom = new TextField();
        prenom.setPromptText("Prénom");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField tel = new TextField();
        tel.setPromptText("Téléphone");
        TextField vitale = new TextField();
        vitale.setPromptText("Carte Vitale");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenom, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Téléphone:"), 0, 3);
        grid.add(tel, 1, 3);
        grid.add(new Label("Carte Vitale:"), 0, 4);
        grid.add(vitale, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nom.getText().trim().isEmpty() || prenom.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom et le prénom sont obligatoires.");
                    alert.showAndWait();
                    return null;
                }
                Client c = new Client();
                c.setNom(nom.getText().trim());
                c.setPrenom(prenom.getText().trim());
                c.setEmail(email.getText().trim().isEmpty() ? null : email.getText().trim());
                c.setTelephone(tel.getText().trim().isEmpty() ? null : tel.getText().trim());
                c.setCarteVitale(vitale.getText().trim().isEmpty() ? null : vitale.getText().trim());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(client -> {
            try {
                controller.saveClient(client);
                refreshClientList();
                // Select the new client in the combo box
                // Since we implemented equals(), it should find it based on the ID set by
                // saveClient
                clientComboBox.getSelectionModel().select(client);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    public void refreshCart() {
        cartTable.refresh();
        // Recalculate Total
        BigDecimal total = BigDecimal.ZERO;
        for (LigneVente item : controller.getCartItems()) {
            total = total.add(item.getSousTotal());
        }
        totalLabel.setText("Total: " + total.toString() + " €");
    }

    public void refreshProductList() {
        controller.loadProducts(productTable);
        controller.loadSalesHistory(historyTable);
    }

    private void doCheckout() {
        Client selected = clientComboBox.getSelectionModel().getSelectedItem();
        // If the selected client has no ID, treat as anonymous to avoid errors
        if (selected != null && selected.getId() == null) {
            selected = null;
        }
        controller.checkout(selected);
        clientComboBox.getSelectionModel().clearSelection();
    }

    public Parent getView() {
        return layout;
    }
}
