package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Registercontroller")
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username != null && password != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Préparer la requête SQL
                    String query = "INSERT INTO utilisateurs (pseudo, mdp, score) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        stmt.setInt(3, 0); // Score initial de l'utilisateur

                        int rowsInserted = stmt.executeUpdate();

                        if (rowsInserted > 0) {
                            request.setAttribute("msg", "Compte créé avec succès !");
                        } else {
                            request.setAttribute("errorMessage", "Une erreur est survenue lors de l'inscription.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Erreur lors de la connexion à la base de données : " + e.getMessage());
                }
            } else {
                request.setAttribute("errorMessage", "Veuillez remplir tous les champs.");
            }

            // Rediriger vers la page d'inscription
            request.getRequestDispatcher("/vue/register.jsp").forward(request, response);
        }
    }
}
