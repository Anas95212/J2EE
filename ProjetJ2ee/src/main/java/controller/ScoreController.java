package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ScoreController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Ex : récupérer le score depuis la session ou BDD
        // Pour tester, on fait un score bidon
        int fakeScore = 42; 
        request.setAttribute("playerScore", fakeScore);
        
        // Rediriger vers une page JSP de score, par exemple "score.jsp"
        RequestDispatcher rd = request.getRequestDispatcher("/score.jsp");
        rd.forward(request, response);
    }
}
