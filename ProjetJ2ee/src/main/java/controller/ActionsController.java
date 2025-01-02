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
        // 1) Récup gameId (obligatoire)
        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("error", "Pas de gameId fourni !");
            redirectToGameJsp(request, response);
            return;
        }

        // 2) Trouver la partie dans la liste
        Partie partie = findPartie(gameId);
        if (partie == null) {
            request.setAttribute("error", "Impossible de trouver la partie " + gameId + " dans la liste statique.");
            redirectToGameJsp(request, response);
            return;
        }

        // 3) Récup pseudo
        String pseudo = (String) request.getSession().getAttribute("loggedUser");
        if (pseudo == null) {
            request.setAttribute("error", "Vous n'êtes pas connecté (loggedUser manquant).");
            redirectToGameJsp(request, response);
            return;
        }

        // 4) Vérifie si c’est bien le tour de ce pseudo
        if (!isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        // 5) Récup direction
        String direction = request.getParameter("direction");
        if (direction == null || direction.isEmpty()) {
            request.setAttribute("error", "Aucune direction précisée (north, south, east, west).");
            redirectToGameJsp(request, response);
            return;
        }

        // 6) Récup soldierId en session
        String soldierId = (String) request.getSession().getAttribute("selectedSoldierId");
        if (soldierId == null) {
            request.setAttribute("error", "Aucun soldat sélectionné.");
            redirectToGameJsp(request, response);
            return;
        }

        // Parse x_y
        String[] coords = soldierId.split("_");
        if (coords.length != 2) {
            request.setAttribute("error", "Identifiant de soldat invalide : " + soldierId);
            redirectToGameJsp(request, response);
            return;
        }
        int oldX = Integer.parseInt(coords[0]);
        int oldY = Integer.parseInt(coords[1]);

        // 7) Récup tuile
        Tuile oldTile = partie.getCarte().getTuile(oldX, oldY);
        if (oldTile == null || oldTile.getSoldatPresent() == null) {
            request.setAttribute("error", "Soldat introuvable sur la tuile (" + soldierId + ").");
            redirectToGameJsp(request, response);
            return;
        }

        Soldat s = oldTile.getSoldatPresent();
        // Vérifier propriétaire
        if (!Objects.equals(s.getOwner().getLogin(), pseudo)) {
            request.setAttribute("error", "Ce soldat ne vous appartient pas.");
            redirectToGameJsp(request, response);
            return;
        }

        // Calculer la nouvelle position
        int newX = oldX, newY = oldY;
        switch (direction) {
            case "north": newX--; break;
            case "south": newX++; break;
            case "east":  newY++; break;
            case "west":  newY--; break;
            default:
                request.setAttribute("error", "Direction inconnue : " + direction);
                redirectToGameJsp(request, response);
                return;
        }

        // Vérifier bornes
        if (newX < 0 || newX >= partie.getCarte().getLignes() ||
            newY < 0 || newY >= partie.getCarte().getColonnes()) {
            request.setAttribute("error", "Déplacement hors limites !");
            redirectToGameJsp(request, response);
            return;
        }

        Tuile newTile = partie.getCarte().getTuile(newX, newY);

        // Vérif MONTAGNE
        if (newTile.getBaseType() == TypeTuile.MONTAGNE) {
            request.setAttribute("error", "Impossible de traverser la montagne !");
            redirectToGameJsp(request, response);
            return;
        }

        // Vérif soldatPresent => combat
        if (newTile.getSoldatPresent() != null) {
            Soldat other = newTile.getSoldatPresent();
            if (!other.getOwner().getLogin().equals(pseudo)) {
                // Combat => rediriger vers combat.jsp
                response.sendRedirect(request.getContextPath() + "/vue/combat.jsp");
                return;
            } else {
                // S'il appartient au même joueur => refus
                request.setAttribute("error", "Un de vos soldats occupe déjà cette case !");
                redirectToGameJsp(request, response);
                return;
            }
        }

        // Vérif VILLE => combat
        if (newTile.getBaseType() == TypeTuile.VILLE) {
            response.sendRedirect(request.getContextPath() + "/vue/combat.jsp");
            return;
        }

        // OK => déplacement
        // Mémoriser oldX/oldY
        request.getSession().setAttribute("undoX", oldX);
        request.getSession().setAttribute("undoY", oldY);

        // Retirer soldat de l'ancienne tuile
        oldTile.setSoldatPresent(null);

        // Placer soldat sur la nouvelle
        newTile.setSoldatPresent(s);
        s.setPositionX(newX);
        s.setPositionY(newY);

        // Mettre à jour soldierId
        request.getSession().setAttribute("selectedSoldierId", newX + "_" + newY);

        request.setAttribute("moveMessage", "Déplacement effectué vers " + direction
                + " (nouvelle position : " + newX + "," + newY + ").");
        
        request.getSession().setAttribute("actionUsedThisTurn", true);
        PartieWebSocket.broadcastGameUpdate(gameId);
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

        if (!isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        partie.nextPlayerTurn();

        // Nettoyer selection
        request.getSession().removeAttribute("selectedSoldierId");
        request.getSession().removeAttribute("undoX");
        request.getSession().removeAttribute("undoY");
        request.setAttribute("endTurnMessage", "Fin de tour. Au suivant !");
        request.getSession().setAttribute("actionUsedThisTurn", false);
        
        PartieWebSocket.broadcastGameUpdate(gameId);
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
}
