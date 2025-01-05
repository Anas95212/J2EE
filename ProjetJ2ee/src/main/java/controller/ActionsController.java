package controller;

import java.io.IOException;


import java.util.Objects;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.Partie;
import model.Joueur;
import model.Soldat;
import model.Tuile;
import model.Tuile.TypeTuile;

public class ActionsController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            // Renvoie par défaut sur game.jsp
            redirectToGameJsp(request, response);
            return;
        }

        switch (action) {
            case "selectSoldier":
                selectSoldier(request, response);
                break;

            case "move":
                moveSoldier(request, response);
                break;

            case "undo":
                undoMove(request, response);
                break;

            case "endTurn":
                endTurn(request, response);
                break;

            case "attack":
                attack(request, response);
                break;
                
            case "updateState":
                updateState(request, response);
                break;


            default:
                // Action non reconnue, renvoyer vers game.jsp
                redirectToGameJsp(request, response);
                break;
        }
    }

    /**
     * Sélection d'un soldat : on stocke soldierId en session HTTP
     * (c'est juste un identifiant, pas la partie).
     */
    private void selectSoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String soldierId = request.getParameter("soldierId");
        if (soldierId == null) {
            request.setAttribute("error", "Aucun identifiant de soldat fourni.");
            redirectToGameJsp(request, response);
            return;
        }

        // On stocke soldierId dans la session
        request.getSession().setAttribute("selectedSoldierId", soldierId);
        
        redirectToGameJsp(request, response);
    }

    private void moveSoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Boolean actionUsed = (Boolean) request.getSession().getAttribute("actionUsedThisTurn");
        if (actionUsed != null && actionUsed) {
            request.setAttribute("error", "Vous avez déjà effectué une action ce tour-ci !");
            redirectToGameJsp(request, response);
            return;
        }

        String gameId = request.getParameter("gameId");
        Partie partie = findPartie(gameId);
        if (partie == null) {
            request.setAttribute("error", "Partie introuvable !");
            redirectToGameJsp(request, response);
            return;
        }

        String pseudo = (String) request.getSession().getAttribute("loggedUser");
        if (pseudo == null || !isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        String direction = request.getParameter("direction");
        String soldierId = (String) request.getSession().getAttribute("selectedSoldierId");
        if (soldierId == null || direction == null) {
            request.setAttribute("error", "Soldat ou direction invalide !");
            redirectToGameJsp(request, response);
            return;
        }

        int oldX = Integer.parseInt(soldierId.split("_")[0]);
        int oldY = Integer.parseInt(soldierId.split("_")[1]);
        Tuile oldTile = partie.getCarte().getTuile(oldX, oldY);
        Soldat s = oldTile.getSoldatPresent();

        int newX = oldX, newY = oldY;
        switch (direction) {
            case "north": newX--; break;
            case "south": newX++; break;
            case "east":  newY++; break;
            case "west":  newY--; break;
        }

        Tuile newTile = partie.getCarte().getTuile(newX, newY);
        if (newTile == null || newTile.getSoldatPresent() != null) {
            request.setAttribute("error", "Déplacement impossible !");
            redirectToGameJsp(request, response);
            return;
        }

        // Déplacement
        oldTile.setSoldatPresent(null);
        newTile.setSoldatPresent(s);
        s.setPositionX(newX);
        s.setPositionY(newY);
        request.getSession().setAttribute("selectedSoldierId", newX + "_" + newY);
        request.getSession().setAttribute("actionUsedThisTurn", true);

        // Diffusion uniquement si nécessaire
        if (isPlayerTurn(partie, pseudo)) {
            PartieWebSocket.broadcastGameUpdate(gameId);
        }
        System.out.println("[ActionsController] Déplacement de " + pseudo + " vers (" + newX + "," + newY + ")");
        redirectToGameJsp(request, response);
    }



    private void undoMove(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("error", "Pas de gameId !");
            redirectToGameJsp(request, response);
            return;
        }
        Partie partie = findPartie(gameId);
        if (partie == null) {
            request.setAttribute("error", "Impossible de trouver la partie " + gameId);
            redirectToGameJsp(request, response);
            return;
        }

        String pseudo = (String) request.getSession().getAttribute("loggedUser");
        if (pseudo == null) {
            request.setAttribute("error", "Vous n'êtes pas connecté.");
            redirectToGameJsp(request, response);
            return;
        }

        if (!isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        String soldierId = (String) request.getSession().getAttribute("selectedSoldierId");
        if (soldierId == null) {
            request.setAttribute("error", "Aucun soldat sélectionné pour annuler.");
            redirectToGameJsp(request, response);
            return;
        }

        Integer oldX = (Integer) request.getSession().getAttribute("undoX");
        Integer oldY = (Integer) request.getSession().getAttribute("undoY");
        if (oldX == null || oldY == null) {
            request.setAttribute("error", "Aucun déplacement précédent à annuler.");
            redirectToGameJsp(request, response);
            return;
        }

        String[] coords = soldierId.split("_");
        if (coords.length != 2) {
            request.setAttribute("error", "Identifiant soldat invalide : " + soldierId);
            redirectToGameJsp(request, response);
            return;
        }

        int currentX = Integer.parseInt(coords[0]);
        int currentY = Integer.parseInt(coords[1]);

        Tuile currentTile = partie.getCarte().getTuile(currentX, currentY);
        if (currentTile == null || currentTile.getSoldatPresent() == null) {
            request.setAttribute("error", "Impossible de localiser le soldat actuel.");
            redirectToGameJsp(request, response);
            return;
        }

        Soldat s = currentTile.getSoldatPresent();
        if (!s.getOwner().getLogin().equals(pseudo)) {
            request.setAttribute("error", "Ce soldat ne vous appartient pas.");
            redirectToGameJsp(request, response);
            return;
        }

        // On retire le soldat de la tuile actuelle
        currentTile.setSoldatPresent(null);

        // On le remet sur la tuile oldX,oldY
        Tuile oldTile = partie.getCarte().getTuile(oldX, oldY);
        if (oldTile == null) {
            request.setAttribute("error", "Tuile précédente introuvable.");
            redirectToGameJsp(request, response);
            return;
        }
        if (oldTile.getSoldatPresent() != null) {
            request.setAttribute("error", "La tuile précédente est déjà occupée !");
            redirectToGameJsp(request, response);
            return;
        }

        oldTile.setSoldatPresent(s);
        s.setPositionX(oldX);
        s.setPositionY(oldY);

        // Nettoyer undoX,undoY
        request.getSession().removeAttribute("undoX");
        request.getSession().removeAttribute("undoY");

        // Mettre à jour soldierId
        request.getSession().setAttribute("selectedSoldierId", oldX + "_" + oldY);
        request.getSession().setAttribute("actionUsedThisTurn", false);
        request.setAttribute("undoMessage", "Déplacement annulé, retour sur (" + oldX + "," + oldY + ").");
        PartieWebSocket.broadcastGameUpdate(gameId);
        redirectToGameJsp(request, response);
    }

    private void endTurn(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Récupération des données nécessaires
        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("error", "Pas de gameId !");
            redirectToGameJsp(request, response);
            return;
        }

        Partie partie = findPartie(gameId);
        if (partie == null) {
            request.setAttribute("error", "Partie introuvable !");
            redirectToGameJsp(request, response);
            return;
        }

        String pseudo = (String) request.getSession().getAttribute("loggedUser");
        if (pseudo == null) {
            request.setAttribute("error", "Vous n'êtes pas connecté.");
            redirectToGameJsp(request, response);
            return;
        }

        // 2. Vérification que c'est bien le tour du joueur
        if (!isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        // 3. Passage explicite au tour suivant
        partie.nextPlayerTurn(true); // Le booléen "true" indique qu'il s'agit d'une fin de tour légitime

        // 4. Nettoyage des données de session liées à l'action
        request.getSession().removeAttribute("selectedSoldierId");
        request.getSession().removeAttribute("undoX");
        request.getSession().removeAttribute("undoY");
        request.setAttribute("endTurnMessage", "Fin de tour. Au suivant !");
        request.getSession().setAttribute("actionUsedThisTurn", false);

        System.out.println("Fin de tour exécutée pour le joueur : " + pseudo);

        // 5. Diffusion du message de mise à jour sans déclencher de changement de tour supplémentaire
        PartieWebSocket.broadcastRefresh(gameId);

        // 6. Redirection vers la page de jeu
        redirectToGameJsp(request, response);
    }





    private void attack(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("attackMessage", "Attaque lancée (non implémentée).");
        redirectToGameJsp(request, response);
    }

    /**
     * Vérifie si c'est bien le tour du pseudo dans la partie
     */
    private boolean isPlayerTurn(Partie partie, String pseudo) {
        if (partie.getJoueurs().isEmpty()) return false;
        Joueur currentPlayer = partie.getJoueurs().get(partie.getIndexJoueurActuel());
        return currentPlayer.getLogin().equals(pseudo);
    }

    /**
     * Recherche la partie dans la liste statique de PartieWebSocket
     */
    private Partie findPartie(String gameId) {
        for (Partie p : PartieWebSocket.getParties()) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * En cas d'erreur ou par défaut, on renvoie sur 'game.jsp'
     * (il faudra que 'game.jsp' lise ?gameId=... pour retouver la partie).
     */
    private void redirectToGameJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // On peut relire gameId (optionnel) pour rajouter ?gameId=... si on veut
        String gameId = request.getParameter("gameId");
        if (gameId == null) {
            // Juste forward
            RequestDispatcher rd = request.getRequestDispatcher("/vue/game.jsp");
            rd.forward(request, response);
        } else {
            // Rediriger en GET => /vue/game.jsp?gameId=...
            // Ou forward
            RequestDispatcher rd = request.getRequestDispatcher("/vue/game.jsp?gameId="+gameId);
            rd.forward(request, response);
        }
    }
    
    private void updateState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String gameId = request.getParameter("gameId");
        Partie partie = findPartie(gameId);

        if (partie == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Partie introuvable\"}");
            return;
        }

        // Récupère les données nécessaires pour la mise à jour
        String htmlCarte = partie.getCarte().toHTML(gameId);
        String currentPlayer = partie.getJoueurs().get(partie.getIndexJoueurActuel()).getLogin();

        // Retourne une réponse JSON
        response.setContentType("application/json");
        response.getWriter().write("{\"htmlCarte\":\"" + htmlCarte.replace("\"", "\\\"") + "\", \"currentPlayer\":\"" + currentPlayer + "\"}");
    }

}
