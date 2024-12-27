<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Scores Finaux</title>
    <link rel="stylesheet" type="text/css" href="../css/style.css">
</head>
<body>
    <h1>Scores de la Partie</h1>
    <table>
        <tr>
            <th>Joueur</th>
            <th>Score</th>
            <th>Territoires Contrôlés</th>
            <th>Combats Gagnés</th>
        </tr>
        <%-- Boucle pour afficher les scores depuis la base de données --%>
        <% 
            List<Joueur> joueurs = (List<Joueur>) request.getAttribute("joueurs");
            for (Joueur joueur : joueurs) {
        %>
            <tr>
                <td><%= joueur.getLogin() %></td>
                <td><%= joueur.getScore() %></td>
                <td><%= joueur.getTerritoires() %></td>
                <td><%= joueur.getCombatsGagnes() %></td>
            </tr>
        <% } %>
    </table>
</body>
</html>
