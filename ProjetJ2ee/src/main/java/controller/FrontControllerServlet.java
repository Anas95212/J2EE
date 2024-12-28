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

        String action = request.getParameter("action");

        if (action == null || action.isEmpty()) {
            action = ""; // Par défaut, aucune action
        }

        // Redirection vers le contrôleur approprié en fonction de l'action
        switch (action) {
            case "login":
                new LoginController().handle(request, response);
                break;

            case "register":
                new LoginController().handle(request, response); // Gérer l'inscription
                break;

            case "move":
            case "attack":
                new ActionsController().handle(request, response);
                break;

            case "score":
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
