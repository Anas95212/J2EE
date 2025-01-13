<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpServlet"%>
<%@ page import="jakarta.servlet.http.HttpServletRequest"%>
<%@ page import="jakarta.servlet.http.HttpServletResponse"%>
<%@ page import="model.Carte"%>
<%@ page import="model.Partie"%>
<%@ page import="model.Joueur"%>
<%@ page import="controller.PartieWebSocket"%>

<%
   String pseudo = (String) session.getAttribute("loggedUser");
   if (pseudo == null) {
       out.println("Vous n'êtes pas connecté (loggedUser manquant).");
       return;
   }

   String gameId = request.getParameter("gameId");
   if (gameId == null || gameId.trim().isEmpty()) {
       out.println("Aucun gameId fourni !");
       return;
   }

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

   int idx = partie.getIndexJoueurActuel();
   Joueur current = partie.getJoueurs().get(idx);

   boolean isMyTurn = current.getLogin().equals(pseudo);
%>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X - Carte du Jeu</title>
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
		    overflow: hidden; /* Évite les barres de défilement */
		}
        h1 {
            text-align: center;
            margin-top: 20px;
            font-size: 28px;
            color: #ffd700;
        }
        .container {
		    display: flex;
		    justify-content: space-between;
		    align-items: flex-start;
		    gap: 20px;
		    width: 100%;
		    max-width: 1200px; /* Ajuste la largeur maximale */
		}
        .game-grid {
		    flex: 0 1 auto;
		    background-color: #f0f0f0;
		    border-collapse: collapse;
		}
        .game-grid td {
    position: relative;
    width: 50px;
    height: 50px;
    border: 1px solid #ccc;
}

.game-grid .background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1; /* Arrière-plan */
}

.game-grid .foreground {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2; /* Soldat au-dessus */
}

