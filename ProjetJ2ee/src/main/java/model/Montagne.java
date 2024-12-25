package model;



/**
 * Classe représentant une montagne, qui est une tuile spéciale.
 * Une montagne est une tuile sur laquelle les unités ne peuvent pas se déplacer.
 */
public class Montagne extends Tuile {

    /**
     * Constructeur par défaut.
     * Initialise une montagne avec des coordonnées par défaut (0, 0).
     */
    public Montagne() {
        super(0, 0, TypeTuile.MONTAGNE, true); // Une montagne est toujours vide et infranchissable
    }

    /**
     * Constructeur avec paramètres.
     * 
     * @param x Coordonnée X de la montagne.
     * @param y Coordonnée Y de la montagne.
     */
    public Montagne(int x, int y) {
        super(x, y, TypeTuile.MONTAGNE, true);
    }

    /**
     * Retourne une représentation sous forme de chaîne de caractères des propriétés de la montagne.
     * 
     * @return Une chaîne de caractères décrivant la montagne.
     */
    @Override
    public String toString() {
        return "Montagne{" +
                "x=" + getX() +
                ", y=" + getY() +
                '}';
    }
}