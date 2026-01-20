package pharmacie.model;

/**
 * Represents a client of the pharmacy.
 */
public class Client extends Personne {
    private String carteVitale;

    public Client() {
    }

    public Client(Long id, String nom, String prenom, String email, String telephone, String carteVitale) {
        super(id, nom, prenom, email);
        this.telephone = telephone;
        this.carteVitale = carteVitale;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCarteVitale() {
        return carteVitale;
    }

    public void setCarteVitale(String carteVitale) {
        this.carteVitale = carteVitale;
    }
}
