package model;




/**
 * Classe représentant une forêt, qui est une tuile spéciale avec des ressources de production.
 */
public class Foret extends Tuile {

    /**
     * Quantité de ressources de production disponibles dans la forêt.
     */
    private int ressources;

    /**
     * Constructeur par défaut.
     * Initialise une forêt avec des ressources par défaut.
     */
    public Foret() {
        super(0, 0, TypeTuile.FORET, false); // Appelle le constructeur de Tuile
        this.ressources = 50; // Quantité par défaut
    }

    /**
     * Constructeur avec paramètres.
     * 
     * @param x         Coordonnée X de la forêt.
     * @param y         Coordonnée Y de la forêt.
     * @param ressources Quantité initiale de ressources dans la forêt.
     */
    public Foret(int x, int y, int ressources) {
        super(x, y, TypeTuile.FORET, false); // Définit la forêt comme une tuile
        this.ressources = ressources;
    }

    /**
     * Obtient la quantité de ressources disponibles dans la forêt.
     * 
     * @return La quantité de ressources.
     */
    public int getRessources() {
        return ressources;
    }

    /**
     * Définit la quantité de ressources disponibles dans la forêt.
     * 
     * @param ressources La nouvelle quantité de ressources.
     */
    public void setRessources(int ressources) {
        this.ressources = ressources;
    }

    /**
     * Réduit les ressources de la forêt lorsqu'elles sont exploitées.
     * 
     * @param quantite La quantité de ressources à exploiter.
     * @return La quantité effectivement exploitée (si elle est disponible).
     */
    public int exploiterRessources(int quantite) {
        if (quantite <= ressources) {
            ressources -= quantite;
            return quantite; // Retourne la quantité exploitée
        } else {
            int toutCeQuiReste = ressources;
            ressources = 0; // Plus de ressources disponibles
            return toutCeQuiReste;
        }
    }

    /**
     * Vérifie si la forêt contient encore des ressources.
     * 
     * @return {@code true} si des ressources sont encore disponibles, {@code false} sinon.
     */
    public boolean aDesRessources() {
        return ressources > 0;
    }

    /**
     * Retourne une représentation sous forme de chaîne de caractères des propriétés de la forêt.
     * 
     * @return Une chaîne de caractères décrivant la forêt.
     */
    @Override
    public String toString() {
        return "Foret{" +
                "x=" + getX() +
                ", y=" + getY() +
                ", ressources=" + ressources +
                '}';
    }
}