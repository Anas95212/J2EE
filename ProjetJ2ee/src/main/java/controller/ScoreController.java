package controller;
 
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
 
public class ScoreController {
 
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        // ----------------------------
        // AJOUT pour lecture BDD
        // ----------------------------
 
        String winner = request.getParameter("winner");
        if (winner == null) winner = "Aucun gagnant détecté";
        request.setAttribute("winner", winner);
 
        // Récupération optionnelle du gameId
        String gameId = request.getParameter("gameId");
        if (gameId == null) {
            gameId = "Partie inconnue";
        }
        request.setAttribute("gameId", gameId);
 
        // On récupère en base TOUS les scores (table "parties")
        // puis on va les ranger dans une structure
        List<ScoreData> scores = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
           
            // Si pas de colonne "game_id", on ne filtre pas
            // On classe simplement par ordre décroissant du score
            String sql = "SELECT pseudo_joueur, score_joueur FROM parties ORDER BY score_joueur DESC";
            try (ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String pseudo = rs.getString("pseudo_joueur");
                    int score   = rs.getInt("score_joueur");
                    scores.add(new ScoreData(pseudo, score));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des scores : " + e.getMessage());
            e.printStackTrace();
        }
 
        // On place la liste des scores dans l'attribut
        request.setAttribute("scores", scores);
 
        // -- Fin de l’AJOUT
 
        // Rediriger vers la page JSP de score
        RequestDispatcher rd = request.getRequestDispatcher("/score.jsp");
        rd.forward(request, response);
    }
 
    // Petite classe interne pour stocker (pseudo, score)
    public static class ScoreData {
        private String pseudo;
        private int score;
 
        public ScoreData(String pseudo, int score) {
            this.pseudo = pseudo;
            this.score = score;
        }
        public String getPseudo() {
            return pseudo;
        }
        public int getScore() {
            return score;
        }
    }
}