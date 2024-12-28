package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Partie;
import model.Joueur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/lobby")
public class LobbyController extends HttpServlet {

    private List<Partie> parties;

    @Override
    public void init() throws ServletException {
        // Initialisation des parties
        parties = new ArrayList<>();
        getServletContext().setAttribute("parties", parties);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Récupérer les parties non commencées
        List<Partie> partiesDisponibles = new ArrayList<>();
        for (Partie partie : parties) {
            if (!partie.isEnCours()) {
                partiesDisponibles.add(partie);
            }
        }

        request.setAttribute("partiesDisponibles", partiesDisponibles);

        // Rediriger vers le lobby
        request.getRequestDispatcher("/vue/lobby.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            String nom = request.getParameter("nom");
            int maxJoueurs = Integer.parseInt(request.getParameter("maxJoueurs"));

            Partie partie = new Partie(nom, maxJoueurs);
            parties.add(partie);
        }

        response.sendRedirect(request.getContextPath() + "/lobby");
    }
}
