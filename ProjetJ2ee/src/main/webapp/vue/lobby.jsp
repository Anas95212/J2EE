<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Lobby des Parties</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        table, th, td {
            border: 1px solid black;
        }
        th, td {
            padding: 10px;
            text-align: left;
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
        .disabled {
            background-color: #cccccc;
            cursor: not-allowed;
        }
        .container {
            margin: 20px;
        }
        h2 {
            margin-top: 30px;
        }
    </style>
    <script>
        let socket;

        function connectWebSocket() {
            socket = new WebSocket("ws://localhost:8080/ProjetJ2ee/ws/parties");

            socket.onopen = function () {
                console.log("WebSocket connecté.");
            };
            
            socket.onerror = function(event) {
                console.error("WebSocket Error: ", event);
            };

            socket.onmessage = function (event) {
                const parties = JSON.parse(event.data);
                const listeParties = document.getElementById("liste-parties");
                listeParties.innerHTML = "";

                if (parties.length === 0) {
                    listeParties.innerHTML = "<p>Aucune partie disponible.</p>";
                    return;
                }

                parties.forEach(partie => {
                    const item = document.createElement("div");
                    item.innerHTML = `
                        <b>ID: ${partie.gameId}</b> - <b>${partie.nom}</b> - ${partie.joueurs.length}/${partie.maxJoueurs} joueurs 
                        <button class="button" onclick="rejoindrePartie('${partie.gameId}')" ${partie.joueurs.length >= partie.maxJoueurs ? "disabled" : ""}>
                            Rejoindre
                        </button>
                    `;
                    listeParties.appendChild(item);
                });
            };



            socket.onclose = function () {
                console.log("WebSocket déconnecté.");
            };

            socket.onerror = function (error) {
                console.error("Erreur WebSocket : ", error);
            };
        }


        function creerPartie() {
            const nom = document.getElementById("nom-partie").value;
            const maxJoueurs = document.getElementById("max-joueurs").value;
            if (nom && maxJoueurs) {
                socket.send(JSON.stringify({ action: "creerPartie", nom, maxJoueurs }));
            }
        }

        function rejoindrePartie(gameId) {
            console.log("Tentative de rejoindre la partie avec gameId :", gameId); // Log
            socket.send(JSON.stringify({ action: "rejoindrePartie", gameId }));
        }



        window.onload = connectWebSocket;
    </script>
</head>
<body>
    <div class="container">
        <h1>Lobby des Parties</h1>

        <h2>Parties disponibles :</h2>
        <div id="liste-parties">
            <p>Chargement des parties...</p>
        </div>

        <h2>Créer une nouvelle partie :</h2>
        <form onsubmit="event.preventDefault(); creerPartie();">
            <label for="nom-partie">Nom de la Partie :</label>
            <input type="text" id="nom-partie" required>
            <br>
            <label for="max-joueurs">Nombre de joueurs maximum (1-6) :</label>
            <input type="number" id="max-joueurs" min="1" max="6" required>
            <br><br>
            <button type="submit" class="button">Créer la Partie</button>
        </form>
    </div>
</body>
</html>
