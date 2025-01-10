package model;


/**
 * Classe représentant une ville, qui est une tuile spéciale avec des propriétés supplémentaires.
 */
public class Ville extends Tuile {

    /**
     * Points de défense de la ville.
     * Déterminent sa résistance face aux attaques.
     */
    private int pointsDeDefense;

    /**
     * Propriétaire de la ville.
     * Peut être un joueur ou null si la ville est neutre.
     */
    private Joueur proprietaire;

    /**
     * Constructeur par défaut.
     * Initialise une ville neutre avec des points de défense par défaut.
     */
    public Ville() {
        super(0, 0, TypeTuile.VILLE, false); // Appelle le constructeur de Tuile
        this.pointsDeDefense = 20; // Par défaut
        this.proprietaire = null;  // Ville neutre
    }

    /**
     * Constructeur avec paramètres.
     * 
     * @param x               Coordonnée X de la ville.
     * @param y               Coordonnée Y de la ville.
     * @param pointsDeDefense Points de défense initiaux de la ville.
     * @param proprietaire    Propriétaire initial de la ville.
     */
    public Ville(int x, int y, int pointsDeDefense, Joueur proprietaire) {
        super(x, y, TypeTuile.VILLE, false); // Définit la ville comme une tuile
        this.pointsDeDefense = pointsDeDefense;
        this.proprietaire = proprietaire;
    }

    /**
     * Obtient les points de défense de la ville.
     * 
     * @return Les points de défense.
     */
    public int getPointsDeDefense() {
        return pointsDeDefense;
    }

    /**
     * Définit les points de défense de la ville.
     * 
     * @param pointsDeDefense Les nouveaux points de défense.
     */
    public void setPointsDeDefense(int pointsDeDefense) {
        this.pointsDeDefense = pointsDeDefense;
    }

    /**
     * Obtient le propriétaire de la ville.
     * 
     * @return Le propriétaire de la ville, ou {@code null} si la ville est neutre.
     */
    public Joueur getProprietaire() {
        return proprietaire;
    }

    /**
     * Définit le propriétaire de la ville.
     * 
     * @param proprietaire Le nouveau propriétaire de la ville.
     */
    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    /**
     * Vérifie si la ville est neutre (sans propriétaire).
     * 
     * @return {@code true} si la ville est neutre, {@code false} sinon.
     */
    public boolean estNeutre() {
        return this.proprietaire == null;
    }

    /**
     * Réduit les points de défense de la ville en cas d'attaque.
     * 
     * @param degats Nombre de points à retirer.
     */
    public void subirAttaque(int degats) {
        this.pointsDeDefense -= degats;
        if (this.pointsDeDefense < 0) {
            this.pointsDeDefense = 0; // Empêche les points négatifs
        }
    }

    /**
     * Retourne une représentation sous forme de chaîne de caractères des propriétés de la ville.
     * 
     * @return Une chaîne de caractères décrivant la ville.
     */
    @Override
    public String toString() {
        return "Ville{" +
                "x=" + getX() +
                ", y=" + getY() +
                ", pointsDeDefense=" + pointsDeDefense +
                ", proprietaire=" + (proprietaire != null ? proprietaire.getLogin() : "Neutre") +
                '}';
    }
}