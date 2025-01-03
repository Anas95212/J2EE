package model;



/**
 * Classe représentant une tuile dans la grille du jeu.
 * Une tuile peut avoir un type, des coordonnées et être vide ou occupée.
 */
public class Tuile {

	
	/**
	 * Enumération représentant les différents types de tuiles possibles dans le jeu.
	 */
	public enum TypeTuile {
	    VILLE, FORET, MONTAGNE, SOLDAT, VIDE
	}
	
    /**
     * Coordonnée X de la tuile.
     */
    private int x;

    /**
     * Coordonnée Y de la tuile.
     */
    private int y;

    /**
     * Type de la tuile (VILLE, FORET, MONTAGNE, VIDE).
     */
    private TypeTuile baseType;

    /**
     * Indicateur si la tuile est vide ou occupée.
     */
    private boolean estVide;
    private Soldat soldatPresent;
    /**
     * Constructeur par défaut.
     * Initialise une tuile vide avec des coordonnées (0, 0).
     */
    public Tuile() {
        this.x = 0;
        this.y = 0;
        this.baseType = TypeTuile.VIDE;
        this.estVide = true;
        this.soldatPresent = null;
    }

    /**
     * Constructeur avec paramètres.
     * 
     * @param x       Coordonnée X de la tuile.
     * @param y       Coordonnée Y de la tuile.
     * @param type    Type de la tuile.
     * @param estVide Indique si la tuile est vide.
     */
    public Tuile(int x, int y, TypeTuile type, boolean estVide) {
        this.x = x;
        this.y = y;
        this.baseType = type;
        this.estVide = estVide;
        this.soldatPresent = null;
    }
    public Soldat getSoldatPresent() {
        return soldatPresent;
    }

    public void setSoldatPresent(Soldat soldatPresent) {
        this.soldatPresent = soldatPresent;
        // Si un soldat est présent => pas vide
        // (ou, si on enlève le soldat => redevient estVide = false si c’est FORET ? A adapter)
        if (soldatPresent != null) {
            this.estVide = false;
        } else {
            // On ne met pas forcément estVide=true, car la baseType peut être FORET
            // "Vide" veut dire "pas de soldat ni de ville ni autre occupant"
            // Donc à toi d’adapter selon la logique
            if (baseType == TypeTuile.VIDE) {
                this.estVide = true;
            }
        }
    }
    /**
     * Obtient la coordonnée X de la tuile.
     * 
     * @return La coordonnée X.
     */
    public int getX() {
        return x;
    }

    /**
     * Définit la coordonnée X de la tuile.
     * 
     * @param x La nouvelle coordonnée X.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Obtient la coordonnée Y de la tuile.
     * 
     * @return La coordonnée Y.
     */
    public int getY() {
        return y;
    }

    /**
     * Définit la coordonnée Y de la tuile.
     * 
     * @param y La nouvelle coordonnée Y.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Obtient le type de la tuile.
     * 
     * @return Le type de la tuile (VILLE, FORET, MONTAGNE, VIDE).
     */
    public TypeTuile getBaseType() {
        return baseType;
    }

    /**
     * Définit le type de la tuile.
     * 
     * @param type Le nouveau type de la tuile.
     */
    public void setBaseType(TypeTuile type) {
        this.baseType = type;
    }

    /**
     * Indique si la tuile est vide ou non.
     * 
     * @return {@code true} si la tuile est vide, {@code false} sinon.
     */
    public boolean isEstVide() {
        return estVide;
    }

    /**
     * Définit si la tuile est vide ou non.
     * 
     * @param estVide {@code true} si la tuile est vide, {@code false} sinon.
     */
    public void setEstVide(boolean estVide) {
        this.estVide = estVide;
    }

    /**
     * Vérifie si la tuile est vide.
     * 
     * @return {@code true} si la tuile est vide, {@code false} sinon.
     */
    public boolean estVide() {
        return this.estVide;
    }

    /**
     * Retourne une représentation sous forme de chaîne de caractères des propriétés de
     * la tuile.
     * 
     * @return Une chaîne de caractères décrivant la tuile.
     */
    @Override
    public String toString() {
        return "Tuile{" +
                "x=" + x +
                ", y=" + y +
                ", baseType=" + baseType +
                ", estVide=" + estVide +
                ", soldatPresent=" + (soldatPresent != null) +
                '}';
    }
}
