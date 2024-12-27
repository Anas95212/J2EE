package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        
        if ("login".equals(action)) {
            // Récupérer paramètres du formulaire (username, password)
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            // Pour tester sans BDD : on fait un test bidon
            if ("admin".equals(username) && "123".equals(password)) {
                // On stocke l’info en session
                HttpSession session = request.getSession();
                session.setAttribute("loggedUser", username);
                
                // Puis on redirige vers la page "game.jsp" (par exemple)
                RequestDispatcher rd = request.getRequestDispatcher("/game.jsp");
                rd.forward(request, response);
            } else {
                // Erreur de login
                request.setAttribute("errorMessage", "Identifiants incorrects");
                RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
                rd.forward(request, response);
            }
        } else if ("register".equals(action)) {
            // Traitement d’une création de compte
            // On pourrait enregistrer l’utilisateur en BDD, etc.
            // Ici on fait un test bidon
            request.setAttribute("msg", "Inscription effectuée (fictive)!");
            RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
            rd.forward(request, response);
        }
    }
}
