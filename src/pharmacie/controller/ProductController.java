package pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import pharmacie.model.Produit;
import pharmacie.service.StockService;
import pharmacie.view.ProductView;

import java.util.List;

public class ProductController {
    private ProductView view;
    private StockService stockService;

    public ProductController(ProductView view) {
        this.view = view;
        this.stockService = new StockService();
    }

    public void loadData(TableView<Produit> table) {
        List<Produit> products = stockService.getAllProduits();
        table.setItems(FXCollections.observableArrayList(products));
    }

    public void addProduct(Produit p) {
        try {
            stockService.saveProduit(p);
            loadData(view.getTable());
            showAlert("Succès", "Produit ajouté avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    public void updateProduct(Produit p) {
        try {
            stockService.saveProduit(p);
            loadData(view.getTable());
            showAlert("Succès", "Produit mis à jour avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    public void deleteProduct(Produit p) {
        if (p == null) {
            showAlert("Avertissement", "Sélectionnez un produit.");
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le produit : " + p.getNom() + " ?");
        confirm.setContentText(
                "Attention: Cette action est irréversible et peut échouer si le produit est lié à des ventes ou commandes.");

        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                stockService.deleteProduit(p.getId());
                loadData(view.getTable());
                showAlert("Succès", "Produit supprimé ainsi que son historique.");
            } catch (Exception e) {
                String msg = e.getMessage();
                // Check if it's our specific stock error or a generic constraint error
                if (msg != null && msg.contains("unités en stock")) {
                    showAlert("Suppression refusée", msg);
                } else if (msg != null && (msg.contains("constraint") || msg.contains("foreign key"))) {
                    showAlert("Erreur de Database",
                            "Impossible de supprimer ce produit. " + msg);
                } else {
                    e.printStackTrace();
                    showAlert("Erreur Inattendue", "Détail : " + (msg != null ? msg : e.toString()));
                }
            }
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
