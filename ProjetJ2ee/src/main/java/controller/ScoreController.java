package controller;
 
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
 
public class ScoreController {
 
    /**
     * 1) Méthode principale "handle" : elle lit gameId, va chercher
     *    la liste des scores, puis stocke la liste dans un attribut request.
     *    
     *    Ici, on ne fait PAS de forward. On laisse la JSP le soin de continuer.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Récupère le paramètre gameId
        String gameId = request.getParameter("gameId");
        // S’il est vide/inexistant, on crée juste une liste vide
        if (gameId == null || gameId.isEmpty()) {
            request.setAttribute("scores", new ArrayList<ScoreData>());
            return;
        }
        // Appel de la méthode interne qui exécute le SELECT
        List<ScoreData> scores = getScoresForGameId(gameId);
        // On place la liste en attribut de la requête
        request.setAttribute("scores", scores);
    }
 
    /**
     * 2) Méthode interne qui exécute la requête SQL :
     *    SELECT pseudo_joueur, score_joueur FROM parties WHERE id_partie = ? 
     *    et renvoie une liste de ScoreData, triée par score décroissant.
     */
    private List<ScoreData> getScoresForGameId(String gameId) {
        List<ScoreData> scores = new ArrayList<>();
 
        // Requête SQL (adaptée si ta colonne est VARCHAR ou INT)
        String sql = "SELECT pseudo_joueur, score_joueur "
                   + "FROM parties "
                   + "WHERE id_partie = ? "
                   + "ORDER BY score_joueur DESC";
 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, gameId);  // si ta colonne id_partie est VARCHAR
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
 
    /**
     * Petite classe interne pour stocker (pseudo, score).
     */
    public static class ScoreData {
        private final String pseudo;
        private final int score;
 
        public ScoreData(String pseudo, int score) {
            this.pseudo = pseudo;
            this.score = score;
        }
        public String getPseudo() { return pseudo; }
        public int getScore() { return score; }
    }
}