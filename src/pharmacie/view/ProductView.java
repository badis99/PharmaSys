package pharmacie.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import pharmacie.controller.ProductController;
import pharmacie.model.Produit;

import java.util.Optional;
import java.math.BigDecimal;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonBar.ButtonData;

public class ProductView {
    private BorderPane layout;
    private TableView<Produit> table;
    private ProductController controller;

    public ProductView() {
        // We initialize parts first
        this.controller = new ProductController(this);
        createView();
        controller.loadData(table);
    }

    private void createView() {
        layout = new BorderPane();
        layout.setPadding(new Insets(10));

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        Button refreshBtn = new Button("Actualiser");
        refreshBtn.setOnAction(e -> controller.loadData(table));

        Button addBtn = new Button("Ajouter Produit");
        addBtn.setOnAction(e -> showAddProductDialog());

        Button editBtn = new Button("Modifier Produit");
        editBtn.setOnAction(e -> {
            Produit p = table.getSelectionModel().getSelectedItem();
            if (p != null) {
                showEditProductDialog(p);
            }
        });

        Button delBtn = new Button("Supprimer Produit");
        delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        delBtn.setOnAction(e -> {
            Produit p = table.getSelectionModel().getSelectedItem();
            controller.deleteProduct(p);
        });

        toolbar.getChildren().addAll(refreshBtn, addBtn, editBtn, delBtn);
        layout.setTop(toolbar);

        // Table
        table = new TableView<>();

        TableColumn<Produit, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Produit, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));

        TableColumn<Produit, Double> prixCol = new TableColumn<>("Prix Vente");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixVente"));

        table.getColumns().addAll(idCol, nomCol, stockCol, prixCol);

        layout.setCenter(table);
    }

    public Parent getView() {
        return layout;
    }

    public TableView<Produit> getTable() {
        return table;
    }

    private void showAddProductDialog() {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Produit");
        dialog.setHeaderText("Saisir les détails du produit");

        ButtonType loginButtonType = new ButtonType("Ajouter", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField();
        TextField description = new TextField();
        TextField prixAchat = new TextField();
        TextField prixVente = new TextField();
        TextField stock = new TextField();
        TextField seuil = new TextField();
        TextField codeBarre = new TextField();

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Prix Achat:"), 0, 2);
        grid.add(prixAchat, 1, 2);
        grid.add(new Label("Prix Vente:"), 0, 3);
        grid.add(prixVente, 1, 3);
        grid.add(new Label("Stock Initial:"), 0, 4);
        grid.add(stock, 1, 4);
        grid.add(new Label("Seuil Min:"), 0, 5);
        grid.add(seuil, 1, 5);
        grid.add(new Label("Code Barre:"), 0, 6);
        grid.add(codeBarre, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    return new Produit(
                            null,
                            nom.getText(),
                            new BigDecimal(prixAchat.getText()),
                            new BigDecimal(prixVente.getText()),
                            Integer.parseInt(stock.getText()),
                            Integer.parseInt(seuil.getText()),
                            codeBarre.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        Optional<Produit> result = dialog.showAndWait();

        result.ifPresent(product -> {
            if (product != null) {
                controller.addProduct(product);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Données invalides");
                alert.setContentText("Veuillez vérifier les champs.");
                alert.showAndWait();
            }
        });
    }

    private void showEditProductDialog(Produit p) {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle("Modifier Produit");
        dialog.setHeaderText("Modifier les détails de " + p.getNom());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nom = new TextField(p.getNom());
        TextField description = new TextField(p.getDescription());
        TextField prixAchat = new TextField(p.getPrixAchat().toString());
        TextField prixVente = new TextField(p.getPrixVente().toString());
        TextField stock = new TextField(String.valueOf(p.getStockActuel()));
        TextField seuil = new TextField(String.valueOf(p.getSeuilMin()));
        TextField codeBarre = new TextField(p.getCodeBarre());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Prix Achat:"), 0, 2);
        grid.add(prixAchat, 1, 2);
        grid.add(new Label("Prix Vente:"), 0, 3);
        grid.add(prixVente, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stock, 1, 4);
        grid.add(new Label("Seuil Min:"), 0, 5);
        grid.add(seuil, 1, 5);
        grid.add(new Label("Code Barre:"), 0, 6);
        grid.add(codeBarre, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    p.setNom(nom.getText());
                    p.setDescription(description.getText());
                    p.setPrixAchat(new BigDecimal(prixAchat.getText()));
                    p.setPrixVente(new BigDecimal(prixVente.getText()));
                    p.setStockActuel(Integer.parseInt(stock.getText()));
                    p.setSeuilMin(Integer.parseInt(seuil.getText()));
                    p.setCodeBarre(codeBarre.getText());
                    return p;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        Optional<Produit> result = dialog.showAndWait();
        result.ifPresent(updatedProduct -> {
            if (updatedProduct != null) {
                controller.updateProduct(updatedProduct);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Données invalides");
                alert.setContentText("Veuillez vérifier les formats numériques.");
                alert.showAndWait();
            }
        });
    }
}
