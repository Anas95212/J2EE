package model;
 
import java.util.ArrayList;
import java.util.List;
 
/**
* Classe représentant un joueur dans le jeu 4X.
*/
public class Joueur {
 
    // Attributs de la classe
 
    // Identifiant unique du joueur.
    private String login;
 
    // Score du joueur.
    private int score;
 
    // Points de production du joueur.
    private int pointsDeProduction;
 
    // Liste des unités (soldats) appartenant au joueur.
    private List<Soldat> unites;
 
    // Liste des villes appartenant au joueur.
    private List<Ville> villes;
 
    /**
     * Constructeur de la classe Joueur.
     * 
     * @param login Identifiant unique du joueur.
     */
    public Joueur(String login) {
        this.login = login;
        this.score = 0; // Initialisation du score à 0
        this.pointsDeProduction = 0; // Initialisation des points de production à 0
        this.unites = new ArrayList<>();
        this.villes = new ArrayList<>();
    }
 
    // Getters et Setters
 
    /**
     * Obtient le login du joueur.
     * @return login du joueur.
     */
    public String getLogin() {
        return login;
    }
 
    /**
     * Définit le login du joueur.
     * @param login Identifiant unique du joueur.
     */
    public void setLogin(String login) {
        this.login = login;
    }
 
    /**
     * Obtient le score du joueur.
     * @return score du joueur.
     */
    public int getScore() {
        return score;
    }
 
    /**
     * Met à jour le score du joueur.
     * @param score Nouveau score à définir.
     */
    public void setScore(int score) {
        this.score = score;
    }
 
    /**
     * Obtient les points de production du joueur.
     * @return points de production du joueur.
     */
    public int getPointsDeProduction() {
        return pointsDeProduction;
    }
 
    /**
     * Met à jour les points de production du joueur.
     * @param pointsDeProduction Nouveaux points de production à définir.
     */
    public void setPointsDeProduction(int pointsDeProduction) {
        this.pointsDeProduction = pointsDeProduction;
    }
 
    /**
     * Obtient la liste des unités du joueur.
     * @return Liste des unités.
     */
    public List<Soldat> getUnites() {
        return unites;
    }
 
    /**
     * Ajoute une unité à la liste des unités du joueur.
     * @param soldat Soldat à ajouter.
     */
    public void ajouterUnite(Soldat soldat) {
        this.unites.add(soldat);
    }
 
    /**
     * Obtient la liste des villes du joueur.
     * @return Liste des villes.
     */
    public List<Ville> getVilles() {
        return villes;
    }
 
    /**
     * Ajoute une ville à la liste des villes du joueur.
     * @param ville Ville à ajouter.
     */
    public void ajouterVille(Ville ville) {
        this.villes.add(ville);
    }
 
    // Méthodes supplémentaires (si nécessaires)
 
    /**
     * Affiche les informations du joueur.
     */
    @Override
    public String toString() {
        return "Joueur{" +
                "login='" + login + '\'' +
                ", score=" + score +
                ", pointsDeProduction=" + pointsDeProduction +
                ", unites=" + unites.size() +
                ", villes=" + villes.size() +
                '}';
    }
}