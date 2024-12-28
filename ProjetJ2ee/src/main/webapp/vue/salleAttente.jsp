<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Partie"%>
<%@ page import="model.Joueur"%>
<!DOCTYPE html>
<html>
<head>
    <title>Salle d'attente</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        .button {
            padding: 10px 20px;
            background-color: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
        }
        .button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <h1>Salle d'attente pour <%= request.getAttribute("nomPartie") %></h1>

    <h2>Joueurs connectés :</h2>
    <ul>
        <% 
            Partie partie = (Partie) request.getAttribute("partie");
            for (Joueur joueur : partie.getJoueurs()) { 
        %>
            <li><%= joueur.getLogin() %></li>
        <% } %>
    </ul>

    <% if (partie.getJoueurs().size() >= 2) { %>
        <form action="<%= request.getContextPath() %>/startGame" method="post">
            <input type="hidden" name="partieId" value="<%= partie.getId() %>">
            <button type="submit" class="button">Démarrer la partie</button>
        </form>
    <% } else { %>
        <p>En attente d'autres joueurs...</p>
    <% } %>
</body>
</html>
