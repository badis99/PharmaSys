# PharmaSys - Système de Gestion de Pharmacie

PharmacieApp est une application de bureau moderne conçue pour simplifier la gestion quotidienne d'une pharmacie. Elle offre une interface intuitive pour le suivi des stocks, la gestion des ventes, les relations fournisseurs et l'analyse financière.

## 🚀 Fonctionnalités Clés

### 📦 Gestion des Stocks
- Suivi en temps réel des quantités de produits.
- Alertes visuelles pour les stocks faibles.
- **Suppression Intelligente** : Permet de supprimer des produits avec historique si le stock est à zéro.

### 💰 Ventes et Revenus
- Simulation de ventes avec mise à jour automatique des stocks.
- Calcul précis du Chiffre d'Affaires et du nombre de transactions.

### 🤝 Gestion des Fournisseurs
- Annuaire complet des fournisseurs avec fiches de contact.
- Système de notation de performance (0-100).
- Gestion des commandes d'approvisionnement avec suivi de réception.

### 📊 Tableaux de Bord Analytiques
- Cartes de performance (KPI) : CA Total, Ventes, Dépenses.
- Graphique de répartition des stocks.
- Analyse du volume d'achats par fournisseur.
- Graphique de comparaison des performances fournisseurs.

## 🛠️ Stack Technique
- **Langage** : Java 21+
- **Interface Graphique** : JavaFX
- **Base de Données** : MySQL
- **Accès aux Données** : JDBC avec Pattern DAO (Data Access Object)
- **Architecture** : MVC (Modèle-Vue-Contrôleur) avec Service Layer

## ⚙️ Installation

### Prérequis
- Java JDK 21 ou supérieur.
- MySQL Server installé et configuré.
- Pilote JDBC MySQL (inclus dans le dossier `lib`).

### Configuration de la Base de Données
1. Exécutez le script SQL `sql/schema.sql` pour créer les tables.
2. (Optionnel) Exécutez `sql/users.sql` pour créer les comptes de test.
3. Modifiez le fichier `src/pharmacie/config/db.properties` avec vos informations de connexion MySQL.

### Compilation et Lancement
L'application propose des scripts simples pour Windows :
- **Compiler** : Exécutez `.\compile.bat`
- **Lancer** : Exécutez `.\run.bat`

## 👥 Rôles Utilisateurs
- **ADMIN** : Accès complet (Gestion utilisateurs, rapports financiers, gestion fournisseurs).
- **EMPLOYEE** : Accès opérationnel (Ventes, consultation stock, gestion produits).

---
*Projet développé dans le cadre d'un cours de BD-POO.*
