package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
public class TestConnection {
    public static void main(String[] args) {
        String query = "SELECT pseudo, score FROM utilisateurs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
 
            // Définir le paramètre de la requête (id = 1)
            stmt.setInt(1, 1);
 
            // Exécuter la requête
            ResultSet rs = stmt.executeQuery();
 
            // Afficher les résultats
            if (rs.next()) {
                String pseudo = rs.getString("pseudo");
                int score = rs.getInt("score");
                System.out.println("Pseudo : " + pseudo + ", Score : " + score);
            } else {
                System.out.println("Aucun joueur trouvé avec id=1");
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
 