.game-grid img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
        .actions {
		    flex: 0 1 auto;
		    text-align: center;
		    display: flex;
		    flex-direction: column;
		    gap: 15px;
		    align-items: center;
		}
        .actions button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
        }
        .actions button:disabled {
            background-color: #888;
            cursor: not-allowed;
        }
        hr {
            width: 100%; /* Le trait prend toute la largeur */
            margin-top: 20px;
            border: 1px solid #ccc;
        }
        .shop {
		    margin-top: 20px;
		    text-align: center;
		    background-color: #333;
		    padding: 10px;
		    border-radius: 8px;
		    color: white;
		    box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3);
		    width: 200px; /* Ajuste la largeur du shop */
		}
		
		.shop h2 {
		    margin-bottom: 10px;
		    color: #ffd700;
		}
		
		.shop button {
		    background-color: #4CAF50;
		    color: white;
		    padding: 10px 20px;
		    border-radius: 5px;
		    cursor: pointer;
		    margin-top: 10px;
		}
		
		.shop button:disabled {
		    background-color: #888;
		    cursor: not-allowed;
		}
		
    </style>
    <script>
        let gameSocket;

        function connectGameWebSocket() {
            const pseudo = "<%= pseudo %>";
            const wsUrl = "ws://" + location.host + "<%= request.getContextPath() %>/ws/parties?user=" + encodeURIComponent(pseudo);
            console.log("Game.jsp: Tentative de connexion WebSocket ->", wsUrl);

            gameSocket = new WebSocket(wsUrl);
            gameSocket.onopen = function () { console.log("Game.jsp: WebSocket connecté avec succès."); };
            gameSocket.onclose = function () { console.warn("Game.jsp: WebSocket déconnecté."); };
            gameSocket.onerror = function (event) { console.error("Game.jsp: Une erreur WebSocket :", event); };
            gameSocket.onmessage = function (event) {
                try {
                    const data = JSON.parse(event.data);

                    if (data.type === "update") {
                        updateGameUI(data);
                    } else if (data.type === "refresh") {
                        location.reload();
                    } else if (data.type === "combatStart") {
                        window.location.href = data.redirect; // Redirection vers combat.jsp
                    } else if (data.type === "combatEnd") {
                        console.log("Fin du combat détectée. Mise à jour de la carte.");
                        location.reload(); // Recharge la page principale pour refléter les changements
                    } else if (data.type === "error") {
                        alert(`Erreur : ${data.message}`);
                    }
                    else if (data.type === "defeat") {
                        // Gestion de la défaite d'un joueur
                        if (data.pseudo === "<%= pseudo %>") {
                        	console.log("==> Défaite reçue, data.pseudo=", data.pseudo, " / local pseudo=", "<%= pseudo %>");
                            alert("Vous avez été éliminé !");
                            window.location.href = "<%= request.getContextPath() %>/vue/defaite.jsp"
                                + "?pseudo=" + encodeURIComponent(pseudo)
                                + "&score=" + encodeURIComponent(data.score)
                                + "&gameId=" + encodeURIComponent(gameId);
                        } else {
                            console.log(`${data.pseudo} a été éliminé.`);
                        }
                    } else if (data.type === "gameEnd") {
                        // Fin de la partie, redirection vers l'accueil
                        alert("La partie est terminée !");
                        window.location.href = "<%= request.getContextPath() %>/vue/lobby.jsp";
                    }
                    else if (data.type === "combatVilleStart") {
                        window.location.href = data.redirect;
                    }

                    else if (data.type === "victory") {
                        if (data.pseudo === "<%= pseudo %>") {
                            alert("Félicitations, vous avez gagné !");
                            window.location.href = "<%= request.getContextPath() %>/vue/victoire.jsp";
                        } else {
                            console.log(`${data.pseudo} a gagné la partie.`);
                        }
                    }
                    else if (data.type === "lobby") {
                    	console.log(`retour lobby detecté`);
                        window.location.href = "<%= request.getContextPath() %>/vue/lobby.jsp";
                      	console.log(`retour lobby effectué`);
                     }

                } catch (e) {
                    console.error("Erreur WebSocket :", e);
                }
            };

        }

        function fetchGameState() {
            fetch(`/ProjetJ2ee/controller?action=updateState&gameId=<%= gameId %>`)
                .then(response => {
                    if (!response.ok) throw new Error("Erreur lors de la récupération de l'état du jeu.");
                    return response.json();
                })
                .then(data => { updateGameUI(data); })
                .catch(error => console.error("Erreur Fetch:", error));
        }

        function updateGameUI(data) {
            const gameGrid = document.querySelector(".game-grid");
            if (gameGrid && data.htmlCarte && gameGrid.innerHTML !== data.htmlCarte) {
                gameGrid.innerHTML = data.htmlCarte;
            }
            const currentPlayerElement = document.querySelector("#current-player");
            if (currentPlayerElement && data.currentPlayer) {
                currentPlayerElement.textContent = data.currentPlayer;
                const isMyTurn = data.currentPlayer === "<%= pseudo %>";
                document.querySelectorAll(".actions button").forEach(button => {
                    button.disabled = !isMyTurn;
                });
            }
        }

        function startGame() {
            connectGameWebSocket();
            setInterval(fetchGameState, 1000);
        }

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
    
    <% 
	    // Vérifie si un joueur a gagné des points
	    String dernierMessage = (String) session.getAttribute("dernierMessage");
	    if (dernierMessage != null && !dernierMessage.isEmpty()) {
	%>
	    <p style="color:yellow; font-weight:bold;"><%= dernierMessage %></p>
	<%
	        // Réinitialiser le message après l'affichage pour éviter de le montrer plusieurs fois
	        session.removeAttribute("dernierMessage");
	    }
	%>
    <%-- Affichage du message de vie --%>
        <c:if test="${not empty lifeMessage}">
            <div class="alert alert-success">
                ${lifeMessage}
            </div>
        </c:if>

        <%-- Affichage du message d'erreur --%>
        <c:if test="${not empty errorlife}">
            <div class="alert alert-danger">
                ${errorlife}
            </div>
        </c:if>
    <hr/>

    <div class="container">
        <table class="game-grid">
            <% 
                String carteHTML = partie.getCarte().toHTML(gameId)
                    .replace("src='/vue/images/", "src='" + request.getContextPath() + "/vue/images/")
                    .replace("href='controller?action=selectSoldier", "href='" + request.getContextPath() + "/controller?action=selectSoldier")
                    .replace("selectSoldier&soldierId=", "selectSoldier&gameId=" + gameId + "&soldierId=");
                out.print(carteHTML);
            %>
        </table>

        <div class="actions">
            <form action="<%= request.getContextPath() %>/controller" method="post">
                <input type="hidden" name="action" value="move"/>
                <input type="hidden" name="gameId" value="<%= gameId %>"/>
                <button type="submit" name="direction" value="north" <%= !isMyTurn ? "disabled" : "" %>>Move North</button>
                <button type="submit" name="direction" value="south" <%= !isMyTurn ? "disabled" : "" %>>Move South</button>
                <button type="submit" name="direction" value="east" <%= !isMyTurn ? "disabled" : "" %>>Move East</button>
                <button type="submit" name="direction" value="west" <%= !isMyTurn ? "disabled" : "" %>>Move West</button>
            </form>
            <form action="<%= request.getContextPath() %>/controller" method="post">
                <input type="hidden" name="action" value="addLifeToSoldier"/>
                <input type="hidden" name="gameId" value="<%= gameId %>"/>
                <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Ajouter de la Vie</button>
            </form>
            <form action="<%= request.getContextPath() %>/controller" method="post">
                <input type="hidden" name="action" value="undo"/>
                <input type="hidden" name="gameId" value="<%= gameId %>"/>
                <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Annuler</button>
            </form>
            <form action="<%= request.getContextPath() %>/controller" method="post">
                <input type="hidden" name="action" value="DoNothing"/>
                <input type="hidden" name="gameId" value="<%= gameId %>"/>
                <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Ne rien faire</button>
            </form>
            <form action="<%= request.getContextPath() %>/controller" method="post">
                <input type="hidden" name="action" value="endTurn"/>
                <input type="hidden" name="gameId" value="<%= gameId %>"/>
                <button type="submit" <%= !isMyTurn ? "disabled" : "" %>>Fin de tour</button>
            </form>
            
            
            <%
    Joueur moi = null;
    for (Joueur j : partie.getJoueurs()) {
        if (j.getLogin().equals(pseudo)) {
            moi = j;
            break;
        }
    }
%>
            
            
            <!-- SHOP -->
		    <div class="shop">
		        <h2>Shop</h2>
		        <p>Votre monnaie : <span id="player-coins"><%= moi.getPointsDeProduction() %></span> pièces</p>
		        <form action="<%= request.getContextPath() %>/controller" method="post">
		            <input type="hidden" name="action" value="buySoldier"/>
		            <input type="hidden" name="gameId" value="<%= gameId %>"/>
		            <button type="submit" <%= moi.getPointsDeProduction() < 15 ? "disabled" : "" %>>
		                Acheter un soldat (15 pièces)
		            </button>
		        </form>
		    </div>
        </div>
    </div>
</body>
</html>
