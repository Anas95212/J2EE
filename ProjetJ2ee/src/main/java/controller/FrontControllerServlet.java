package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/controller")
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

        String path = request.getServletPath();

        // Exclusion des JSP et des ressources statiques (CSS, JS, etc.)
        if (path.endsWith(".jsp") || path.startsWith("/css") || path.startsWith("/js")) {
            RequestDispatcher rd = request.getRequestDispatcher(path);
            rd.forward(request, response);
            return;
        }

        // Récupération de l'action demandée (ex: ?action=login)
        String action = request.getParameter("action");

        if (action == null || action.isEmpty()) {
            action = ""; // Par défaut, aucune action
        }

        // Redirection vers le contrôleur approprié en fonction de l'action
        switch (action) {
            case "login":
            case "register":
                // Déléguer la requête à LoginController
                new LoginController().handle(request, response);
                break;

            case "move":
            case "attack":
                // Déléguer la requête à ActionsController
                new ActionsController().handle(request, response);
                break;

            case "score":
                // Déléguer la requête à ScoreController
                new ScoreController().handle(request, response);
                break;

            default:
                // Action par défaut ou page d'accueil
                RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");
                rd.forward(request, response);
                break;
        }
    }
}
