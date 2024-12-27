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
            // R�cup�re par ex. une direction: north, south, etc.
            String direction = request.getParameter("dir");
            
            // ICI on ferait la logique de d�placement du soldat
            // (ex: mise � jour d�un objet Soldier dans la session ou la BDD).
            // Pour tester, on met un message tout simple
            request.setAttribute("moveMessage", "Soldat d�plac� vers " + direction);
            
            // Forward vers la game.jsp par exemple
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        } else if ("attack".equals(action)) {
            // Logique d�attaque
            request.setAttribute("attackMessage", "Attaque lanc�e !");
            
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        } else {
            // Action non reconnue, on renvoie vers la game.jsp par d�faut
            RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
            rd.forward(request, response);
        }
    }
}
