<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Carte"%>
<%@ page import="model.Partie"%>
<%@ page import="model.Joueur"%>
<%@ page import="controller.PartieWebSocket"%>

<%
   // On récupère le pseudo logué (toujours en session HTTP, si tu veux le garder ainsi)
   String pseudo = (String) session.getAttribute("loggedUser");
   if (pseudo == null) {
       out.println("Vous n'êtes pas connecté (loggedUser manquant).");
       return;
   }

   // On récupère gameId depuis le paramètre ?gameId=...
   String gameId = request.getParameter("gameId");
   if (gameId == null || gameId.trim().isEmpty()) {
       out.println("Aucun gameId fourni !");
       return;
   }

   // On cherche la partie dans la liste statique
   Partie partie = null;
   for (Partie p : PartieWebSocket.getParties()) {
       if (p.getGameId().equals(gameId)) {
           partie = p;
           break;
       }
   }

   if (partie == null) {
       out.println("Aucune partie avec gameId=" + gameId + " n'a été trouvée dans PartieWebSocket !");
       return;
   }

   // Index du joueur actuel
   int idx = partie.getIndexJoueurActuel();
   Joueur current = partie.getJoueurs().get(idx);

   // Determine si c'est mon tour
   boolean isMyTurn = current.getLogin().equals(pseudo);
   System.out.println("DEBUG: pseudo (session) = [" + pseudo + "], current.getLogin() = [" + current.getLogin() + "]");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X - Carte du Jeu</title>
    <style>
        /* CONTAINER PRINCIPAL POUR LA GRILLE ET LES BOUTONS */
        .container {
            display: flex;
            justify-content: center;
            align-items: flex-start;
            gap: 20px;
            margin: 20px;
        }

        /* CONTENEUR DES BOUTONS */
        .actions {
            display: flex;
            flex-direction: column;
            gap: 10px;
            align-items: flex-start;
        }

        /* GRILLE */
        .game-grid {
            border-collapse: collapse;
            margin: 20px auto;
            background-color: #f0f0f0;
            box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.25);
        }

        .game-grid td {
            width: 50px;
            height: 50px;
            border: 1px solid #ccc;
            text-align: center;
            background-color: #e9e9e9;
            position: relative;
        }

        /* IMAGES DANS LES CELLULES */
        .game-grid td img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 5px;
            box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3);
        }

        /* TITRE ET DIV PRINCIPAL */
        body {
            font-family: 'Arial', sans-serif;
            background-color: #2d2d2d;
            color: #fff;
            margin: 0;
            padding: 0;
        }

        h1 {
            text-align: center;
            margin-top: 20px;
            font-size: 28px;
            color: #ffd700;
            text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
        }

        /* BOUTONS */
        form button {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 10px 20px;
            font-size: 16px;
            margin: 5px 0;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        form button:hover {
            background-color: #45a049;
            transform: translateY(-3px);
            box-shadow: 0px 5px 10px rgba(0, 0, 0, 0.3);
        }
    </style>
    <script>
        let gameSocket;

        function connectGameWebSocket() {
            const pseudo = "<%= pseudo %>";
            const wsUrl = "ws://" + location.host + "<%= request.getContextPath() %>/ws/parties?user=" + encodeURIComponent(pseudo);
            console.log("Game.jsp: connexion WebSocket ->", wsUrl);
            gameSocket = new WebSocket(wsUrl);

            gameSocket.onopen = function() {
                console.log("Game.jsp: WebSocket connecté.");
            };

            gameSocket.onclose = function() {
                console.log("Game.jsp: WebSocket déconnecté.");
            };

            gameSocket.onerror = function(event) {
                console.error("Game.jsp: WebSocket Error:", event);
            };

            gameSocket.onmessage = function(event) {
                try {
                    const data = JSON.parse(event.data);
                    if (data.reload === "true") {
                        console.log("Game.jsp: reload demandée -> location.reload()");
                        location.reload();
                    }
                } catch (e) {
                    console.error("Game.jsp: Erreur parse JSON:", e);
                }
            };
        }

        window.onload = connectGameWebSocket;
    </script>
</head>
<body>

    <h1>Carte du jeu</h1>
    <p>Tour en cours : <span style="color:<%= current.getCouleur() %>"><%= current.getLogin() %></span></p>

    <% if (!isMyTurn) { %>
       <p style="color:red;">Ce n'est pas votre tour, vous ne pouvez pas jouer.</p>
    <% } else { %>
       <p style="color:lightgreen;">C'est à vous de jouer !</p>
    <% } %>

    <!-- Boutons de déplacement -->
    <form action="<%= request.getContextPath() %>/controller" method="post">
        <input type="hidden" name="action" value="move"/>
        <input type="hidden" name="gameId" value="<%= gameId %>"/>
        <button type="submit" name="direction" value="north" <%= !isMyTurn ? "disabled" : "" %>>Move North</button>
        <button type="submit" name="direction" value="south" <%= !isMyTurn ? "disabled" : "" %>>Move South</button>
        <button type="submit" name="direction" value="east"  <%= !isMyTurn ? "disabled" : "" %>>Move East</button>
        <button type="submit" name="direction" value="west"  <%= !isMyTurn ? "disabled" : "" %>>Move West</button>
    </form>

    <!-- Bouton Undo -->
    <form action="<%= request.getContextPath() %>/controller" method="post">
        <input type="hidden" name="action" value="undo"/>
        <input type="hidden" name="gameId" value="<%= gameId %>"/>
        <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Annuler</button>
    </form>

    <!-- Bouton Fin de tour -->
    <form action="<%= request.getContextPath() %>/controller" method="post">
        <input type="hidden" name="action" value="endTurn"/>
        <input type="hidden" name="gameId" value="<%= gameId %>"/>
        <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Fin de tour</button>
    </form>

    <hr/>

    <%
        // On affiche la carte
        String carteHTML = partie.getCarte().toHTML(gameId);

        // Correction du chemin 'images/' => '/ProjetJ2ee/images/' par exemple :
        carteHTML = carteHTML.replace("src='images/", "src='" + request.getContextPath() + "/vue/images/");
        carteHTML = carteHTML.replace(
            "href='controller?action=selectSoldier",
            "href='" + request.getContextPath() + "/controller?action=selectSoldier"
        );
        carteHTML = carteHTML.replace(
            "selectSoldier&soldierId=",
            "selectSoldier&gameId=" + gameId + "&soldierId="
        );
        out.print(carteHTML);
    %>

</body>
</html>
