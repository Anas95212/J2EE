package model;

/**
* Classe représentant un soldat dans le jeu 4X.
*/
public class Soldat {
 
    // Attributs de la classe
 
    /**
     * Coordonnées X de la position du soldat sur la grille.
     */
    private int positionX;
 
    /**
     * Coordonnées Y de la position du soldat sur la grille.
     */
    private int positionY;
 
    /**
     * Points de défense du soldat.
     */
    private int pointsDeDefense;
 
    /**
     * Points de vie du soldat.
     */
    private int pointsDeVie;
 
    /**
     * Indique si le soldat est blessé (true si les points de vie sont inférieurs à 100).
     */
    private boolean blesse;
    private Joueur owner; 
    /**
     * Constructeur par défaut.
     * Initialise un soldat avec des coordonnées (0, 0), des points de défense et des points de vie par défaut.
     */
    public Soldat() {
        this.positionX = 0;
        this.positionY = 0;
        this.pointsDeDefense = 10; // Exemple de valeur par défaut
        this.pointsDeVie = 100; // Exemple de valeur par défaut
        this.blesse = false;
        this.owner = null;
    }
 
    /**
     * Constructeur avec paramètres.
     * Permet de définir les coordonnées initiales, les points de défense et les points de vie du soldat.
     *
     * @param positionX Coordonnées X du soldat.
     * @param positionY Coordonnées Y du soldat.
     * @param pointsDeDefense Points de défense initiaux du soldat.
     * @param pointsDeVie Points de vie initiaux du soldat.
     */
    public Soldat(int positionX, int positionY, int pointsDeDefense, int pointsDeVie, Joueur owner) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.pointsDeDefense = pointsDeDefense;
        this.pointsDeVie = pointsDeVie;
        this.blesse = pointsDeVie < 100;
        this.owner = owner;
    }
 
    // Getters et Setters
    public Joueur getOwner() {
        return owner;
    }
    public void setOwner(Joueur owner) {
        this.owner = owner;
    }
    public int getPositionX() {
        return positionX;
    }
 
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
 
    public int getPositionY() {
        return positionY;
    }
 
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
 
    public int getPointsDeDefense() {
        return pointsDeDefense;
    }
 
    public void setPointsDeDefense(int pointsDeDefense) {
        this.pointsDeDefense = pointsDeDefense;
    }
 
    public int getPointsDeVie() {
        return pointsDeVie;
    }
 
    public void setPointsDeVie(int pointsDeVie) {
        this.pointsDeVie = pointsDeVie;
        this.blesse = pointsDeVie < 100; // Mettre à jour le statut blessé
    }
 
    public boolean isBlesse() {
        return blesse;
    }
 
    // Méthodes supplémentaires
 
    /**
     * Diminue les points de vie du soldat en cas d'attaque.
     * Si les points de vie tombent à zéro ou moins, le soldat est considéré comme mort.
     *
     * @param degats Nombre de points à soustraire des points de vie.
     * @return true si le soldat est encore en vie, false s'il est mort.
     */
    public boolean subirDegats(int degats) {
        this.pointsDeVie -= degats;
        if (this.pointsDeVie <= 0) {
            this.pointsDeVie = 0; // Assurer que les points de vie ne deviennent pas négatifs
            this.blesse = true;
            return false; // Le soldat est mort
        }
        this.blesse = this.pointsDeVie < 100;
        return true; // Le soldat est encore en vie
    }
 
    /**
     * Soigne le soldat, augmentant ses points de vie.
     *
     * @param pointsSoins Nombre de points à ajouter aux points de vie.
     */
    public void soigner(int pointsSoins) {
        this.pointsDeVie += pointsSoins;
        if (this.pointsDeVie > 100) { // Limiter les points de vie à un maximum (par exemple 100)
            this.pointsDeVie = 100;
        }
        this.blesse = this.pointsDeVie < 100; // Mettre à jour le statut blessé
    }
 
    /**
     * Affiche les informations du soldat.
     */
    @Override
    public String toString() {
        return "Soldat{" +
                "positionX=" + positionX +
                ", positionY=" + positionY +
                ", pointsDeDefense=" + pointsDeDefense +
                ", pointsDeVie=" + pointsDeVie +
                ", blesse=" + blesse +
                '}';
    }
}
 