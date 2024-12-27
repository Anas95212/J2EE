<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X</title>
    <link rel="stylesheet" type="text/css" href="../css/style.css">
</head>
<body>
    <h1>Carte du Jeu</h1>
    <table>
        <%-- Génération de la grille 10x10 en Java pour refléter l'état du jeu --%>
        <%
            int gridSize = 10;
            for (int i = 0; i < gridSize; i++) {
        %>
            <tr>
                <% for (int j = 0; j < gridSize; j++) { %>
                    <td>
                        <%-- Afficher des éléments dynamiques (par ex: villes, soldats) --%>
                        <% if (/* Condition pour une ville */) { %>
                            <img src="../images/city.png" alt="Ville">
                        <% } else if (/* Condition pour une forêt */) { %>
                            <img src="../images/forest.png" alt="Forêt">
                        <% } else if (/* Condition pour une montagne */) { %>
                            <img src="../images/mountain.png" alt="Montagne">
                        <% } else { %>
                            <img src="../images/empty.png" alt="Vide">
                        <% } %>
                    </td>
                <% } %>
            </tr>
        <% } %>
    </table>
    <div>
        <button>Déplacer au Nord</button>
        <button>Déplacer au Sud</button>
        <button>Déplacer à l'Est</button>
        <button>Déplacer à l'Ouest</button>
        <button>Recruter un Soldat</button>
        <button>Fourrager des Ressources</button>
    </div>
</body>
</html>
