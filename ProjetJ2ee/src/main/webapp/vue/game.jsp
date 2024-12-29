<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Carte" %>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X - Carte du Jeu</title>
    <style>
        .game-grid {
            border-collapse: collapse;
        }
        .game-grid td {
            width: 40px;
            height: 40px;
            border: 1px solid black;
            text-align: center;
        }
        .game-grid .vide {
            background-color: #f4f4f4;
        }
        .game-grid .ville {
            background-color: #d4a017;
        }
        .game-grid .foret {
            background-color: #228b22;
        }
        .game-grid .montagne {
            background-color: #8b4513;
        }
    </style>
</head>
<body>
    <h1>Jeu 4X - Carte du Jeu</h1>

    <%
        // session est implicitement disponible dans une JSP
        // donc PAS de "HttpSession session = request.getSession();" ici

        Carte carte = (Carte) session.getAttribute("carte");

        if (carte == null) {
            // Si aucune carte n'existe, on la crée et on la stocke en session
            carte = new Carte(15, 15);
            carte.initialiserCarte();
            session.setAttribute("carte", carte);
        }

        // Générer le HTML de la carte
        String carteHTML = carte.toHTML();
    %>

    <div>
        <h3>Carte :</h3>
        <div><%= carteHTML %></div>
    </div>

    <div>
        <h3>Actions :</h3>
        <form action="game?action=move" method="post">
            <button type="submit" name="direction" value="north">Move North</button>
            <button type="submit" name="direction" value="south">Move South</button>
            <button type="submit" name="direction" value="east">Move East</button>
            <button type="submit" name="direction" value="west">Move West</button>
        </form>
    </div>
</body>
</html>
