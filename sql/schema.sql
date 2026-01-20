USE pharmacie_db;

-- Disable foreign key checks to allow dropping tables if they exist
SET foreign_key_checks = 0;

DROP TABLE IF EXISTS ligne_vente;
DROP TABLE IF EXISTS vente;
DROP TABLE IF EXISTS ligne_commande;
DROP TABLE IF EXISTS commande;
DROP TABLE IF EXISTS produit;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS fournisseur;
DROP TABLE IF EXISTS utilisateur;

SET foreign_key_checks = 1;

-- Table: Utilisateur (Admin / Employe)
CREATE TABLE utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYE') NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table: Fournisseur
CREATE TABLE fournisseur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL UNIQUE,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(150),
    note_performance INT DEFAULT 0 COMMENT '0-100 scale'
) ENGINE=InnoDB;

-- Table: Client
CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(150),
    carte_vitale VARCHAR(50) UNIQUE
) ENGINE=InnoDB;

-- Table: Produit
CREATE TABLE produit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    prix_achat DECIMAL(10, 2) NOT NULL,
    prix_vente DECIMAL(10, 2) NOT NULL,
    stock_actuel INT NOT NULL DEFAULT 0,
    seuil_min INT NOT NULL DEFAULT 5,
    code_barre VARCHAR(50) UNIQUE
) ENGINE=InnoDB;

-- Table: Commande (Supplier Orders)
CREATE TABLE commande (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fournisseur_id BIGINT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('EN_ATTENTE', 'RECUE', 'ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE',
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Table: Ligne_Commande
CREATE TABLE ligne_commande (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    commande_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    quantite INT NOT NULL CHECK (quantite > 0),
    FOREIGN KEY (commande_id) REFERENCES commande(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Table: Vente (Sales to Clients)
CREATE TABLE vente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_id BIGINT,
    utilisateur_id BIGINT,
    total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE SET NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Table: Ligne_Vente
CREATE TABLE ligne_vente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    quantite INT NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(10, 2) NOT NULL COMMENT 'Price at time of sale',
    sous_total DECIMAL(12, 2) GENERATED ALWAYS AS (quantite * prix_unitaire) STORED,
    FOREIGN KEY (vente_id) REFERENCES vente(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Initial Seed Data

-- Admin User (Password: admin123 -> SHA256 hashed usually, simply text for now as placeholder, app must hash)
-- In a real scenario, we insert the HASHED password. Let's assume the app hashes 'admin123'
INSERT INTO utilisateur (nom, prenom, email, password_hash, role) VALUES 
('Admin', 'System', 'admin@pharmacie.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN');

INSERT INTO fournisseur (nom, telephone, email) VALUES 
('Pfizer Distribution', '0102030405', 'contact@pfizer.com'),
('Sanofi Wholesaler', '0607080910', 'sales@sanofi.com');

INSERT INTO produit (nom, prix_achat, prix_vente, stock_actuel, seuil_min, code_barre) VALUES 
('Paracetamol 500mg', 1.50, 3.00, 100, 20, '123456789'),
('Ibuprofene 400mg', 2.00, 4.50, 50, 10, '987654321'),
('Amoxicilline 1g', 3.50, 7.90, 30, 5, '112233445');
