package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant une partie de jeu multijoueur.
 */
public class Partie {
    private String gameId; // Identifiant unique de la partie
    private String nomPartie; // Nom de la partie
    private int maxJoueurs; // Nombre maximum de joueurs
    private List<Joueur> joueurs; // Liste des joueurs participants
    private boolean enCours; // État de la partie (true = en cours, false = en attente)
    private Carte carte; // Carte de la partie

    /**
     * Constructeur pour initialiser une nouvelle partie.
     *
     * @param nomPartie  Le nom de la partie.
     * @param maxJoueurs Le nombre maximum de joueurs.
     */
    public Partie(String nomPartie, int maxJoueurs) {
        this.gameId = generateGameId();
        this.nomPartie = nomPartie;
        this.maxJoueurs = maxJoueurs;
        this.joueurs = new ArrayList<>();
        this.enCours = false;
        this.carte = new Carte(15, 15); // Par défaut, une carte 15x15
        this.carte.initialiserCarte();
    }

    /**
     * Génère un identifiant unique pour la partie.
     * 
     * @return Un ID unique.
     */
    private String generateGameId() {
        return "GAME-" + System.currentTimeMillis();
    }

    // Getters et setters
    public String getGameId() {
        return gameId;
    }

    public String getNomPartie() {
        return nomPartie;
    }

    public void setNomPartie(String nomPartie) {
        this.nomPartie = nomPartie;
    }

    public int getMaxJoueurs() {
        return maxJoueurs;
    }

    public void setMaxJoueurs(int maxJoueurs) {
        this.maxJoueurs = maxJoueurs;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public boolean isEnCours() {
        return enCours;
    }

    public void setEnCours(boolean enCours) {
        this.enCours = enCours;
    }

    public Carte getCarte() {
        return carte;
    }

    public void setCarte(Carte carte) {
        this.carte = carte;
    }

    /**
     * Ajoute un joueur à la partie.
     *
     * @param joueur Le joueur à ajouter.
     * @return true si le joueur a été ajouté, false si la partie est pleine.
     */
    public boolean ajouterJoueur(Joueur joueur) {
        if (joueurs.size() < maxJoueurs) {
            joueurs.add(joueur);
            return true;
        }
        return false; // La partie est pleine
    }
    
    
    /**
     * Retire un joueur de la partie.
     *
     * @param joueur Le joueur à retirer.
     * @return true si le joueur a été retiré avec succès, false sinon.
     */
    public boolean retirerJoueur(Joueur joueur) {
        if (joueurs.contains(joueur)) {
            joueurs.remove(joueur);
            return true;
        }
        return false; // Le joueur n'était pas dans la partie
    }

    /**
     * Vérifie si un joueur est déjà dans la partie.
     *
     * @param joueur Le joueur à vérifier.
     * @return true si le joueur est dans la partie, false sinon.
     */
    public boolean contientJoueur(Joueur joueur) {
        return joueurs.contains(joueur);
    }

    @Override
    public String toString() {
        return "Partie{" +
                "gameId='" + gameId + '\'' +
                ", nomPartie='" + nomPartie + '\'' +
                ", maxJoueurs=" + maxJoueurs +
                ", joueurs=" + joueurs.size() +
                ", enCours=" + enCours +
                '}';
    }
}
