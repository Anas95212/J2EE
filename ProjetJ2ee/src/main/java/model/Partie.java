package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une partie multijoueur.
 */
public class Partie {
    /** Identifiant unique de la partie (exemple : "GAME-123456789"). */
    private String gameId;
    
    /** Nom choisi pour la partie (ex: "Ma Partie Fun"). */
    private String nomPartie;
    
    /** Nombre maximum de joueurs autorisés dans la partie. */
    private int maxJoueurs;
    
    /** Liste des joueurs actuellement présents dans la partie. */
    private List<Joueur> joueurs;
    
    /** Indique si la partie est en cours (true) ou en attente (false). */
    private boolean enCours;
    
    /** Carte associée à la partie (exemple : terrain de jeu 2D). */
    private Carte carte;
    
    /**
     * Identifie le créateur de la partie (ex : "Joueur_12345").
     * Cette information permet de gérer des actions réservées (ex: lancer la partie).
     */
    private String createur;
    
    /** Index pour savoir quel joueur est en train de jouer. */
    private int indexJoueurActuel = 0;
    
    /** Combat en cours, s'il y en a un (null sinon). */
    private Combat combatEnCours;
    
    private CombatVille combatVilleEnCours;
    
    /**
     * Constructeur par défaut (sans préciser le créateur).
     * @param nomPartie nom donné à la partie
     * @param maxJoueurs nombre maximum de joueurs
     */
    public Partie(String nomPartie, int maxJoueurs) {
        this(nomPartie, maxJoueurs, null);
    }

    /**
     * Constructeur principal, permettant de préciser le créateur de la partie.
     * @param nomPartie nom donné à la partie
     * @param maxJoueurs nombre maximum de joueurs
     * @param createur identifiant du créateur (ex: "Joueur_12345")
     */
    public Partie(String nomPartie, int maxJoueurs, String createur) {
        this.gameId = generateGameId();
        this.nomPartie = nomPartie;
        this.maxJoueurs = maxJoueurs;
        this.joueurs = new ArrayList<>();
        this.enCours = false;
        this.carte = new Carte(12, 12); 
        this.carte.initialiserCarte();
        this.createur = createur; 
    }

    /**
     * Génère un identifiant unique pour la partie
     * basé sur l'horodatage système.
     * @return un identifiant unique (ex: "GAME-1684923477463")
     */
    private String generateGameId() {
        return "GAME-" + System.currentTimeMillis();
    }

    // -------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------

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

    public String getCreateur() {
        return createur;
    }

    public void setCreateur(String createur) {
        this.createur = createur;
    }

    public int getIndexJoueurActuel() {
        return indexJoueurActuel;
    }

    public void setIndexJoueurActuel(int indexJoueurActuel) {
        this.indexJoueurActuel = indexJoueurActuel;
    }

    public Combat getCombatEnCours() {
        return combatEnCours;
    }

    public void setCombatEnCours(Combat combatEnCours) {
        this.combatEnCours = combatEnCours;
    }
    
    public CombatVille getCombatVilleEnCours() {
        return combatVilleEnCours;
    }

    public void setCombatVilleEnCours(CombatVille combatVilleEnCours) {
        this.combatVilleEnCours = combatVilleEnCours;
    }
    /**
     * Retourne une liste contenant uniquement les noms (logins) des joueurs.
     */
    public List<String> getNomsJoueurs() {
        List<String> noms = new ArrayList<>();
        for (Joueur joueur : joueurs) {
            noms.add(joueur.getLogin());
        }
        return noms;
    }

    // -------------------------------------------------------------------
    // Méthodes de gestion
    // -------------------------------------------------------------------

    /**
     * Tente d'ajouter un joueur dans la partie.
     * @param joueur joueur à ajouter
     * @return true si l'ajout est réussi, false si la partie est pleine
     */
    public boolean ajouterJoueur(Joueur joueur) {
        if (joueurs.size() < maxJoueurs) {
            joueurs.add(joueur);
            return true;
        }
        return false;
    }

    /**
     * Retire un joueur de la partie.
     * @param joueur joueur à retirer
     * @return true si le joueur a été trouvé et retiré, false sinon
     */
    public boolean retirerJoueur(Joueur joueur) {
        if (joueurs.contains(joueur)) {
            joueurs.remove(joueur);
            return true;
        }
        return false;
    }

    /**
     * Vérifie si un joueur précis est déjà dans cette partie.
     * @param joueur joueur à vérifier
     * @return true si le joueur est présent, false sinon
     */
    public boolean contientJoueur(Joueur joueur) {
        return joueurs.contains(joueur);
    }

    /**
     * Fait passer le tour au joueur suivant (version sans paramètre).
     */
    public void nextPlayerTurn() {
        if (!joueurs.isEmpty()) {
            indexJoueurActuel = (indexJoueurActuel + 1) % joueurs.size();
        }
    }

    /**
     * Fait passer le tour au joueur suivant (version avec endTurnTriggered).
     * @param endTurnTriggered si true, on incrémente l'index
     */
    public void nextPlayerTurn(boolean endTurnTriggered) {
        if (endTurnTriggered && !joueurs.isEmpty()) {
            indexJoueurActuel = (indexJoueurActuel + 1) % joueurs.size();
        }
    }

    @Override
    public String toString() {
        return "Partie{" +
                "gameId='" + gameId + '\'' +
                ", nomPartie='" + nomPartie + '\'' +
                ", maxJoueurs=" + maxJoueurs +
                ", joueurs=" + joueurs.size() +
                ", enCours=" + enCours +
                ", createur=" + createur +
                '}';
    }
    
    
    public List<Joueur> verifierJoueursElimines() {
        List<Joueur> joueursElimines = new ArrayList<>();
        for (Joueur joueur : new ArrayList<>(joueurs)) {
            if (joueur.getUnites().isEmpty()) {
                joueursElimines.add(joueur);
                retirerJoueur(joueur);
            }
        }
        return joueursElimines;
    }

    public boolean isPartieTerminee() {
        return joueurs.size() <= 1; // La partie se termine quand un seul joueur reste
    }

}