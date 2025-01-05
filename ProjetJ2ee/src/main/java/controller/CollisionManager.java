package controller;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Partie;
import model.Soldat;
import model.Tuile;
import model.Combat; // On importe la classe Combat
 
import java.io.IOException;
import java.util.Objects;
 
/**
* Classe pour gérer les collisions entre soldats dans le jeu.
*/
public class CollisionManager {
 
    /**
     * Gère la collision entre deux soldats et lance le combat.
     *
     * @param partie      La partie en cours.
     * @param attaquant   Le soldat attaquant.
     * @param defenseur   Le soldat défenseur.
     * @param gameId      L'identifiant de la partie.
     * @param request     La requête HTTP.
     * @param response    La réponse HTTP.
     */
    public static void gererCollision(Partie partie, Soldat attaquant, Soldat defenseur, String gameId,
                                      HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (partie == null || attaquant == null || defenseur == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être nuls.");
        }
 
        // 1. Créer une instance de Combat et l'ajouter à la partie
        Combat combat = new Combat(attaquant, defenseur);
        partie.setCombatEnCours(combat);
 
        System.out.println("Combat initié entre :");
        System.out.println("Attaquant : " + attaquant.getOwner().getLogin() + " (PV = " + attaquant.getPointsDeVie() + ")");
        System.out.println("Défenseur : " + defenseur.getOwner().getLogin() + " (PV = " + defenseur.getPointsDeVie() + ")");
 
        // 2. Rediriger vers la page `combat.jsp` avec le gameId
        response.sendRedirect(request.getContextPath() + "/vue/combat.jsp?gameId=" + gameId);
    }
 
    /**
     * Tente de déplacer un soldat et gère les collisions si un autre soldat est présent.
     *
     * @param partie       La partie en cours.
     * @param soldat       Le soldat qui se déplace.
     * @param direction    La direction du déplacement (north, south, east, west).
     * @param gameId       L'identifiant de la partie.
     * @param request      La requête HTTP.
     * @param response     La réponse HTTP.
     */
    public static void deplacerSoldat(Partie partie, Soldat soldat, String direction, String gameId,
                                      HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (partie == null || soldat == null || direction == null || gameId == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être nuls.");
        }
 
        // Calculer la nouvelle position en fonction de la direction
        int newX = soldat.getPositionX();
        int newY = soldat.getPositionY();
 
        switch (direction) {
            case "north":
                newX--;
                break;
            case "south":
                newX++;
                break;
            case "east":
                newY++;
                break;
            case "west":
                newY--;
                break;
            default:
                throw new IllegalArgumentException("Direction invalide : " + direction);
        }
 
        // Vérifier les limites de la carte
        if (newX < 0 || newX >= partie.getCarte().getLignes() || newY < 0 || newY >= partie.getCarte().getColonnes()) {
            System.out.println("Déplacement hors limites !");
            return;
        }
 
        Tuile nouvelleTuile = partie.getCarte().getTuile(newX, newY);
 
        if (nouvelleTuile == null) {
            System.out.println("Tuile introuvable !");
            return;
        }
 
        // Vérifier si la tuile contient un autre soldat
        if (nouvelleTuile.getSoldatPresent() != null) {
            Soldat defenseur = nouvelleTuile.getSoldatPresent();
 
            // Vérifier si le défenseur appartient à un joueur différent
            if (!Objects.equals(soldat.getOwner().getLogin(), defenseur.getOwner().getLogin())) {
                // Collision entre un attaquant et un défenseur : lancer le combat
                gererCollision(partie, soldat, defenseur, gameId, request, response);
            } else {
                System.out.println("Un soldat allié est déjà présent sur la tuile.");
            }
        } else {
            // Déplacement sans collision
            Tuile ancienneTuile = partie.getCarte().getTuile(soldat.getPositionX(), soldat.getPositionY());
            if (ancienneTuile != null) {
                ancienneTuile.setSoldatPresent(null);
            }
 
            nouvelleTuile.setSoldatPresent(soldat);
            soldat.setPositionX(newX);
            soldat.setPositionY(newY);
 
            System.out.println("Soldat déplacé vers la tuile (" + newX + ", " + newY + ").");
        }
    }
}