USE pharmacie_db;

-- Drop old constraint
ALTER TABLE vente DROP FOREIGN KEY vente_ibfk_2;

-- Make utilisateur_id nullable
ALTER TABLE vente MODIFY utilisateur_id BIGINT NULL;

-- Add new constraint with ON DELETE SET NULL
ALTER TABLE vente ADD CONSTRAINT fk_vente_utilisateur 
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL;
