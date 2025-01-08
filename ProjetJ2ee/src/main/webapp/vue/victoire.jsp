<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controller.ScoreController" %>
<%@ page import="controller.ScoreController.ScoreData" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>Victoire</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #2d2d2d;
            color: #fff;
            margin: 0;
            padding: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
        }
        h1 {
            color: #ffd700;
            font-size: 48px;
            margin-bottom: 20px;
        }
        p {
            font-size: 20px;
            margin-bottom: 40px;
        }
        a {
            text-decoration: none;
            color: #4CAF50;
            font-size: 18px;
            padding: 10px 20px;
            border: 1px solid #4CAF50;
            border-radius: 5px;
        }
        a:hover {
            background-color: #4CAF50;
            color: white;
        }
    </style>
</head>
<body>
    <h1>Victoire !</h1>
    <p>Félicitations, vous êtes le dernier joueur en vie et vous avez remporté la partie !</p>
    
    
         <%
        String pseudo = request.getParameter("pseudo");
        String score = request.getParameter("score");
        String gameId = request.getParameter("gameId");
        
        if (pseudo == null || score == null) {
            out.println("Erreur : paramètres manquants !");
            return;
        }
    %>
    
<h2>Bravo <%= pseudo %>, vous êtes le vainqueur !</h2>
    <p>Votre score final : <%= score %></p>
    
    
        // Afficher les infos vainqueur
    %>
    <h2>Bravo <%= pseudo %>, vous êtes le vainqueur !</h2>
    <p>Votre score final : <%= score %></p>

    <%
        // Appeler ScoreController pour récupérer tous les scores de cette partie
        List<ScoreData> listeScores = null;
        if (gameId != null && !gameId.isEmpty()) {
            ScoreController sc = new ScoreController();
            listeScores = sc.getScoresForGameId(gameId);
        }
    %>

    <!-- Afficher la liste des scores -->
    <h3>Scores de la partie (id_partie = <%= gameId %>):</h3>
    <%
        if (listeScores != null && !listeScores.isEmpty()) {
    %>
        <table>
            <tr>
                <th>Pseudo</th>
                <th>Score</th>
            </tr>
            <%
                for (ScoreData sd : listeScores) {
            %>
            <tr>
                <td><%= sd.getPseudo() %></td>
                <td><%= sd.getScore() %></td>
            </tr>
            <%
                }
            %>
        </table>
    <%
        } else {
            out.println("<p>Aucun score trouvé pour cette partie.</p>");
        }
    %>
    
    
    <a href="<%= request.getContextPath() %>/vue/lobby.jsp">Retour au Lobby</a>
</body>
</html>
