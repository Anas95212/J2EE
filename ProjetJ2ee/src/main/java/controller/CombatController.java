package controller;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
 
import model.Combat;
import model.Partie;
import model.Soldat;
import model.Joueur;
import java.util.*;
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
 
        // Recherche de la partie
        Partie partie = findPartie(gameId);
        if (partie == null) {
            sendJsonError(response, "Partie introuvable");
            return;
        }
 
        // Recherche du combat en cours
        Combat combat = partie.getCombatEnCours();
        if (combat == null || !combat.getCombatId().equals(combatId)) {
            sendJsonError(response, "Combat introuvable ou ID incorrect");
            return;
        }
 
        // Traitement de l'action demandée
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
 
    /**
     * Gère la récupération de l'état du combat.
     */
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
 
    /**
     * Gère le lancement du dé et la mise à jour des points de vie.
     */
    private void handleRollDice(HttpServletResponse response, Combat combat, Partie partie) throws IOException {
        // Lancer le dé et infliger les dégâts
        combat.lancerDeEtAttaquer();
 
        // Vérifier si le combat est terminé
        if (!combat.isEnCours()) {	
        	Joueur perdant = null;
        	boolean combatTermine = false;
        	if (combat.getPvSoldat1() <= 0) {
        		System.out.println("On rentre dans le cas soldat 1 pv < 0");
                enleverSoldatDeLaCarte(combat.getSoldat1(), partie);
                combat.getSoldat2().getOwner().incrementerScore(100);
                combatTermine = true;
                perdant = combat.getSoldat1().getOwner();
                //PartieWebSocket.broadcastDefeat(partie.getGameId(),perdant.getLogin());
                
                if(perdant.getUnites().isEmpty()) {
                	PartieWebSocket.broadcastDefeat(partie.getGameId(), combat.getSoldat2().getOwner().getLogin());
                }
                partie.retirerJoueur(perdant);
                
            }
            if (combat.getPvSoldat2() <= 0) {
            	System.out.println("On rentre dans le cas soldat 2 pv < 0");
                enleverSoldatDeLaCarte(combat.getSoldat2(), partie);
                combat.getSoldat1().getOwner().incrementerScore(100);
                //PartieWebSocket.broadcastDefeat(partie.getGameId(), combat.getSoldat2().getOwner().getLogin());
                combatTermine = true;
                perdant = combat.getSoldat2().getOwner();
                if(perdant.getUnites().isEmpty()) {
                	PartieWebSocket.broadcastDefeat(partie.getGameId(), combat.getSoldat2().getOwner().getLogin());
                }
                partie.retirerJoueur(perdant);
            }
            
            Joueur vainqueur = checkForVictory(partie);
            
            if (vainqueur != null) {
            	System.out.println("On rentre dans le cas où y'a un vainqueur");
                PartieWebSocket.broadcastVictory(partie.getGameId(), vainqueur.getLogin());
                PartieWebSocket.broadcastGameEnd(partie.getGameId()); // Fin de la partie
                partie.setCombatEnCours(null); // Fin du combat
                return;
            }
            // Marquer la fin du combat
            partie.setCombatEnCours(null);
            
            
 
            // Informer tous les joueurs de la fin du combat via WebSocket
            //PartieWebSocket.broadcastCombatEnd(partie.getGameId());
            System.out.println("On envoie combat end");
       
        }
 
        // Renvoyer les informations au client
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"valeurDe\":" + combat.getDerniereValeurDe() + ",");
        out.print("\"pvSoldat1\":" + combat.getPvSoldat1() + ",");
        out.print("\"pvSoldat2\":" + combat.getPvSoldat2() + ",");
        out.print("\"tourSoldat1\":" + combat.isTourSoldat1() + ",");
        out.print("\"enCours\":" + combat.isEnCours());
        
        out.print("}");
        out.flush();
    }
 
    /**
     * Recherche une partie en fonction de l'ID.
     */
    private Partie findPartie(String gameId) {
        if (gameId == null) return null;
        return PartieWebSocket.getParties().stream()
                .filter(p -> p.getGameId().equals(gameId))
                .findFirst()
                .orElse(null);
    }
 
    /**
     * Retire un soldat de la carte et du joueur associé.
     */
    private Joueur checkForVictory(Partie partie) {
        List<Joueur> joueursRestants = new ArrayList<>();
        for (Joueur joueur : partie.getJoueurs()) {
            if (joueur.getUnites().size() > 0) { // Vérifie si le joueur a encore des unités
                joueursRestants.add(joueur);
            }
        }
        return joueursRestants.size() == 1 ? joueursRestants.get(0) : null;
    }
    private void enleverSoldatDeLaCarte(Soldat soldat, Partie partie) {
        int x = soldat.getPositionX();
        int y = soldat.getPositionY();
 
        // Suppression du soldat de la carte
        if (x >= 0 && x < partie.getCarte().getLignes() &&
                y >= 0 && y < partie.getCarte().getColonnes()) {
            if (partie.getCarte().getTuile(x, y).getSoldatPresent() == soldat) {
                partie.getCarte().getTuile(x, y).setSoldatPresent(null);
            }
        }
 
        // Suppression du soldat de la liste du joueur
        soldat.getOwner().getUnites().remove(soldat);
    }
 
    /**
     * Envoie un message d'erreur en format JSON.
     */
    private void sendJsonError(HttpServletResponse response, String errorMsg) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + errorMsg + "\"}");
    }
    
    
    
}