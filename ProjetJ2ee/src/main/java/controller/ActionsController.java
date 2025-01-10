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
import model.Ville;
import model.Combat;
import model.CombatVille;

/**
 * ActionsController gère toutes les actions de jeu :
 * - Sélectionner un soldat
 * - Déplacement
 * - Annuler un déplacement
 * - Fin de tour
 * - Attaquer
 * - Détection de collision
 * - Lancement/fin de Combat
 * - Mise à jour (updateState)
 */
public class ActionsController {

    /**
     * Méthode principale qui reçoit la requête et envoie vers l'action appropriée.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            // Par défaut, redirige vers game.jsp
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

            case "updateState":
                updateState(request, response);
                break;
            
            case "addLifeToSoldier":
                addLifeToSoldier(request, response);
                break;
            case "buySoldier":
            	buySoldier(request, response);
            	break;
            case "DoNothing":
            	endTurn(request, response);
                break;

            default:
                // Action non reconnue => retour sur game.jsp
                redirectToGameJsp(request, response);
                break;
        }
    }

    /**
     * 1) Sélection d'un soldat (stocke soldierId en session HTTP)
     */
    private void selectSoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String soldierId = request.getParameter("soldierId");
        if (soldierId == null) {
            request.setAttribute("error", "Aucun identifiant de soldat fourni.");
            redirectToGameJsp(request, response);
            return;
        }
        // Stocke soldierId en session
        request.getSession().setAttribute("selectedSoldierId", soldierId);

