<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Carte" %>
<%@ page import="model.Tuile.TypeTuile" %>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X</title>
    <link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>
    <h1>Carte du jeu</h1>

    <%
        // Exemple de récupération d'une carte (à adapter avec votre logique réelle)
        Carte carte = (Carte) request.getServletContext().getAttribute("gameMap");
        if (carte == null) {
            carte = new Carte(10, 10); // Crée une carte par défaut
            carte.mettreAJourTuile(0, 0, TypeTuile.VILLE, false);
            carte.mettreAJourTuile(2, 2, TypeTuile.FORET, false);
            carte.mettreAJourTuile(4, 4, TypeTuile.MONTAGNE, true);
            request.getServletContext().setAttribute("gameMap", carte);
        }
    %>
    <div id="game-map">
        <%= carte.toHTML() %>
    </div>

    <h2>Actions :</h2>
    <form action="FrontControllerServlet" method="post">
        <input type="hidden" name="action" value="move" />
        <label for="direction">Direction :</label>
        <select name="dir" id="direction">
            <option value="north">Nord</option>
            <option value="south">Sud</option>
            <option value="east">Est</option>
            <option value="west">Ouest</option>
        </select>
        <button type="submit">Déplacer</button>
    </form>

    <form action="FrontControllerServlet" method="post">
        <input type="hidden" name="action" value="attack" />
        <button type="submit">Attaquer</button>
    </form>

    <c:if test="${not empty moveMessage}">
        <p>${moveMessage}</p>
    </c:if>
    <c:if test="${not empty attackMessage}">
        <p>${attackMessage}</p>
    </c:if>
</body>
</html>
