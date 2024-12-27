package controller;

import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ActionsController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("move".equals(action)) {
            // Récupère par ex. une direction: north, south, etc.
            String direction = request.getParameter("dir");
            
            // ICI on ferait la logique de déplacement du soldat
            // (ex: mise à jour d’un objet Soldier dans la session ou la BDD).
            // Pour tester, on met un message tout simple
            request.setAttribute("moveMessage", "Soldat déplacé vers " + direction);
            
            // Forward vers la game.jsp par exemple
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        } else if ("attack".equals(action)) {
            // Logique d’attaque
            request.setAttribute("attackMessage", "Attaque lancée !");
            
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        } else {
            // Action non reconnue, on renvoie vers la game.jsp par défaut
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        }
    }
}
