package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginController {

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("errorMessage", "Veuillez remplir tous les champs !");
                request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Préparer la requête SQL pour vérifier les identifiants
                String query = "SELECT * FROM utilisateurs WHERE pseudo = ? AND mdp = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // Authentification réussie
                            HttpSession session = request.getSession();
                            session.setAttribute("loggedUser", username);

                            // Redirection vers le lobby
                            response.sendRedirect(request.getContextPath() + "/lobby");
                        } else {
                            // Identifiants incorrects
                            request.setAttribute("errorMessage", "Identifiants incorrects");
                            request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Erreur lors de la connexion à la base de données : " + e.getMessage());
                request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
            }

        } else if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("errorMessage", "Veuillez remplir tous les champs !");
                request.getRequestDispatcher("/vue/register.jsp").forward(request, response);
            } else {
                request.setAttribute("msg", "Inscription réussie ! Vous pouvez vous connecter.");
                request.getRequestDispatcher("/vue/login.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/vue/login.jsp");
        }
    }
}
