package pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import pharmacie.dao.interfaces.DAOFactory;
import pharmacie.dao.interfaces.UtilisateurDAO;
import pharmacie.model.Utilisateur;
import pharmacie.service.AuthService;
import pharmacie.view.UserManagementView;

import java.time.LocalDateTime;
import java.util.List;

public class UserManagementController {
    private UserManagementView view;
    private UtilisateurDAO utilisateurDAO;
    private AuthService authService;
    private Utilisateur currentUser;

    public UserManagementController(UserManagementView view, Utilisateur currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        this.utilisateurDAO = DAOFactory.getFactory(DAOFactory.Type.MYSQL).getUtilisateurDAO();
        this.authService = new AuthService();
    }

    public void loadData(TableView<Utilisateur> table) {
        List<Utilisateur> users = utilisateurDAO.findAll();
        table.setItems(FXCollections.observableArrayList(users));
    }

    public void addUser(Utilisateur u) {
        // Hash password before saving
        String hash = authService.hashPassword(u.getPasswordHash());
        u.setPasswordHash(hash);
        u.setDateCreation(LocalDateTime.now());

        utilisateurDAO.save(u);
        loadData(view.getTable());
    }

    public void deleteUser(Utilisateur u) {
        if (u == null)
            return;

        if (u.getId().equals(currentUser.getId())) {
            showError("Action impossible", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'utilisateur : " + u.getNom() + " " + u.getPrenom());
        confirm.setContentText("Êtes-vous sûr ? Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    utilisateurDAO.delete(u.getId());
                    loadData(view.getTable());
                } catch (RuntimeException e) {
                    showError("Erreur de suppression", e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
