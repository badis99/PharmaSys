package pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import pharmacie.dao.interfaces.ClientDAO;
import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.dao.interfaces.VenteDAO;
import pharmacie.model.Client;
import pharmacie.model.Vente;
import pharmacie.view.ClientView;

import java.util.List;

public class ClientController {
    private ClientView view;
    private ClientDAO clientDAO;
    private VenteDAO venteDAO;

    public ClientController(ClientView view) {
        this.view = view;
        this.clientDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getClientDAO();
        this.venteDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getVenteDAO();
    }

    public void loadClients(TableView<Client> table) {
        table.setItems(FXCollections.observableArrayList(clientDAO.findAll()));
    }

    public void addClient(String nom, String prenom, String email, String tel, String secruite) {
        if (nom.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Nom et Prénom obligatoires.");
            return;
        }

        Client c = new Client();
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setEmail(email);
        c.setTelephone(tel);
        c.setCarteVitale(secruite);

        try {
            clientDAO.save(c);
            view.refreshTable();
            showAlert("Succès", "Client ajouté.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    public List<Vente> getClientHistory(Client c) {
        if (c == null)
            return FXCollections.emptyObservableList();
        // Assuming VenteDAO has a findByClient method or we filter all
        // For simplicity/speed if DAO misses it, let's just filter list manually or add
        // method locally if feasible.
        // Checking VenteDAO... It's an interface.
        // Let's rely on filtering findAll() for now or specific method if exists.
        // Actually MySQLVenteDAO might not have specific findByClient exposed in
        // interface.
        // We will fetch all and filter for this prototype to avoid big DAO refactor.
        List<Vente> all = venteDAO.findAll();
        return all.stream()
                .filter(v -> v.getClient() != null && v.getClient().getId().equals(c.getId()))
                .toList();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
