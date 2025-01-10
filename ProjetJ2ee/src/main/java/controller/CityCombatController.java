package controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;

import model.Partie;
import model.CombatVille;
import model.Soldat;
import model.Ville;
import model.Joueur;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet gérant les actions de combat Soldat vs Ville
 * (rollDice, riposte, getState).
 */
@WebServlet("/CityCombatController")
public class CityCombatController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Méthode centrale qui reçoit les actions (getState, rollDice, riposte).
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            sendJsonError(response, "Action manquante");
            return;
        }

        String gameId = request.getParameter("gameId");
        String combatId = request.getParameter("combatId");

        // 1) Retrouver la partie
        Partie partie = findPartie(gameId);
        if (partie == null) {
            sendJsonError(response, "Partie introuvable (gameId=" + gameId + ")");
            return;
        }

        // 2) Retrouver le CombatVille en cours
        CombatVille cv = partie.getCombatVilleEnCours();
        if (cv == null || !cv.getCombatId().equals(combatId)) {
            sendJsonError(response, "CombatVille introuvable ou ID incorrect");
            return;
        }

        // 3) Dispatcher selon l'action
        switch (action) {
            case "getState":
                handleGetState(response, cv);
                break;

            case "rollDice":
                handleRollDice(response, cv, partie);
                break;

            case "riposte":
                handleRiposte(response, cv, partie);
                break;

            default:
                sendJsonError(response, "Action inconnue : " + action);
        }
    }

    /**
     * Renvoie l'état du combat en JSON : { pvSoldat, defVille, enCours, tourSoldat }
     */
    private void handleGetState(HttpServletResponse response, CombatVille cv)
            throws IOException {
        sendJsonState(response, cv);
    }

    /**
     * Le soldat attaque la ville (lance le dé).
     */
    private void handleRollDice(HttpServletResponse response, CombatVille cv, Partie partie)
            throws IOException {
        // 1) Lancement de dé => baisse de la défense de la ville
        cv.attaquerVille();

        // Diffuser update en temps réel (pour spectateurs)
        PartieWebSocket.broadcastCombatVilleUpdate(
            partie.getGameId(),
            cv.getPvSoldat(),
            cv.getDefVille(),
            cv.isTourSoldat(),
            cv.getDerniereValeurDe()
        );

        // 2) Si le combat n'est plus en cours => la ville est tombée à 0
        if (!cv.isEnCours()) {
            // => la ville est conquise
            conquerirVille(cv, partie);
            
            // Vérifier si on a un vainqueur global (dernier joueur en vie)
            Joueur vainqueur = checkForVictory(partie);
            if (vainqueur != null) {
                // => Seul survivant => victoooooire !
                PartieWebSocket.broadcastVictory(partie.getGameId(), vainqueur.getLogin());
                // (optionnel) si tu veux supprimer la partie :
                // partieTerminee(partie);
            } else {
                // Sinon => la partie continue => tout le monde retourne sur game.jsp
                PartieWebSocket.broadcastCombatVilleEnd(partie.getGameId());
            }

            // Fin du combat
            partie.setCombatVilleEnCours(null);

            // Envoyer l'état final pour le joueur qui a fait l'attaque
            sendJsonState(response, cv);
            return;
        }

        // 3) Sinon le combat continue => on renvoie l'état pour mettre à jour l'UI
        sendJsonState(response, cv);
    }

    /**
     * La ville riposte (lance le dé).
     */
    private void handleRiposte(HttpServletResponse response, CombatVille cv, Partie partie)
            throws IOException {
        cv.riposteVille();
        PartieWebSocket.broadcastCombatVilleUpdate(partie.getGameId(),
                cv.getPvSoldat(),
                cv.getDefVille(),
                cv.isTourSoldat(),
                cv.getDerniereValeurDe());

        if (!cv.isEnCours()) {
            Soldat s = cv.getSoldatAttaquant();
            //enleverSoldatDeLaCarte(s, partie);

            Joueur j = s.getOwner();
            if (j.getUnites().isEmpty()) {
                // Il est complètement éliminé
                partie.retirerJoueur(j);
                PartieWebSocket.broadcastDefeat(partie.getGameId(), j.getLogin());
                enleverSoldatDeLaCarte(s, partie);
            } else {
                // Défaite partielle
                PartieWebSocket.broadcastDefeat(partie.getGameId(), j.getLogin());
                enleverSoldatDeLaCarte(s, partie);
            }

            Joueur vainqueur = checkForVictory(partie);
            if (vainqueur != null) {
                PartieWebSocket.broadcastVictory(partie.getGameId(), vainqueur.getLogin());
            } else {
                // Personne n’a gagné -> tout le monde (attaquant, adversaire, 
                // potentiels spectateurs) revient sur game.jsp
                PartieWebSocket.broadcastCombatVilleEnd(partie.getGameId());
            }

            partie.setCombatVilleEnCours(null);
            sendJsonState(response, cv);
            return;
        }

        // Combat continue
        sendJsonState(response, cv);
    }


    /**
     * La ville est conquise (défense <= 0).
     * => On met la ville au propriétaire du soldat.
     * => Le soldat conserve ses PV restants.
     * => On donne 50 pièces au soldat attaquant.
     */
    private void conquerirVille(CombatVille cv, Partie partie) {
        Soldat soldat = cv.getSoldatAttaquant();
        Ville ville   = cv.getVilleCible();

        // Affecter la ville
        ville.setProprietaire(soldat.getOwner());
        ville.setPointsDeDefense(10); // si tu veux la réinitialiser

        // Donne 50 pièces (max)
        soldat.getOwner().setPointsDeProduction(50);

        // On libère le combat en cours
        partie.setCombatVilleEnCours(null);
    }

    /**
     * Supprime un soldat de la carte et de la liste d'unités du joueur.
     */
    private void enleverSoldatDeLaCarte(Soldat soldat, Partie partie) {
        int x = soldat.getPositionX();
        int y = soldat.getPositionY();
        if (x >= 0 && x < partie.getCarte().getLignes() &&
            y >= 0 && y < partie.getCarte().getColonnes()) {
            if (partie.getCarte().getTuile(x, y).getSoldatPresent() == soldat) {
                partie.getCarte().getTuile(x, y).setSoldatPresent(null);
            }
        }
        soldat.getOwner().supprimerUnite(soldat);
    }

    /**
     * Renvoie l'état du combat (pvSoldat, defVille, dernierDe, etc.) sous forme JSON.
     */
    private void sendJsonState(HttpServletResponse response, CombatVille cv)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        out.print("{");
        out.print("\"valeurDe\":" + cv.getDerniereValeurDe() + ",");
        out.print("\"pvSoldat\":" + cv.getPvSoldat() + ",");
        out.print("\"defVille\":" + cv.getDefVille() + ",");
        out.print("\"enCours\":" + cv.isEnCours() + ",");
        out.print("\"tourSoldat\":" + cv.isTourSoldat());
        out.print("}");
        out.flush();
    }

    /**
     * Vérifie s'il ne reste qu'un seul joueur avec des unités.
     * S'il n'en reste qu'un, on le considère vainqueur.
     */
    private Joueur checkForVictory(Partie partie) {
        List<Joueur> joueursRestants = new ArrayList<>();
        for (Joueur j : partie.getJoueurs()) {
            if (!j.getUnites().isEmpty()) {
                joueursRestants.add(j);
            }
        }
        return (joueursRestants.size() == 1) ? joueursRestants.get(0) : null;
    }

    /**
     * Retrouve la partie par son gameId dans PartieWebSocket.
     */
    private Partie findPartie(String gameId) {
        if (gameId == null) return null;
        for (Partie p : PartieWebSocket.getParties()) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Renvoie une erreur JSON { "error" : "..." }
     */
    private void sendJsonError(HttpServletResponse response, String msg)
            throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + msg + "\"}");
    }

    // (Optionnel) Méthode si tu veux supprimer la partie
    // private void partieTerminee(Partie partie) {
    //     PartieWebSocket.getParties().remove(partie);
    //     // etc.
    // }

}
