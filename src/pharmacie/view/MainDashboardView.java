package pharmacie.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import pharmacie.controller.MainDashboardController;
import pharmacie.model.Utilisateur;

public class MainDashboardView {
    private MainDashboardController controller;
    private Scene scene;
    private Utilisateur currentUser;

    // UI Components
    private BorderPane mainLayout;
    private Label welcomeLabel;

    public MainDashboardView(Utilisateur user) {
        this.currentUser = user;
        this.controller = new MainDashboardController(this, user);
        createView();
    }

    private void createView() {
        mainLayout = new BorderPane();

        // --- Top: Header ---
        VBox header = new VBox();
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #333;");

        welcomeLabel = new Label("Bienvenue, " + currentUser.getPrenom() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        header.getChildren().add(welcomeLabel);
        mainLayout.setTop(header);

        // --- Left: Navigation ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #e0e0e0;");
        sidebar.setPrefWidth(200);

        Button produitsBtn = navButton("Produits");
        produitsBtn.setOnAction(e -> controller.showProducts());

        Button ventesBtn = navButton("Ventes");
        ventesBtn.setOnAction(e -> controller.showSales());

        Button commandesBtn = navButton("Commandes Fournisseur");
        commandesBtn.setOnAction(e -> controller.showOrders());

        Button clientsBtn = navButton("Clients");
        clientsBtn.setOnAction(e -> controller.showClients());

        Button reportsBtn = navButton("Rapports (Admin)");
        reportsBtn.setOnAction(e -> controller.showReports());

        sidebar.getChildren().addAll(produitsBtn, ventesBtn, commandesBtn, reportsBtn);

        // Add User Button only if Admin
        if (currentUser.getRole() == pharmacie.model.Role.ADMIN) {
            Button usersBtn = navButton("Gestion Utilisateurs");
            usersBtn.setOnAction(e -> controller.showUsers());

            Button suppBtn = navButton("Fournisseurs");
            suppBtn.setOnAction(e -> controller.showSuppliers());

            sidebar.getChildren().addAll(usersBtn, suppBtn);
        }

        Button logoutBtn = navButton("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> controller.logout());

        sidebar.getChildren().addAll(new Separator(), logoutBtn);
        mainLayout.setLeft(sidebar);

        // --- Center: Content ---
        Label placeholder = new Label("Sélectionnez une option dans le menu.");
        mainLayout.setCenter(placeholder);

        this.scene = new Scene(mainLayout, 1024, 768);
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(40);
        return btn;
    }

    public Scene getScene() {
        return scene;
    }

    public void setCenterContent(javafx.scene.Node content) {
        mainLayout.setCenter(content);
    }
}
