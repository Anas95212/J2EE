package model;

import java.util.ArrayList;
import java.util.Collections;
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
    public int getLignes() {
        return lignes;
    }
 
    public void setLignes(int lignes) {
        this.lignes = lignes;
    }
 
    public int getColonnes() {
        return colonnes;
    }
 
    public void setColonnes(int colonnes) {
        this.colonnes = colonnes;
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
            tuile.setBaseType(type);
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
            if (tuile.getBaseType() == TypeTuile.VILLE && !tuile.isEstVide()) {
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
    public String toHTML(String gameId) {
        StringBuilder html = new StringBuilder();
        html.append("<table class='game-grid'>");

        for (int x = 0; x < lignes; x++) {
            html.append("<tr>");
            for (int y = 0; y < colonnes; y++) {
                Tuile tuile = getTuile(x, y);

                // Récupère la baseType
                TypeTuile bt = tuile.getBaseType();

                // Commencer la <td>
                html.append("<td style='position:relative; width:50px; height:50px; ");

                // S’il y a un soldat, on met un fond coloré
                if (tuile.getSoldatPresent() != null) {
                    Joueur owner = tuile.getSoldatPresent().getOwner();
                    if (owner != null && owner.getCouleur() != null) {
                        html.append("background-color:").append(owner.getCouleur()).append("; ");
                    }
                }
                html.append("'>"); // fin du style='...'

                // 1) Afficher l’image correspondant à la baseType
                switch (bt) {
                    case VILLE:
                        html.append("<img src='/ProjetJ2ee/vue/images/castle.png' alt='Ville' style='width:100%;height:100%;'/>");
                        break;
                    case FORET:
                        html.append("<img src='/ProjetJ2ee/vue/images/forest.png' alt='Forêt' style='width:100%;height:100%;'/>");
                        break;
                    case MONTAGNE:
                        html.append("<img src='/ProjetJ2ee/vue/images/mountain.png' alt='Montagne' style='width:100%;height:100%;'/>");
                        break;
                    case VIDE:
                        // rien ou image vide
                        break;
                }

                // 2) Si un soldat est présent, afficher le soldat avec le gameId
                Soldat soldat = tuile.getSoldatPresent();
                if (soldat != null) {
                    String soldierId = soldat.getPositionX() + "_" + soldat.getPositionY();

                    html.append("<a href='/ProjetJ2ee/controller?action=selectSoldier")
                        .append("&gameId=").append(gameId)
                        .append("&soldierId=").append(soldierId)
                        .append("'>")
                        .append("<img src='/ProjetJ2ee/vue/images/knight.png' alt='Soldat' style='width:100%;height:100%;'/>")
                        .append("</a>");
                }

                html.append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }



    
    
    
    /**
     * Initialise une carte de jeu 15x15 avec une rÃ©partition prÃ©dÃ©finie des types de tuiles.
     * Les tuiles sont rÃ©parties comme suit :
     * - Villes (chÃ¢teaux) : PlacÃ©es Ã  des coordonnÃ©es spÃ©cifiques.
     * - Soldats (Ã©pÃ©es) : PlacÃ©s Ã  des coordonnÃ©es spÃ©cifiques.
     * - ForÃªts (arbres) : PlacÃ©es Ã  des coordonnÃ©es spÃ©cifiques.
     * - Montagnes : PlacÃ©es Ã  des coordonnÃ©es spÃ©cifiques.
     * - Les autres tuiles sont dÃ©finies comme vides.
     * 
     * Cette mÃ©thode prÃ©pare une carte fixe adaptÃ©e pour une grille de 15x15.
     */
    public void initialiserCarte() {
        // Initialiser une liste pour contenir toutes les coordonnées de la grille
        List<int[]> positions = new ArrayList<>();
        for (int x = 0; x < lignes; x++) {
            for (int y = 0; y < colonnes; y++) {
                positions.add(new int[] {x, y}); // Ajouter chaque case comme une paire (x, y)
            }
        }

        // Mélanger les positions pour les rendre aléatoires
        Collections.shuffle(positions);

        // Fixer le nombre de tuiles pour chaque type
        int nbVilles = 5;
        int nbForets = 6;
        int nbMontagnes = 10;
        //int nbSoldats = 2;

        // Placer les villes
        for (int i = 0; i < nbVilles; i++) {
            int[] pos = positions.remove(0); // Récupérer une position aléatoire
            mettreAJourTuile(pos[0], pos[1], TypeTuile.VILLE, false);
        }

        // Placer les forêts
        for (int i = 0; i < nbForets; i++) {
            int[] pos = positions.remove(0); // Récupérer une position aléatoire
            mettreAJourTuile(pos[0], pos[1], TypeTuile.FORET, false);
        }

        // Placer les montagnes
        for (int i = 0; i < nbMontagnes; i++) {
            int[] pos = positions.remove(0); // Récupérer une position aléatoire
            mettreAJourTuile(pos[0], pos[1], TypeTuile.MONTAGNE, false);
        }

        // Placer les soldats
        /*for (int i = 0; i < nbSoldats; i++) {
            int[] pos = positions.remove(0); // Récupérer une position aléatoire
            mettreAJourTuile(pos[0], pos[1], TypeTuile.SOLDAT, false);
        }*/

        // Le reste des cases sera vide
        for (int[] pos : positions) {
            mettreAJourTuile(pos[0], pos[1], TypeTuile.VIDE, true);
        }
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
    
    public String toJSON() {
        StringBuilder sb = new StringBuilder("[");
        for (int x = 0; x < lignes; x++) {
            for (int y = 0; y < colonnes; y++) {
                Tuile tuile = getTuile(x, y);
                sb.append("{")
                  .append("\"x\":").append(tuile.getX()).append(",")
                  .append("\"y\":").append(tuile.getY()).append(",")
                  .append("\"type\":\"").append(tuile.getBaseType()).append("\",")
                  .append("\"soldat\":").append(tuile.getSoldatPresent() != null)
                  .append("},");
            }
        }
        // Retire la dernière virgule
        if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

}