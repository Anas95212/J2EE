package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://mysql-zinee91.alwaysdata.net:3306/zinee91_j2ee"; // Ajout du schéma à l'URL
    private static final String USER = "zinee91_game";
    private static final String PASSWORD = "J2EE2025";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Établir la connexion
            connection = getConnection();
            System.out.println("Connexion réussie à la base de données !");
            
            // Créer un statement pour exécuter une requête
            statement = connection.createStatement();
            
            // Exécuter une requête pour récupérer les données de la table "utilisateurs"
            String query = "SELECT * FROM utilisateurs";
            resultSet = statement.executeQuery(query);
            
            // Afficher les données
            System.out.println("Contenu de la table 'utilisateurs' :");
            while (resultSet.next()) {
                int id = resultSet.getInt("id"); // Remplacez "id" par les colonnes de votre table
                String nom = resultSet.getString("pseudo"); // Exemple de colonne
                String email = resultSet.getString("mdp"); // Exemple de colonne
                System.out.println("ID: " + id + ", pseudo: " + nom + ", mdp: " + email);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion ou d'exécution SQL : " + e.getMessage());
        } finally {
            // Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }
}
