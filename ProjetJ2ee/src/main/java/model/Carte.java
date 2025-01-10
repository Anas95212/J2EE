package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
                TypeTuile bt = tuile.getBaseType();
                Soldat soldat = tuile.getSoldatPresent();
 
                html.append("<td style='position:relative; width:50px; height:50px; ");
 
                // 1) S’il y a un soldat, on prend la couleur du soldat.
                if (soldat != null) {
                    Joueur owner = soldat.getOwner();
                    if (owner != null && owner.getCouleur() != null) {
                        html.append("background-color:").append(owner.getCouleur()).append("; ");
                    }
                }
                // 2) Sinon, si c'est une Ville conquise, on prend la couleur du propriétaire.
                else if (bt == TypeTuile.VILLE && tuile instanceof Ville) {
                    Ville laVille = (Ville) tuile;
                    Joueur prop = laVille.getProprietaire();
                    if (prop != null && prop.getCouleur() != null) {
                        html.append("background-color:").append(prop.getCouleur()).append("; ");
                    }
                }
 
                html.append("'>"); // fin du style='...'
 
                // 3) Afficher l’image de base (forêt, montagne, ville...)
                if (bt == TypeTuile.FORET) {
                    html.append("<div class='background'>")
                        .append("<img src='/ProjetJ2ee/vue/images/forest.png' alt='Forêt' style='width:100%; height:100%;'/>")
                        .append("</div>");
                }
                else if (bt == TypeTuile.MONTAGNE) {
                    html.append("<div class='background'>")
                        .append("<img src='/ProjetJ2ee/vue/images/mountain.png' alt='Montagne' style='width:100%; height:100%;'/>")
                        .append("</div>");
                }
                else if (bt == TypeTuile.VILLE) {
                    // On superpose l’image du château
                    html.append("<div class='background'>")
                        .append("<img src='/ProjetJ2ee/vue/images/castle.png' alt='Ville' style='width:100%; height:100%;'/>")
                        .append("</div>");
                }
                // Pour les cases TypeTuile.VIDE => pas d’image de fond
 
                // 4) S’il y a un soldat, on l’ajoute en premier plan (foreground)
                if (soldat != null) {
                    html.append("<div class='foreground'>")
                        .append("<a href='/ProjetJ2ee/controller?action=selectSoldier")
                        .append("&gameId=").append(gameId)
                        .append("&soldierId=").append(soldat.getPositionX()).append("_").append(soldat.getPositionY())
                        .append("'>")
                        .append("<img src='/ProjetJ2ee/vue/images/knight.png' alt='Soldat' style='width:100%; height:100%;'/>")
                        .append("</a>")
                        .append("</div>");
                }
 
                html.append("</td>");
            }
            html.append("</tr>");
        }
 
        html.append("</table>");
        return html.toString();
    }



    public List<Soldat> getTousSoldats() {
        List<Soldat> resultat = new ArrayList<>();
        for (Tuile t : tuiles) {
            if (t.getSoldatPresent() != null) {
                resultat.add(t.getSoldatPresent());
            }
        }
        return resultat;
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
        int nbForets = 5;
        int nbMontagnes = 15;


        // Placer les villes
        for (int i = 0; i < nbVilles; i++) {
            int[] pos = positions.remove(0);
            // Créer une instance de Ville
            Ville city = new Ville(pos[0], pos[1], 20, null); 
            // city.setPointsDeDefense(100) si tu veux
            // city.setProprietaire(null) => ville neutre

            // On la range dans la liste des tuiles, à l’index calculé
            int index = pos[0] * colonnes + pos[1];
            tuiles.set(index, city); 
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
    public Tuile getTuilesLibresAleatoires() {
        List<Tuile> tuilesLibres = new ArrayList<>();
        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                Tuile tuile = getTuile(i, j);
                if (tuile.isEstVide() && tuile.getBaseType() == TypeTuile.VIDE) {
                    tuilesLibres.add(tuile);
                }
            }
        }
        if (!tuilesLibres.isEmpty()) {
            return tuilesLibres.get(new Random().nextInt(tuilesLibres.size()));
        }
        return null;
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