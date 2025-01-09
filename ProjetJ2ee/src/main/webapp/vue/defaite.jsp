<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.Connection, java.sql.PreparedStatement, java.sql.SQLException, controller.DatabaseConnection" %>
<!DOCTYPE html>
<html>
<head>
    <title>Défaite</title>
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
            color: #ff4c4c;
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
    <h1>Défaite...</h1>
    
    
<%
    String pseudo = request.getParameter("pseudo");
    String score = request.getParameter("score");
    String gameId = request.getParameter("gameId"); // Ajoutez cette valeur dans vos paramètres

    if (pseudo == null || score == null || gameId == null) {
        out.println("Erreur : paramètres manquants !");
        return;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
        // Préparer l'insertion dans la base de données
        String insertQuery = "INSERT INTO zinee91_j2ee.parties (id_partie, pseudo_joueur , score_joueur) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, gameId);
            stmt.setString(2, pseudo);
            stmt.setInt(3, Integer.parseInt(score));

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                out.println("<p style='color: lightgreen;'>Les informations de défaite ont été enregistrées dans la base de données.</p>");
            } else {
                out.println("<p style='color: red;'>Une erreur est survenue lors de l'enregistrement.</p>");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        out.println("<p style='color: red;'>Erreur lors de la connexion à la base de données : " + e.getMessage() + "</p>");
    }
%>
    
   <h2>Dommage, <%= pseudo %>, vous avez perdu !</h2>
    <p>Votre score final : <%= score %></p>
    

    
    <a href="<%= request.getContextPath() %>/vue/lobby.jsp">Retour au Lobby</a>
    
    
</body>
</html>
