package pharmacie.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import pharmacie.controller.UserManagementController;
import pharmacie.model.Role;
import pharmacie.model.Utilisateur;

import java.util.Optional;

public class UserManagementView {
    private BorderPane layout;
    private TableView<Utilisateur> table;
    private UserManagementController controller;
    private Utilisateur currentUser;

    public UserManagementView(Utilisateur currentUser) {
        this.currentUser = currentUser;
        this.controller = new UserManagementController(this, currentUser);
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

        Button addBtn = new Button("Ajouter Utilisateur");
        addBtn.setOnAction(e -> showAddUserDialog());

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setOnAction(e -> {
            Utilisateur selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.deleteUser(selected);
            } else {
                showAlert("Erreur", "Veuillez sélectionner un utilisateur à supprimer.");
            }
        });

        toolbar.getChildren().addAll(refreshBtn, addBtn, deleteBtn);
        layout.setTop(toolbar);

        // Table
        table = new TableView<>();

        TableColumn<Utilisateur, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Utilisateur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Utilisateur, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Utilisateur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Utilisateur, Role> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, roleCol);

        layout.setCenter(table);
    }

    private void showAddUserDialog() {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Utilisateur");
        dialog.setHeaderText("Créer un compte");

        ButtonType okButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField email = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().setAll(Role.values());
        roleCombo.setValue(Role.EMPLOYE);

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nom, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenom, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(password, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                Utilisateur u = new Utilisateur();
                u.setNom(nom.getText());
                u.setPrenom(prenom.getText());
                u.setEmail(email.getText());
                u.setPasswordHash(password.getText()); // Controller will hash this
                u.setRole(roleCombo.getValue());
                return u;
            }
            return null;
        });

        Optional<Utilisateur> result = dialog.showAndWait();
        result.ifPresent(u -> controller.addUser(u));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Parent getView() {
        return layout;
    }

    public TableView<Utilisateur> getTable() {
        return table;
    }
}