        redirectToGameJsp(request, response);
    }

    /**
     * 2) Méthode de déplacement : 
     *    - Vérifie la validité (tour, soldierId)
     *    - Calcule la nouvelle position
     *    - Vérifie collision => si ennemi, on lance un Combat
     *    - Sinon on met simplement à jour la tuile
     */
    private void moveSoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // (Optionnel) Vérifier si déjà une action utilisée ce tour ?
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

        // Retrouve le soldat à partir de soldierId (ex: "x_y")
        int oldX = Integer.parseInt(soldierId.split("_")[0]);
        int oldY = Integer.parseInt(soldierId.split("_")[1]);
        Tuile oldTile = partie.getCarte().getTuile(oldX, oldY);
        if (oldTile == null || oldTile.getSoldatPresent() == null) {
            request.setAttribute("error", "Soldat introuvable sur la tuile d'origine.");
            redirectToGameJsp(request, response);
            return;
        }

        Soldat s = oldTile.getSoldatPresent();
        if (!s.getOwner().getLogin().equals(pseudo)) {
            request.setAttribute("error", "Ce soldat ne vous appartient pas.");
            redirectToGameJsp(request, response);
            return;
        }

        // Calcul de la nouvelle position
        int newX = oldX, newY = oldY;
        switch (direction) {
            case "north": newX--; break;
            case "south": newX++; break;
            case "east":  newY++; break;
            case "west":  newY--; break;
        }

        // Vérifier limites
        if (newX < 0 || newX >= partie.getCarte().getLignes() ||
            newY < 0 || newY >= partie.getCarte().getColonnes()) {
            request.setAttribute("error", "Déplacement hors-limites !");
            redirectToGameJsp(request, response);
            return;
        }

        Tuile newTile = partie.getCarte().getTuile(newX, newY);
        if (newTile == null) {
            request.setAttribute("error", "Tuile cible introuvable !");
            redirectToGameJsp(request, response);
            return;
        }

        // Vérif MONTAGNE
        if (newTile.getBaseType() == TypeTuile.MONTAGNE) {
            request.setAttribute("error", "Impossible de traverser la montagne !");
            redirectToGameJsp(request, response);
            return;
        }

        // Vérification collision avec un soldat
        Soldat defenseur = newTile.getSoldatPresent();
        if (defenseur != null) {
            // S'il y a un soldat sur cette tuile => ennemi ou allié ?
            if (!defenseur.getOwner().getLogin().equals(pseudo)) {
                // ENNEMI => Lancer un combat soldat vs soldat
                doCombat(partie, s, defenseur, gameId, request, response);
                return;
            } else {
                // ALLIÉ => collision non autorisée
                request.setAttribute("error", "Un de vos soldats est déjà sur cette tuile.");
                redirectToGameJsp(request, response);
                return;
            }
        }

     // Vérification si la tuile est une Ville
        if (newTile.getBaseType() == TypeTuile.VILLE && newTile instanceof Ville) {
            Ville ville = (Ville) newTile;

            // Cas : la ville a déjà un propriétaire
            if (ville.getProprietaire() != null) {
                // Si c'est le même que le soldat => PAS de combat
                if (ville.getProprietaire().equals(s.getOwner())) {
                    // ==> On se déplace VRAIMENT sur la ville
                    oldTile.setSoldatPresent(null);
                    newTile.setSoldatPresent(s);
                    s.setPositionX(newX);
                    s.setPositionY(newY);

                    // Stocker undo si on veut annuler plus tard
                    request.getSession().setAttribute("undoX", oldX);
                    request.getSession().setAttribute("undoY", oldY);

                    // Marquer l'action comme utilisée
                    request.getSession().setAttribute("actionUsedThisTurn", true);

                    // Mettre soldierId à jour
                    request.getSession().setAttribute("selectedSoldierId", newX + "_" + newY);

                    // Rediriger vers la page de jeu
                    redirectToGameJsp(request, response);
                    return;

                } else {
                    // Ville ennemie => Combat Soldat vs Ville
                    CombatVille combatVille = new CombatVille(s, ville);
                    partie.setCombatVilleEnCours(combatVille);
                    PartieWebSocket.broadcastCombatVilleStart(gameId, combatVille.getCombatId());

                    response.sendRedirect(request.getContextPath()
                         + "/vue/cityCombat.jsp?gameId=" + gameId
                         + "&combatId=" + combatVille.getCombatId());
                    return;
                }
            } else {
                // Ville neutre => lancer un combat Soldat vs Ville
                CombatVille combatVille = new CombatVille(s, ville);
                partie.setCombatVilleEnCours(combatVille);
                PartieWebSocket.broadcastCombatVilleStart(gameId, combatVille.getCombatId());

                response.sendRedirect(request.getContextPath()
                     + "/vue/cityCombat.jsp?gameId=" + gameId
                     + "&combatId=" + combatVille.getCombatId());
                return;
            }
        }


        // Aucune collision ni ville => on déplace simplement le soldat
        oldTile.setSoldatPresent(null);
        newTile.setSoldatPresent(s);
        s.setPositionX(newX);
        s.setPositionY(newY);

        // Si c'est une forêt => gain de 5 pièces (max 50)
        if (newTile.getBaseType() == TypeTuile.FORET) {
            Joueur owner = s.getOwner();
            if (owner != null) {
                int currentPoints = owner.getPointsDeProduction();
                if (currentPoints < 50) {
                    currentPoints = currentPoints + 5;
                    if (currentPoints > 50) currentPoints = 50;
                    owner.setPointsDeProduction(currentPoints);

                    String message = "Le joueur " + owner.getLogin()
                            + " a gagné 5 points de production en entrant dans une forêt. Total : "
                            + currentPoints + " pièces.";
                    request.getSession().setAttribute("dernierMessage", message);
                } else {
                    String message = "Le joueur " + owner.getLogin() 
                            + " a atteint le maximum de 50 pièces. Aucun point ajouté.";
                    request.getSession().setAttribute("dernierMessage", message);
                }
            }
        }

        // Stocker undo si on veut l'annuler
        request.getSession().setAttribute("undoX", oldX);
        request.getSession().setAttribute("undoY", oldY);

        // Marque l'action comme utilisée
        request.getSession().setAttribute("actionUsedThisTurn", true);

        // Mettre soldierId à jour
        request.getSession().setAttribute("selectedSoldierId", newX + "_" + newY);

        // (Optionnel) Broadcast update
        //PartieWebSocket.broadcastGameUpdate(gameId);

        redirectToGameJsp(request, response);
    }


    /**
     * 3) Annuler le déplacement (undoMove)
     *    - On repart sur (undoX,undoY) si possible.
     */
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

        // On le remet sur la tuile oldX, oldY
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

        // soldierId redevient oldX_oldY
        request.getSession().setAttribute("selectedSoldierId", oldX + "_" + oldY);

        // On libère l'action => on peut se redéplacer
        request.getSession().setAttribute("actionUsedThisTurn", false);

        request.setAttribute("undoMessage", "Déplacement annulé, retour sur (" + oldX + "," + oldY + ").");

        //PartieWebSocket.broadcastGameUpdate(gameId);
        redirectToGameJsp(request, response);
    }

    /**
     * 4) Fin de tour => nextPlayerTurn
     */
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

        // Passe au joueur suivant
        partie.nextPlayerTurn(true);

        // Nettoie la session
        request.getSession().removeAttribute("selectedSoldierId");
        request.getSession().removeAttribute("undoX");
        request.getSession().removeAttribute("undoY");
        request.getSession().setAttribute("actionUsedThisTurn", false);

        request.setAttribute("endTurnMessage", "Fin de tour, au suivant !");

        PartieWebSocket.broadcastRefresh(gameId);
        redirectToGameJsp(request, response);
    }

    /**
     * 6) Mise à jour d'état en JSON => "/controller?action=updateState&gameId=..."
     *    Utilisé par fetch() depuis game.jsp ?
     */
    private void updateState(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String gameId = request.getParameter("gameId");
        Partie partie = findPartie(gameId);

        if (partie == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Partie introuvable\"}");
            return;
        }

        // (Exemple) On génère un JSON qui contient la carte HTML + currentPlayer
        String htmlCarte = partie.getCarte().toHTML(gameId); 
        String currentPlayer = partie.getJoueurs().get(partie.getIndexJoueurActuel()).getLogin();

        // Renvoyer un JSON
        response.setContentType("application/json");
        // Remplacer les " par \"
        String safeHtml = htmlCarte.replace("\"", "\\\"");
        response.getWriter().write("{\"htmlCarte\":\"" + safeHtml + "\", \"currentPlayer\":\"" + currentPlayer + "\"}");
    }

    /**
     * Méthode pour créer un objet Combat et déclencher la redirection par WebSocket
     * (cas de collision).
     */
    private void doCombat(Partie partie, Soldat attaquant, Soldat defenseur,
            String gameId, HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		// Crée le Combat
		Combat combat = new Combat(attaquant, defenseur);
		
		// Stocke dans la partie
		partie.setCombatEnCours(combat);
		
		// Diffuse l'info via WebSocket si nécessaire
		PartieWebSocket.broadcastCombatStart(gameId, combat.getCombatId());
		
		// Redirection vers la page combat.jsp
		String redirectUrl = request.getContextPath() + "/vue/combat.jsp?gameId=" + gameId + "&combatId=" + combat.getCombatId();
		response.sendRedirect(redirectUrl);
		}


    /**
     * Vérifie si c'est le tour de ce pseudo dans cette partie.
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
        if (gameId == null) return null;
        for (Partie p : PartieWebSocket.getParties()) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Redirige par défaut vers 'game.jsp' (avec ou sans ?gameId=...).
     */
    private void redirectToGameJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String gameId = request.getParameter("gameId");
        if (gameId == null) {
            RequestDispatcher rd = request.getRequestDispatcher("/vue/game.jsp");
            rd.forward(request, response);
        } else {
            RequestDispatcher rd = request.getRequestDispatcher("/vue/game.jsp?gameId=" + gameId);
            rd.forward(request, response);
        }
    }
    
    
    private void addLifeToSoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Récupérer gameId et vérifier sa validité
        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("error", "Pas de gameId fourni !");
            redirectToGameJsp(request, response);
            return;
        }

        // 2) Récupérer la partie
        Partie partie = findPartie(gameId);
        if (partie == null) {
            request.setAttribute("error", "Partie introuvable !");
            redirectToGameJsp(request, response);
            return;
        }

        // 3) Récupérer le pseudo du joueur connecté
        String pseudo = (String) request.getSession().getAttribute("loggedUser");
        if (pseudo == null) {
            request.setAttribute("error", "Vous n'êtes pas connecté.");
            redirectToGameJsp(request, response);
            return;
        }

        // 4) Vérifier que c'est le tour du joueur
        if (!isPlayerTurn(partie, pseudo)) {
            request.setAttribute("error", "Ce n'est pas votre tour !");
            redirectToGameJsp(request, response);
            return;
        }

        // 5) Récupérer le soldat sélectionné
        String soldierId = (String) request.getSession().getAttribute("selectedSoldierId");
        if (soldierId == null) {
            request.setAttribute("error", "Aucun soldat sélectionné.");
            redirectToGameJsp(request, response);
            return;
        }

        // 6) Identifier la position du soldat
        String[] coords = soldierId.split("_");
        if (coords.length != 2) {
            request.setAttribute("error", "Identifiant de soldat invalide.");
            redirectToGameJsp(request, response);
            return;
        }
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        // 7) Récupérer la tuile du soldat
        Tuile tile = partie.getCarte().getTuile(x, y);
        if (tile == null || tile.getSoldatPresent() == null) {
            request.setAttribute("error", "Soldat introuvable à cette position.");
            redirectToGameJsp(request, response);
            return;
        }

        // 8) Récupérer le soldat
        Soldat soldat = tile.getSoldatPresent();
        if (!soldat.getOwner().getLogin().equals(pseudo)) {
            request.setAttribute("error", "Ce soldat ne vous appartient pas.");
            redirectToGameJsp(request, response);
            return;
        }

        // 9) Ajouter de la vie au soldat
        int currentLife = soldat.getPointsDeVie();
        int maxLife = 20;
        if (currentLife < maxLife) {
            soldat.setPointsDeVie(20);

            // Confirmer l'ajout de vie
            request.setAttribute("lifeMessage", "Vous avez ajouté " + 20 + " points de vie à votre soldat.");

        } else {
            request.setAttribute("errorlife", "Ce soldat a déjà toute sa vie, veuillez effectuer une autre action !");
        }

        // 10) Rediriger vers la page de jeu avec les mises à jour
        //PartieWebSocket.broadcastGameUpdate(gameId);
        redirectToGameJsp(request, response);
    }
    private void buySoldier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String gameId = request.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("error", "Pas de gameId fourni !");
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

        Joueur joueur = partie.getJoueurs().stream()
                .filter(j -> j.getLogin().equals(pseudo))
                .findFirst()
                .orElse(null);

        if (joueur == null) {
            request.setAttribute("error", "Joueur introuvable !");
            redirectToGameJsp(request, response);
            return;
        }

        // Vérifier si le joueur a suffisamment de pièces
        if (joueur.getPointsDeProduction() < 15) {
            request.setAttribute("error", "Pas assez de pièces pour acheter un soldat !");
            redirectToGameJsp(request, response);
            return;
        }

        // Soustraire le coût
        joueur.setPointsDeProduction(joueur.getPointsDeProduction() - 15);

        // Ajouter un soldat sur une tuile aléatoire
        Tuile tuileDisponible = partie.getCarte().getTuilesLibresAleatoires();
        if (tuileDisponible != null) {
            Soldat nouveauSoldat = new Soldat(0,0,10,100,joueur);
            nouveauSoldat.setPositionX(tuileDisponible.getX());
            nouveauSoldat.setPositionY(tuileDisponible.getY());
            tuileDisponible.setSoldatPresent(nouveauSoldat);
            joueur.ajouterUnite(nouveauSoldat);
        } else {
            request.setAttribute("error", "Impossible d'ajouter un soldat, aucune tuile disponible !");
        }

        // Retourner à la page de jeu
        redirectToGameJsp(request, response);
    }

}