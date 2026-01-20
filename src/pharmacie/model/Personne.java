package pharmacie.model;

/**
 * Abstract base class representing a person.
 * Used for Utilisateur and Client.
 */
public abstract class Personne {
    protected Long id;
    protected String nom;
    protected String prenom;
    protected String email;

    public Personne() {
    }

    public Personne(Long id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Personne personne = (Personne) o;
        return id != null ? id.equals(personne.id) : personne.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
