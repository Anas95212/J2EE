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

        switch (action) {
            case "login":
            case "register":
                // Gérer login / register via LoginController
                new LoginController().handle(request, response);
                break;

            case "selectSoldier":
            case "addLifeToSoldier":
            case "DoNothing":
            case "move":
            case "undo":
            case "endTurn":
            case "attack":
                // Redirection vers ActionsController pour toutes ces actions
                new ActionsController().handle(request, response);
                break;

            case "score":
                // ScoreController
                new ScoreController().handle(request, response);
                break;

            default:
                // Aucune action reconnue => renvoie vers home.jsp ou page d’accueil
                RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");
                rd.forward(request, response);
                break;
        }
    }
}
