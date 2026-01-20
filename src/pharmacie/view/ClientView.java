package pharmacie.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import pharmacie.controller.ClientController;
import pharmacie.model.Client;
import pharmacie.model.Vente;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ClientView {
    private BorderPane layout;
    private ClientController controller;
    private TableView<Client> clientTable;
    private TableView<Vente> historyTable;

    public ClientView() {
        this.controller = new ClientController(this);
        createView();
        controller.loadClients(clientTable);
    }

    private void createView() {
        layout = new BorderPane();

        TabPane tabs = new TabPane();

        Tab listTab = new Tab("Liste Clients");
        listTab.setClosable(false);
        listTab.setContent(createListPane());

        Tab historyTab = new Tab("Historique Achats");
        historyTab.setClosable(false);
        historyTab.setContent(createHistoryPane());

        tabs.getTabs().addAll(listTab, historyTab);
        layout.setCenter(tabs);
    }

    private BorderPane createListPane() {
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(10));

        clientTable = new TableView<>();
        setupClientTable();

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button refreshBtn = new Button("Actualiser");
        refreshBtn.setOnAction(e -> controller.loadClients(clientTable));

        Button addBtn = new Button("Nouveau Client");
        addBtn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddDialog());

        actions.getChildren().addAll(refreshBtn, addBtn);

        p.setCenter(clientTable);
        p.setBottom(actions);
        return p;
    }

    private BorderPane createHistoryPane() {
        BorderPane p = new BorderPane();
        p.setPadding(new Insets(10));

        Label lbl = new Label("Sélectionnez un client dans la liste pour voir son historique ci-dessous:");

        historyTable = new TableView<>();
        setupHistoryTable();

        p.setTop(lbl);
        p.setCenter(historyTable);

        // Listen to selection changes in client table to update history
        clientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                historyTable.setItems(javafx.collections.FXCollections.observableArrayList(
                        controller.getClientHistory(newVal)));
            }
        });

        return p;
    }

    private void setupClientTable() {
        TableColumn<Client, String> nom = new TableColumn<>("Nom");
        nom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Client, String> prenom = new TableColumn<>("Prénom");
        prenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Client, String> tel = new TableColumn<>("Tél");
        tel.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        clientTable.getColumns().addAll(nom, prenom, tel);
    }

    private void setupHistoryTable() {
        TableColumn<Vente, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateVente().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        TableColumn<Vente, String> total = new TableColumn<>("Total");
        total.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTotal().toString() + " €"));

        historyTable.getColumns().addAll(date, total);
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Client");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nom = new TextField();
        TextField prenom = new TextField();
        TextField email = new TextField();
        TextField tel = new TextField();
        TextField sec = new TextField();

        grid.addRow(0, new Label("Nom:"), nom);
        grid.addRow(1, new Label("Prénom:"), prenom);
        grid.addRow(2, new Label("Email:"), email);
        grid.addRow(3, new Label("Tél:"), tel);
        grid.addRow(4, new Label("Carte Vitale:"), sec);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            controller.addClient(nom.getText(), prenom.getText(), email.getText(), tel.getText(), sec.getText());
        }
    }

    public void refreshTable() {
        controller.loadClients(clientTable);
    }

    public Parent getView() {
        return layout;
    }
}
