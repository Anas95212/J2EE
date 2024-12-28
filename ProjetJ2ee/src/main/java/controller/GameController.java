package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carte;

@WebServlet("/game")
public class GameController extends HttpServlet {

    private Carte carte;

    @Override
    public void init() throws ServletException {
        // Initialise la carte lorsque la servlet démarre
        carte = new Carte(15, 15);
        carte.initialiserCarte();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Passe la carte à la vue
        request.setAttribute("carteHTML", carte.toHTML());

        // Redirection vers game.jsp en utilisant le chemin contextuel
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/vue/game.jsp");
    }
}
