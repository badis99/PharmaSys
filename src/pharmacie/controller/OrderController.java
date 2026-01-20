package pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import pharmacie.dao.interfaces.CommandeDAO;
import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.dao.interfaces.FournisseurDAO;
import pharmacie.dao.interfaces.ProduitDAO;
import pharmacie.model.Commande;
import pharmacie.model.Fournisseur;
import pharmacie.model.LigneCommande;
import pharmacie.model.Produit;
import pharmacie.view.OrderView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderController {
    private OrderView view;
    private CommandeDAO commandeDAO;
    private FournisseurDAO fournisseurDAO;
    private ProduitDAO produitDAO;
    private ObservableList<LigneCommande> orderItems;

    public OrderController(OrderView view) {
        this.view = view;
        this.commandeDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getCommandeDAO();
        this.fournisseurDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getFournisseurDAO();
        this.produitDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getProduitDAO();
        this.orderItems = FXCollections.observableArrayList();
    }

    public List<Fournisseur> getAllSuppliers() {
        return fournisseurDAO.findAll();
    }

    public void loadProducts(TableView<Produit> table) {
        table.setItems(FXCollections.observableArrayList(produitDAO.findAll()));
    }

    public ObservableList<LigneCommande> getOrderItems() {
        return orderItems;
    }

    public void addItem(Produit p, int qty) {
        // Check if already in order
        for (LigneCommande item : orderItems) {
            if (item.getProduit().getId().equals(p.getId())) {
                item.setQuantite(item.getQuantite() + qty);
                view.refreshTables();
                return;
            }
        }
        LigneCommande item = new LigneCommande();
        item.setProduit(p);
        item.setQuantite(qty);
        orderItems.add(item);
    }

    public void createOrder(Fournisseur f) {
        if (f == null) {
            showAlert("Erreur", "Veuillez sélectionner un fournisseur.");
            return;
        }
        if (orderItems.isEmpty()) {
            showAlert("Erreur", "La commande est vide.");
            return;
        }

        Commande cmd = new Commande();
        cmd.setFournisseur(f);
        cmd.setDateCreation(LocalDateTime.now());
        cmd.setStatut(pharmacie.model.StatutCommande.EN_ATTENTE);
        cmd.setLignes(new ArrayList<>(orderItems));

        try {
            commandeDAO.save(cmd);
            orderItems.clear();
            view.clearSelection(); // Reset UI selection
            view.refreshTables();
            showAlert("Succès", "Commande créée avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de créer la commande.");
        }
    }

    public List<Commande> getOrderHistory() {
        return commandeDAO.findAll();
    }

    public void receiveOrder(Commande c) {
        if (c == null)
            return;
        if (c.getStatut() != pharmacie.model.StatutCommande.EN_ATTENTE) {
            showAlert("Info", "Cette commande n'est pas en attente.");
            return;
        }

        try {
            // Update stock
            for (LigneCommande line : c.getLignes()) {
                Produit p = line.getProduit();
                p.setStockActuel(p.getStockActuel() + line.getQuantite());
                produitDAO.save(p);
            }

            // Update status
            c.setStatut(pharmacie.model.StatutCommande.RECUE);
            commandeDAO.save(c); // Update status in DB

            view.refreshTables();
            showAlert("Succès", "Commande réceptionnée. Stock mis à jour.");

            // Check for low stock alerts
            List<Produit> lowStock = produitDAO.findLowStock();
            if (!lowStock.isEmpty()) {
                StringBuilder sb = new StringBuilder("Note : Les produits suivants restent sous le seuil minimal :\n");
                for (Produit p : lowStock) {
                    sb.append("- ").append(p.getNom()).append(" (Stock: ").append(p.getStockActuel()).append(")\n");
                }
                showAlert("Stock toujours Bas", sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réception.");
        }
    }

    public void cancelOrder(Commande c) {
        if (c == null)
            return;
        if (c.getStatut() != pharmacie.model.StatutCommande.EN_ATTENTE) {
            showAlert("Info", "Impossible d'annuler une commande déjà traitée.");
            return;
        }

        try {
            c.setStatut(pharmacie.model.StatutCommande.ANNULEE);
            commandeDAO.save(c);
            view.refreshTables();
            showAlert("Succès", "Commande annulée.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'annulation.");
        }
    }

    public void updateOrder(Commande c) {
        try {
            commandeDAO.save(c);
            view.refreshTables();
            showAlert("Succès", "Commande mise à jour.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
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
