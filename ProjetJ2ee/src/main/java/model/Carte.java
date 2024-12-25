package model;

import java.util.ArrayList;
import java.util.List;

import model.Tuile.TypeTuile;

/**
 * Classe représentant une carte de jeu composée d'une grille de tuiles.
 * Optimisée pour une utilisation en environnement multijoueur et dans des vues JSP.
 */
public class Carte {

    /**
     * Nombre de lignes de la carte.
     */
    private int lignes;

    /**
     * Nombre de colonnes de la carte.
     */
    private int colonnes;

    /**
     * Liste des tuiles de la carte, représentée comme une grille.
     */
    private List<Tuile> tuiles;

    /**
     * Constructeur de la classe Carte.
     * Initialise une grille de tuiles vides.
     * 
     * @param lignes  Le nombre de lignes de la grille.
     * @param colonnes Le nombre de colonnes de la grille.
     */
    public Carte(int lignes, int colonnes) {
        this.lignes = lignes;
        this.colonnes = colonnes;
        this.tuiles = new ArrayList<>();

        // Initialisation des tuiles de la grille
        for (int x = 0; x < lignes; x++) {
            for (int y = 0; y < colonnes; y++) {
                tuiles.add(new Tuile(x, y, TypeTuile.VIDE, true));
            }
        }
    }

    /**
     * Obtient une tuile spécifique à partir de ses coordonnées.
     * 
     * @param x La coordonnée X de la tuile.
     * @param y La coordonnée Y de la tuile.
     * @return La tuile correspondante, ou {@code null} si les coordonnées sont invalides.
     */
    public Tuile getTuile(int x, int y) {
        if (x >= 0 && x < lignes && y >= 0 && y < colonnes) {
            return tuiles.get(x * colonnes + y);
        }
        return null;
    }

    /**
     * Met à jour le type d'une tuile à des coordonnées spécifiques.
     * 
     * @param x     La coordonnée X de la tuile.
     * @param y     La coordonnée Y de la tuile.
     * @param type  Le nouveau type de la tuile.
     * @param estVide Indique si la tuile est vide.
     */
    public void mettreAJourTuile(int x, int y, TypeTuile type, boolean estVide) {
        Tuile tuile = getTuile(x, y);
        if (tuile != null) {
            tuile.setType(type);
            tuile.setEstVide(estVide);
        }
    }

    /**
     * Retourne la liste des tuiles contrôlées par un joueur.
     * 
     * @param joueur Le joueur concerné.
     * @return Une liste de tuiles appartenant au joueur.
     */
    public List<Tuile> getTuilesControleesPar(Joueur joueur) {
        List<Tuile> tuilesControlees = new ArrayList<>();
        for (Tuile tuile : tuiles) {
            if (tuile.getType() == TypeTuile.VILLE && !tuile.isEstVide()) {
                // Logique d'attribution au joueur (si nécessaire, adapter cette partie)
                tuilesControlees.add(tuile);
            }
        }
        return tuilesControlees;
    }

    /**
     * Génère une représentation HTML de la carte pour l'intégration dans une vue JSP.
     * 
     * @return Une chaîne de caractères contenant la représentation HTML de la carte.
     */
    public String toHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<table class='game-grid'>");
        for (int x = 0; x < lignes; x++) {
            html.append("<tr>");
            for (int y = 0; y < colonnes; y++) {
                Tuile tuile = getTuile(x, y);
                html.append("<td class='tile ").append(tuile.getType().toString().toLowerCase()).append("'>");
                // Ajouter une image selon le type de tuile
                switch (tuile.getType()) {
                    case VILLE:
                        html.append("<img src='images/castle.png' alt='Ville'/>");
                        break;
                    case FORET:
                        html.append("<img src='images/forest.png' alt='Forêt'/>");
                        break;
                    case MONTAGNE:
                        html.append("<img src='images/mountain.png' alt='Montagne'/>");
                        break;
                    case VIDE:
                        html.append(""); // Rien pour les tuiles vides
                        break;
                }
                html.append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }


    /**
     * Retourne une représentation sous forme de chaîne de caractères de la carte.
     * Affiche chaque tuile et ses propriétés.
     * 
     * @return Une chaîne de caractères décrivant la carte.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < lignes; x++) {
            for (int y = 0; y < colonnes; y++) {
                sb.append(getTuile(x, y).toString()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
