package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// (Imports à ajuster selon votre projet)
public class ScoreController {

    /**
     * Récupère la liste de (pseudo, score) pour le gameId donné
     * depuis la table 'parties', filtrée par 'id_partie = ?',
     * triée par score décroissant.
     */
    public List<ScoreData> getScoresForGameId(String gameId) {
        List<ScoreData> scores = new ArrayList<>();

        // Requête SQL : SELECT pseudo_joueur, score_joueur FROM parties WHERE id_partie = ? ...
        String sql = "SELECT pseudo_joueur, score_joueur "
                   + "FROM parties "
                   + "WHERE id_partie = ? "
                   + "ORDER BY score_joueur DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gameId);  // Si la colonne id_partie est de type VARCHAR
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String pseudo = rs.getString("pseudo_joueur");
                    int score = rs.getInt("score_joueur");
                    scores.add(new ScoreData(pseudo, score));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scores;
    }

    // Petite classe interne pour stocker (pseudo, score)
    public static class ScoreData {
        private String pseudo;
        private int score;

        public ScoreData(String pseudo, int score) {
            this.pseudo = pseudo;
            this.score = score;
        }
        public String getPseudo() { return pseudo; }
        public int getScore() { return score; }
    }
}
