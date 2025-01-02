<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Salle d'attente</title>
    <style>
        /* STYLE GLOBAL */
        body {
            font-family: 'Arial', sans-serif;
            background-color: #2d2d2d;
            color: #fff;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            height: 100vh;
        }

        /* TITRE PRINCIPAL */
        h1 {
            text-align: center;
            margin-bottom: 20px;
            font-size: 28px;
            color: #ffd700;
            text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
        }

        /* LISTE DES JOUEURS CONNECTÉS */
        #listeJoueurs {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            list-style: none;
            padding: 0;
            margin: 20px 0;
        }

        #listeJoueurs li {
            background-color: #444;
            color: #fff;
            padding: 10px 15px;
            border-radius: 5px;
            box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3);
            white-space: nowrap;
            text-align: center;
            max-width: 100px;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        /* BOUTONS DE COULEUR */
        .color-buttons {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin: 20px 0;
        }

        .color-buttons button {
            width: 55px;
            height: 55px;
            border: none;
            border-radius: 50%;
            cursor: pointer;
            transition: transform 0.2s ease;
        }

        .color-buttons button:hover {
            transform: scale(1.1);
        }

        /* BOUTON LANCER LA PARTIE */
        button.lancer {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 10px 20px;
            font-size: 16px;
            margin-top: 20px;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
        }

        button.lancer:hover {
            background-color: #45a049;
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
        }
    </style>
    <script>
        let socket;
        let currentGameId = null;

        function getParam(name) {
            const params = new URLSearchParams(window.location.search);
            return params.get(name);
        }

        function getGameIdFromURL() {
            return getParam("gameId");
        }

        function connectWebSocket(pseudo) {
            const wsUrl = "ws://localhost:8080/ProjetJ2ee/ws/parties?user=" + encodeURIComponent(pseudo);
            console.log("SalleAttente: connexion WebSocket ->", wsUrl);

            socket = new WebSocket(wsUrl);

            socket.onopen = function() {
                console.log("SalleAttente: WebSocket connecté. Pseudo =", pseudo);
            };

            socket.onclose = function() {
                console.log("SalleAttente: WebSocket déconnecté.");
            };

            socket.onerror = function(event) {
                console.error("SalleAttente: WebSocket Error:", event);
            };

            socket.onmessage = function(event) {
                let data;
                try {
                    data = JSON.parse(event.data);
                } catch(e) {
                    console.error("SalleAttente: Erreur parse JSON:", e);
                    return;
                }

                if (Array.isArray(data)) {
                    gererListeParties(data);
                    return;
                }

                if (data.error) {
                    alert("Erreur: " + data.error);
                    return;
                }

                if (data.redirect) {
                    console.log("SalleAttente: Redirection =>", data.redirect);
                    window.location.href = data.redirect;
                    return;
                }

                console.log("SalleAttente: Message non géré:", data);
            };
        }

        function gererListeParties(parties) {
            const partie = parties.find(p => p.gameId === currentGameId);
            if (!partie) {
                document.getElementById("info").innerHTML =
                  "<p class='error'>Erreur : aucune partie n'a l'ID " + currentGameId + "</p>";
                return;
            }

            afficherPartie(partie);

            if (partie.enCours === true) {
                console.log("Partie en cours ! Redirection vers game.jsp...");
                window.location.href = "game.jsp?gameId=" + partie.gameId;
            }
        }

        function afficherPartie(partie) {
            const infoDiv = document.getElementById("info");
            infoDiv.innerHTML = "<h1>Salle d'attente pour " + partie.nom + "</h1>";

            const ulJoueurs = document.getElementById("listeJoueurs");
            ulJoueurs.innerHTML = "";
            partie.joueurs.forEach(j => {
                const li = document.createElement("li");
                li.textContent = j.login;
                ulJoueurs.appendChild(li);
            });
        }

        function lancerPartie() {
            console.log("SalleAttente: Lancement de la partie ID =", currentGameId);
            socket.send(JSON.stringify({
                action: "lancerPartie",
                gameId: currentGameId
            }));
        }

        function choisirCouleur(couleur) {
            let gameId = "<%= request.getParameter("gameId") %>";
            socket.send(JSON.stringify({
                action: "choisirCouleur",
                gameId: gameId,
                couleur: couleur
            }));
        }

        window.onload = function() {
            currentGameId = getGameIdFromURL();
            const userParam = getParam("user") || "inconnu";
            connectWebSocket(userParam);
        };
    </script>
</head>
<body>
    <div id="info">
        <h1>Salle d'attente</h1>
        <p>Chargement en cours...</p>
    </div>

    <h2>Joueurs connectés :</h2>
    <ul id="listeJoueurs"></ul>

    <div class="color-buttons">
        <button style="background-color:#0250E9;" onclick="choisirCouleur('#0250E9')">Bleu</button>
        <button style="background-color:#FF381E;" onclick="choisirCouleur('#FF381E')">Rouge</button>
        <button style="background-color:#009D02;" onclick="choisirCouleur('#009D02')">Vert</button>
        <button style="background-color:#FF8500;" onclick="choisirCouleur('#FF8500')">Orange</button>
        <button style="background-color:#F1E919;" onclick="choisirCouleur('#F1E919')">Jaune</button>
        <button style="background-color:#800080;" onclick="choisirCouleur('#800080')">Violet</button>
    </div>

    <button class="lancer" onclick="lancerPartie()">Lancer la partie</button>
</body>
</html>
