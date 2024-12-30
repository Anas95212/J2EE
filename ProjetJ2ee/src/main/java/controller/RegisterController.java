package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Vérifier si le pseudo existe déjà dans la base de données
                    String checkQuery = "SELECT COUNT(*) FROM utilisateurs WHERE pseudo = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setString(1, username);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                // Le pseudo existe déjà
                                request.setAttribute("errorMessage", "Ce pseudo est déjà utilisé. Veuillez en choisir un autre.");
                                request.getRequestDispatcher("/vue/register.jsp").forward(request, response);
                                return;
                            }
                        }
                    }

                    // Insérer le nouvel utilisateur si le pseudo n'existe pas
                    String insertQuery = "INSERT INTO utilisateurs (pseudo, mdp, score) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
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
