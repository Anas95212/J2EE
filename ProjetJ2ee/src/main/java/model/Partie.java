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
    
    /** Nombre maximum de joueurs autorisés dans cette partie. */
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
    private int indexJoueurActuel = 0;
    /**
     * Constructeur par défaut (sans préciser le créateur).
     * @param nomPartie nom donné à la partie
     * @param maxJoueurs nombre maximum de joueurs
     */
    public Partie(String nomPartie, int maxJoueurs) {
        // On appelle le nouveau constructeur en passant createur=null
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
        this.createur = createur; // On stocke le créateur
    }

    /**
     * Génère un identifiant unique pour la partie
     * basé sur l'horodatage système.
     * @return un identifiant unique (ex: "GAME-1684923477463")
     */
    private String generateGameId() {
        return "GAME-" + System.currentTimeMillis();
    }

    // -------------------------
    // Getters / Setters
    // -------------------------
    
    /**
     * Retourne une liste contenant uniquement les noms (logins) des joueurs.
     * @return Liste des noms des joueurs.
     */
    public List<String> getNomsJoueurs() {
        List<String> noms = new ArrayList<>();
        for (Joueur joueur : joueurs) { // joueurs est la liste des joueurs dans la partie
            noms.add(joueur.getLogin());
        }
        return noms;
    }
    public int getIndexJoueurActuel() {
        return indexJoueurActuel;
    }

    // AJOUT : setter (optionnel si nécessaire)
    public void setIndexJoueurActuel(int index) {
        this.indexJoueurActuel = index;
    }
    /**
     * Retourne l'ID unique de la partie.
     * @return l'identifiant (ex: "GAME-12345")
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Retourne le nom de la partie.
     * @return nom de la partie
     */
    public String getNomPartie() {
        return nomPartie;
    }

    /**
     * Définit (modifie) le nom de la partie.
     * @param nomPartie nouveau nom
     */
    public void setNomPartie(String nomPartie) {
        this.nomPartie = nomPartie;
    }

    /**
     * Retourne le nombre max. de joueurs autorisés.
     * @return le nombre maximal de joueurs
     */
    public int getMaxJoueurs() {
        return maxJoueurs;
    }

    /**
     * Définit la limite de joueurs autorisés.
     * @param maxJoueurs nombre max. de joueurs
     */
    public void setMaxJoueurs(int maxJoueurs) {
        this.maxJoueurs = maxJoueurs;
    }

    /**
     * Retourne la liste actuelle des joueurs participants.
     * @return liste des joueurs
     */
    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    /**
     * Indique si la partie est en cours (true) ou non (false).
     * @return true si la partie est en cours, false sinon
     */
    public boolean isEnCours() {
        return enCours;
    }

    /**
     * Modifie l'état de la partie (en cours ou non).
     * @param enCours true pour lancer la partie, false pour la signaler en attente
     */
    public void setEnCours(boolean enCours) {
        this.enCours = enCours;
    }

    /**
     * Retourne la carte du jeu associée à cette partie.
     * @return carte de la partie
     */
    public Carte getCarte() {
        return carte;
    }

    /**
     * Définit la carte du jeu associée à cette partie.
     * @param carte instance de Carte
     */
    public void setCarte(Carte carte) {
        this.carte = carte;
    }

    /**
     * Retourne l'identifiant du créateur de la partie.
     * @return ex: "Joueur_12345"
     */
    public String getCreateur() {
        return createur;
    }

    /**
     * Modifie (redéfinit) le créateur de la partie.
     * @param createur nouveau créateur
     */
    public void setCreateur(String createur) {
        this.createur = createur;
    }

    // -------------------------
    // Méthodes de gestion
    // -------------------------
    public void nextPlayerTurn() {
        if (!joueurs.isEmpty()) {
            indexJoueurActuel = (indexJoueurActuel + 1) % joueurs.size();
        }
    }
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
    
    public void nextPlayerTurn(boolean endTurnTriggered) {
        if (endTurnTriggered && !joueurs.isEmpty()) {
            indexJoueurActuel = (indexJoueurActuel + 1) % joueurs.size();
        }
    }

}
