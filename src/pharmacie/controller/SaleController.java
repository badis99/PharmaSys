package pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import pharmacie.dao.interfaces.ClientDAO;
import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.dao.interfaces.ProduitDAO;
import pharmacie.dao.interfaces.VenteDAO;
import pharmacie.model.*;
import pharmacie.view.SaleView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleController {
    private SaleView view;
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    private ClientDAO clientDAO;
    private ObservableList<LigneVente> cartItems;
    private Utilisateur currentUser;

    public SaleController(SaleView view, Utilisateur user) {
        this.view = view;
        this.currentUser = user;
        this.produitDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getProduitDAO();
        this.venteDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getVenteDAO();
        this.clientDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getClientDAO();
        this.cartItems = FXCollections.observableArrayList();
    }

    public void loadProducts(TableView<Produit> table) {
        List<Produit> products = produitDAO.findAll();
        table.setItems(FXCollections.observableArrayList(products));
    }

    public void saveClient(Client c) {
        clientDAO.save(c);
    }

    public List<Client> getAllClients() {
        return clientDAO.findAll();
    }

    public void loadSalesHistory(TableView<Vente> table) {
        List<Vente> sales = venteDAO.findAll();
        table.setItems(FXCollections.observableArrayList(sales));
    }

    public ObservableList<LigneVente> getCartItems() {
        return cartItems;
    }

    public void addToCart(Produit product, int quantity) {
        if (product.getStockActuel() < quantity) {
            showAlert("Stock Insuffisant", "Pas assez de stock pour ce produit.");
            return;
        }

        // Check if already in cart
        for (LigneVente item : cartItems) {
            if (item.getProduit().getId().equals(product.getId())) {
                if (product.getStockActuel() < item.getQuantite() + quantity) {
                    showAlert("Stock Insuffisant", "Pas assez de stock cumulé.");
                    return;
                }
                item.setQuantite(item.getQuantite() + quantity);
                item.setSousTotal(product.getPrixVente().multiply(BigDecimal.valueOf(item.getQuantite())));
                view.refreshCart();
                return;
            }
        }

        LigneVente newLine = new LigneVente();
        newLine.setProduit(product);
        newLine.setQuantite(quantity);
        newLine.setPrixUnitaire(product.getPrixVente());
        newLine.setSousTotal(product.getPrixVente().multiply(BigDecimal.valueOf(quantity)));

        cartItems.add(newLine);
        view.refreshCart();
    }

    public void checkout(Client client) {
        if (cartItems.isEmpty()) {
            showAlert("Panier vide", "Ajoutez des produits avant de valider.");
            return;
        }

        Vente vente = new Vente();
        vente.setDateVente(LocalDateTime.now());
        vente.setUtilisateur(currentUser);
        vente.setClient(client);
        vente.setLignes(new ArrayList<>(cartItems));
        vente.calculerTotal();

        // Save Vente (DAO should handle saving lines and updating stock ideally,
        // but if DAO is simple, we might need to update stock manually here)
        // For this project, assuming DAO handles complexity or we do it sequentially.
        // Let's do it transactionally ideally, but for now sequentially.

        try {
            venteDAO.save(vente);

            cartItems.clear();
            view.refreshCart();
            view.refreshProductList(); // Reload stock in table
            showAlert("Succès", "Vente enregistrée avec succès !");

            // Check for low stock alerts
            List<Produit> lowStock = produitDAO.findLowStock();
            if (!lowStock.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "Attention ! Les produits suivants ont atteint le seuil minimal :\n");
                for (Produit p : lowStock) {
                    sb.append("- ").append(p.getNom()).append(" (Stock: ").append(p.getStockActuel()).append(")\n");
                }
                showAlert("Alerte Stock Bas", sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement de la vente.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
