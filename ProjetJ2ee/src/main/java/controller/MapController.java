package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carte;

@WebServlet("/createMap")
public class MapController extends HttpServlet {

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Créer une carte 20x20
        Carte carte = new Carte(20, 20);
        carte.initialiserCarte(); // Initialiser la configuration

        // Stocker la carte en attribut de requête pour l'afficher
        request.setAttribute("carteHTML", carte.toHTML());

        // Rediriger vers une page JSP pour l'affichage
        request.getRequestDispatcher("/vue/game.jsp").forward(request, response);
    }
}
