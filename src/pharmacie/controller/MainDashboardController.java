package pharmacie.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pharmacie.model.Utilisateur;
import pharmacie.view.LoginView;
import pharmacie.view.MainDashboardView;
import pharmacie.view.ProductView;
import pharmacie.view.SceneManager;
import pharmacie.view.UserManagementView;

public class MainDashboardController {
    private MainDashboardView view;
    private Utilisateur user;

    public MainDashboardController(MainDashboardView view, Utilisateur user) {
        this.view = view;
        this.user = user;
    }

    public void showProducts() {
        // Switch center content to Product Management View
        ProductView productView = new ProductView();
        view.setCenterContent(productView.getView());
    }

    public void showSales() {
        pharmacie.view.SaleView saleView = new pharmacie.view.SaleView(user);
        view.setCenterContent(saleView.getView());
    }

    public void showOrders() {
        pharmacie.view.OrderView orderView = new pharmacie.view.OrderView();
        view.setCenterContent(orderView.getView());
    }

    public void showSuppliers() {
        if (user.getRole() != pharmacie.model.Role.ADMIN) {
            view.setCenterContent(new VBox(new Label("Accès refusé.")));
            return;
        }
        pharmacie.view.SupplierView supplierView = new pharmacie.view.SupplierView();
        view.setCenterContent(supplierView.getView());
    }

    public void showClients() {
        pharmacie.view.ClientView clientView = new pharmacie.view.ClientView();
        view.setCenterContent(clientView.getView());
    }

    public void showReports() {
        if (user.getRole() != pharmacie.model.Role.ADMIN) {
            view.setCenterContent(new VBox(new Label("Accès refusé: Administrateur requis.")));
            return;
        }

        pharmacie.view.ReportView reportView = new pharmacie.view.ReportView();
        view.setCenterContent(reportView.getView());
    }

    public void showUsers() {
        if (user.getRole() != pharmacie.model.Role.ADMIN) {
            view.setCenterContent(new VBox(new Label("Accès refusé: Administrateur requis.")));
            return;
        }
        UserManagementView userView = new UserManagementView(user);
        view.setCenterContent(userView.getView());
    }

    public void logout() {
        LoginView login = new LoginView();
        SceneManager.getInstance().switchScene("Login", login.getScene());
    }
}
