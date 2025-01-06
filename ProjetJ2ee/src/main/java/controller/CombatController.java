package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import model.Combat;
import model.Partie;
import model.Soldat;
import model.Tuile;
import model.Joueur; 

@WebServlet("/CombatController")
public class CombatController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            sendJsonError(response, "Action manquante");
            return;
        }
        String gameId = request.getParameter("gameId");
        String combatId = request.getParameter("combatId");

        Partie partie = findPartie(gameId);
        if (partie == null) {
            sendJsonError(response, "Partie introuvable");
            return;
        }

        Combat combat = partie.getCombatEnCours();
        if (combat == null || !combat.getCombatId().equals(combatId)) {
            sendJsonError(response, "Combat introuvable ou mismatch ID");
            return;
        }

        switch (action) {
            case "getState":
                handleGetState(response, combat);
                break;
            case "rollDice":
                handleRollDice(response, combat, partie);
                break;
            default:
                sendJsonError(response, "Action inconnue");
                break;
        }
    }

    private void handleGetState(HttpServletResponse response, Combat combat) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"pvSoldat1\":" + combat.getPvSoldat1() + ",");
        out.print("\"pvSoldat2\":" + combat.getPvSoldat2() + ",");
        out.print("\"tourSoldat1\":" + combat.isTourSoldat1() + ",");
        out.print("\"soldat1Owner\":\"" + combat.getSoldat1().getOwner().getLogin() + "\",");
        out.print("\"soldat2Owner\":\"" + combat.getSoldat2().getOwner().getLogin() + "\",");
        out.print("\"enCours\":" + combat.isEnCours());
        out.print("}");
        out.flush();
    }

    
    private void handleRollDice(HttpServletResponse response, Combat combat, Partie partie) throws IOException {
        combat.lancerDeEtAttaquer();

        // Vérification si un soldat est mort
        if (!combat.isEnCours()) {
            // Retirer les soldats morts et notifier les défaites
            if (combat.getPvSoldat1() <= 0) {
                enleverSoldatDeLaCarte(combat.getSoldat1(), partie);
                PartieWebSocket.broadcastDefeat(partie.getGameId(), combat.getSoldat1().getOwner().getLogin());
            }
            if (combat.getPvSoldat2() <= 0) {
                enleverSoldatDeLaCarte(combat.getSoldat2(), partie);
                PartieWebSocket.broadcastDefeat(partie.getGameId(), combat.getSoldat2().getOwner().getLogin());
            }

            // Vérifie si un seul joueur reste en vie (victoire)
            Joueur vainqueur = checkForVictory(partie);
            if (vainqueur != null) {
                PartieWebSocket.broadcastVictory(partie.getGameId(), vainqueur.getLogin());
                PartieWebSocket.broadcastGameEnd(partie.getGameId()); // Fin de la partie
                partie.setCombatEnCours(null); // Fin du combat
                return;
            }

            // Sinon, fin du combat, mais la partie continue
            partie.setCombatEnCours(null);
            PartieWebSocket.broadcastCombatEnd(partie.getGameId());
        }

        // Réponse JSON indiquant que l'action a été effectuée avec succès
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"ok\"}");
    }

    private Joueur checkForVictory(Partie partie) {
        List<Joueur> joueursRestants = new ArrayList<>();
        for (Joueur joueur : partie.getJoueurs()) {
            if (joueur.getUnites().size() > 0) { // Vérifie si le joueur a encore des unités
                joueursRestants.add(joueur);
            }
        }
        return joueursRestants.size() == 1 ? joueursRestants.get(0) : null;
    }





    private Partie findPartie(String gameId) {
        if (gameId == null) return null;
        for (Partie p : PartieWebSocket.getParties()) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }
    private void enleverSoldatDeLaCarte(Soldat s, Partie partie) {
        int x = s.getPositionX();
        int y = s.getPositionY();
        if (x >= 0 && x < partie.getCarte().getLignes() && 
            y >= 0 && y < partie.getCarte().getColonnes()) {

            if (partie.getCarte().getTuile(x, y).getSoldatPresent() == s) {
                partie.getCarte().getTuile(x, y).setSoldatPresent(null);
            }
        }
        // On enlève aussi ce soldat de la liste d'unités du joueur, si besoin
        s.getOwner().getUnites().remove(s);
    }

    private void sendJsonError(HttpServletResponse response, String errorMsg) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + errorMsg + "\"}");
    }
    
    

}
