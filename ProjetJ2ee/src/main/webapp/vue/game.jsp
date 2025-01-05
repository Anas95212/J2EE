<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpServlet"%>
<%@ page import="jakarta.servlet.http.HttpServletRequest"%>
<%@ page import="jakarta.servlet.http.HttpServletResponse"%>
<%@ page import="model.Carte"%>
<%@ page import="model.Partie"%>
<%@ page import="model.Joueur"%>
<%@ page import="controller.PartieWebSocket"%>

<%
   // On récupère le pseudo logué
   String pseudo = (String) session.getAttribute("loggedUser");
   if (pseudo == null) {
       out.println("Vous n'êtes pas connecté (loggedUser manquant).");
       return;
   }

   // On récupère gameId
   String gameId = request.getParameter("gameId");
   if (gameId == null || gameId.trim().isEmpty()) {
       out.println("Aucun gameId fourni !");
       return;
   }

   // Recherche de la partie
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

   // Joueur actuel
   int idx = partie.getIndexJoueurActuel();
   Joueur current = partie.getJoueurs().get(idx);

   // Determine si c'est le tour de l'utilisateur
   boolean isMyTurn = current.getLogin().equals(pseudo);
%>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X - Carte du Jeu</title>
    <style>
        /* Styles identiques à votre code */
        .container { display: flex; justify-content: center; align-items: flex-start; gap: 20px; margin: 20px; }
        .actions { display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
        .game-grid { border-collapse: collapse; margin: 20px auto; background-color: #f0f0f0; }
        .game-grid td { width: 50px; height: 50px; border: 1px solid #ccc; text-align: center; }
        .game-grid td img { width: 100%; height: 100%; object-fit: cover; }
        body { font-family: 'Arial', sans-serif; background-color: #2d2d2d; color: #fff; margin: 0; padding: 0; }
        h1 { text-align: center; margin-top: 20px; font-size: 28px; color: #ffd700; }
        form button { background-color: #4CAF50; color: white; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
        form button:disabled { background-color: #888; cursor: not-allowed; }
    </style>
    <script>
        let gameSocket;

        // Fonction pour connecter le WebSocket
        function connectGameWebSocket() {
            const pseudo = "<%= pseudo %>";
            const wsUrl = "ws://" + location.host + "<%= request.getContextPath() %>/ws/parties?user=" + encodeURIComponent(pseudo);
            console.log("Game.jsp: Tentative de connexion WebSocket ->", wsUrl);

            // Création du WebSocket
            gameSocket = new WebSocket(wsUrl);

            // Événement : Connexion ouverte
            gameSocket.onopen = function () {
                console.log("Game.jsp: WebSocket connecté avec succès.");
            };

            // Événement : Connexion fermée
            gameSocket.onclose = function () {
                console.warn("Game.jsp: WebSocket déconnecté.");
            };

            // Événement : Erreur détectée
            gameSocket.onerror = function (event) {
                console.error("Game.jsp: Une erreur s'est produite avec le WebSocket :", event);
            };

            // Événement : Message reçu
            gameSocket.onmessage = function (event) {
                console.log("Game.jsp: Message reçu via WebSocket.");
                try {
                    const data = JSON.parse(event.data);
                    console.log("Game.jsp: Données reçues JSON ->", data);

                    // Vérifie le type de message reçu
                    if (data.type === "update") {
                        console.log("Game.jsp: Mise à jour de la carte détectée.");
                        updateGameUI(data);

                    } else if (data.type === "refresh") {
                        console.log("Game.jsp: Rechargement demandé via WebSocket.");
                        location.reload(); // Recharge la page entière
                    } else if (data.type === "combatStart") {
                        console.log("Début du combat détecté. Redirection...");
                        window.location.href = data.redirect; // Redirection vers combat.jsp
                    } else if (data.type === "error") {
                        console.error("Game.jsp: Message d'erreur reçu ->", data.message);
                        alert(`Erreur : ${data.message}`);
                    } else {
                        console.log("Game.jsp: Type de message WebSocket non reconnu ->", data.type);
                    }
                } catch (e) {
                    console.error("Game.jsp: Erreur lors du traitement des données WebSocket :", e);
                }
            };
        }

        // Fonction pour récupérer l'état via Fetch API
        function fetchGameState() {
            fetch(`/ProjetJ2ee/controller?action=updateState&gameId=<%= gameId %>`)
                .then(response => {
                    if (!response.ok) throw new Error("Erreur lors de la récupération de l'état du jeu.");
                    return response.json();
                })
                .then(data => {
                    updateGameUI(data);
                })
                .catch(error => console.error("Erreur Fetch:", error));
        }

        // Fonction commune pour mettre à jour l'UI
        function updateGameUI(data) {
    // Met à jour la carte
    const gameGrid = document.querySelector(".game-grid");
    if (gameGrid && data.htmlCarte) {
        // Remplace seulement si le contenu est différent
        if (gameGrid.innerHTML !== data.htmlCarte) {
            gameGrid.innerHTML = data.htmlCarte;
        }
    }

    // Met à jour le joueur actuel
    const currentPlayerElement = document.querySelector("#current-player");
    if (currentPlayerElement && data.currentPlayer) {
        currentPlayerElement.textContent = data.currentPlayer;

        // Désactiver les boutons si ce n'est pas le tour de l'utilisateur
        const isMyTurn = data.currentPlayer === "<%= pseudo %>";
        document.querySelectorAll("form button").forEach(button => {
            button.disabled = !isMyTurn;
        });
    }
}


        // Initialise la connexion WebSocket et démarre les mises à jour régulières
        function startGame() {
            connectGameWebSocket(); // Connexion WebSocket
            setInterval(fetchGameState, 1000); // Mise à jour via Fetch toutes les secondes
        }

        // Initialisation
        window.onload = startGame;
        
        
        

    </script>
</head>
<body>
    <h1>Carte du jeu</h1>
    <p>Tour en cours : <span id="current-player" style="color:<%= current.getCouleur() %>"><%= current.getLogin() %></span></p>

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
        <button type="submit" name="direction" value="east" <%= !isMyTurn ? "disabled" : "" %>>Move East</button>
        <button type="submit" name="direction" value="west" <%= !isMyTurn ? "disabled" : "" %>>Move West</button>
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

    <!-- Carte affichée dynamiquement -->
    <table class="game-grid">
    <% 
        String carteHTML = partie.getCarte().toHTML(gameId)
            .replace("src='/vue/images/", "src='" + request.getContextPath() + "/vue/images/")
            .replace("href='controller?action=selectSoldier", "href='" + request.getContextPath() + "/controller?action=selectSoldier")
            .replace("selectSoldier&soldierId=", "selectSoldier&gameId=" + gameId + "&soldierId=");
        out.print(carteHTML);
    %>
</table>

</body>
</html>