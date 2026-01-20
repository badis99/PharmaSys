package pharmacie.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import pharmacie.controller.SupplierController;
import pharmacie.model.Fournisseur;

import java.util.Optional;

public class SupplierView {
    private BorderPane layout;
    private SupplierController controller;
    private TableView<Fournisseur> table;

    public SupplierView() {
        this.controller = new SupplierController(this);
        createView();
        controller.loadData(table);
    }

    private void createView() {
        layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Header
        Label title = new Label("Gestion des Fournisseurs");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        layout.setTop(title);

        // Table
        table = new TableView<>();
        setupTable();
        layout.setCenter(table);

        // Actions
        HBox actions = new HBox(15);
        actions.setPadding(new Insets(15, 0, 0, 0));
        actions.setAlignment(Pos.CENTER);

        Button reloadBtn = new Button("Actualiser");
        reloadBtn.setOnAction(e -> controller.loadData(table));

        Button addBtn = new Button("Ajouter Fournisseur");
        addBtn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddDialog());

        Button delBtn = new Button("Supprimer");
        delBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        delBtn.setOnAction(e -> confirmDelete());

        actions.getChildren().addAll(reloadBtn, addBtn, delBtn);
        layout.setBottom(actions);
    }

    private void setupTable() {
        TableColumn<Fournisseur, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Fournisseur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Fournisseur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Fournisseur, String> telCol = new TableColumn<>("Téléphone");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        TableColumn<Fournisseur, String> adrCol = new TableColumn<>("Adresse");
        adrCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        table.getColumns().addAll(idCol, nomCol, emailCol, telCol, adrCol);
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Fournisseur");
        dialog.setHeaderText("Saisir les détails du fournisseur");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField addrField = new TextField();

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Téléphone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Adresse:"), 0, 3);
        grid.add(addrField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.addSupplier(
                    nomField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    addrField.getText());
        }
    }

    private void confirmDelete() {
        Fournisseur selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un fournisseur.").show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer " + selected.getNom() + " ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            controller.deleteSupplier(selected);
        }
    }

    public void refreshTable() {
        controller.loadData(table);
    }

    public Parent getView() {
        return layout;
    }
}
