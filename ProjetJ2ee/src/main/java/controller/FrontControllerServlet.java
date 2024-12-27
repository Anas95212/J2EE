package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/*") // Intercepte toutes les requêtes qui arrivent sur l'application
public class FrontControllerServlet extends HttpServlet {

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

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Récupérer l'action demandée (ex: ?action=login ou ?action=moveSoldier)
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "";
        }
        
        // 2) Rediriger vers le contrôleur approprié
        switch (action) {
            case "login":
            case "register":
                // On délègue la requête à LoginController
                new LoginController().handle(request, response);
                break;
                
            case "move":
            case "attack":
                // On délègue la requête à ActionsController
                new ActionsController().handle(request, response);
                break;
                
            case "score":
                // On délègue la requête à ScoreController
                new ScoreController().handle(request, response);
                break;
                
            default:
                // Action par défaut ou page d'accueil
                // Par exemple, on forward vers une JSP "home.jsp"
                RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");
                rd.forward(request, response);
                break;
        }
    }
}
