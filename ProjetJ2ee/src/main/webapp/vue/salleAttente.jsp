<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Salle d'attente</title>
    <style>
        /* STYLE GLOBAL */
	body {
	    font-family: 'Arial', sans-serif; /* Police moderne */
	    background-color: #2d2d2d; /* Fond sombre */
	    color: #fff; /* Texte en blanc pour contraste */
	    margin: 0;
	    padding: 0;
	    display: flex;
	    justify-content: center;
	    align-items: center;
	    flex-direction: column;
	    height: 100vh; /* Prend toute la hauteur de la fenêtre */
	}
	
	/* TITRE PRINCIPAL */
	h1 {
	    text-align: center;
	    margin-bottom: 20px;
	    font-size: 28px;
	    color: #ffd700; /* Doré pour le titre */
	    text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
	}
	
	/* INFORMATIONS ET ERREURS */
	#error-message {
	    color: red;
	    font-size: 14px;
	    margin: 10px 0;
	}
	
	#info {
	    text-align: center;
	    margin: 20px 0;
	    background-color: #3b3b3b; /* Fond du bloc d'information */
	    padding: 20px;
	    border-radius: 10px;
	    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre légère */
	}
	
	/* LISTE DES JOUEURS */
	ul {
	    list-style: none; /* Retire les puces */
	    padding: 0;
	    margin: 0;
	}
	
	ul li {
	    background-color: #444; /* Fond légèrement plus clair */
	    color: #fff; /* Texte blanc */
	    padding: 10px;
	    margin-bottom: 10px;
	    border-radius: 5px; /* Coins arrondis */
	    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3); /* Ombre légère */
	}
	
	/* BOUTON */
	button {
	    background-color: #4CAF50; /* Vert agréable */
	    color: white;
	    border: none;
	    padding: 10px 20px;
	    font-size: 16px;
	    margin-top: 20px;
	    border-radius: 5px;
	    cursor: pointer;
	    transition: all 0.3s ease;
	    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre légère */
	}
	
	button:hover {
	    background-color: #45a049; /* Vert légèrement plus foncé au survol */
	    transform: translateY(-3px); /* Effet de levée */
	    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); /* Ombre renforcée au survol */
	}
	/* LISTE DES JOUEURS CONNECTÉS */
	#listeJoueurs {
	    display: flex; /* Utilise le mode flex */
	    flex-wrap: wrap; /* Permet de passer à la ligne si trop d'éléments */
	    gap: 10px; /* Espacement entre les joueurs */
	    list-style: none; /* Supprime les puces */
	    padding: 0;
	    margin: 20px 0;
	}
	
	#listeJoueurs li {
	    background-color: #444; /* Fond légèrement contrasté */
	    color: #fff; /* Texte blanc */
	    padding: 10px 15px;
	    border-radius: 5px; /* Coins arrondis */
	    box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3); /* Ombre légère */
	    white-space: nowrap; /* Empêche les noms trop longs de passer à la ligne */
	    text-align: center; /* Centre le texte */
	}
	
    </style>
    <script>
        let socket;
        let currentGameId = null;  

        /**
         * Récupère la valeur d'un paramètre dans l'URL (ex: ?gameId=G1&user=Bob).
         */
        function getParam(name) {
            const params = new URLSearchParams(window.location.search);
            return params.get(name);
        }

        /**
         * Récupère le gameId depuis l'URL (ex: ?gameId=GAME-12345).
         */
        function getGameIdFromURL() {
            return getParam("gameId");
        }

        /**
         * Se connecte au WebSocket en passant ?user=lePseudo dans l'URL.
         */
        function connectWebSocket(pseudo) {
            // On construit l'URL WebSocket en ajoutant ?user=...
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

                // 1) Si data est un tableau => c'est la liste des parties
                if (Array.isArray(data)) {
                    gererListeParties(data);
                    return;
                }

                // 2) Si data.error => on affiche
                if (data.error) {
                    alert("Erreur: " + data.error);
                    return;
                }

                // 3) Si data.redirect => redirection
                if (data.redirect) {
                    console.log("SalleAttente: Redirection =>", data.redirect);
                    window.location.href = data.redirect;
                    return;
                }

                // Sinon, message non géré
                console.log("SalleAttente: Message non géré:", data);
            };
        }

        /**
         * Cherche la partie qui a l'ID = currentGameId,
         * puis met à jour l'affichage. 
         * Si on voit enCours = true => on redirige vers game.jsp?gameId=...
         */
        function gererListeParties(parties) {
            const partie = parties.find(p => p.gameId === currentGameId);
            if (!partie) {
                document.getElementById("info").innerHTML =
                  "<p class='error'>Erreur : aucune partie n'a l'ID " + currentGameId + "</p>";
                return;
            }

            afficherPartie(partie);

            // Si la partie est enCours => on redirige vers game.jsp
            if (partie.enCours === true) {
                console.log("Partie en cours ! Redirection vers game.jsp...");
                window.location.href = "game.jsp?gameId=" + partie.gameId;
            }
        }

        /**
         * Affiche le nom de la partie, la liste des joueurs, etc.
         */
        function afficherPartie(partie) {
            const infoDiv = document.getElementById("info");
            infoDiv.innerHTML = "<h1>Salle d'attente pour " + partie.nom + "</h1>";

            // On affiche les joueurs
            const ulJoueurs = document.getElementById("listeJoueurs");
            ulJoueurs.innerHTML = "";
            partie.joueurs.forEach(j => {
                const li = document.createElement("li");
                li.textContent = j;
                ulJoueurs.appendChild(li);
            });
        }

        /**
         * Envoie {"action":"lancerPartie","gameId":currentGameId} au serveur,
         * ce qui va déclencher partie.setEnCours(true) et un broadcast (envoyerListeParties).
         */
        function lancerPartie() {
            console.log("SalleAttente: Lancement de la partie ID =", currentGameId);
            socket.send(JSON.stringify({
                action: "lancerPartie",
                gameId: currentGameId
            }));
        }

        // ---- Au chargement de la page ----
        window.onload = function() {
            currentGameId = getGameIdFromURL();
            
            // On récupère le param ?user=... dans l'URL
            const userParam = getParam("user") || "inconnu";

            // On se connecte au WebSocket en passant ce pseudo
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

    <button class="button" onclick="lancerPartie()">Lancer la partie</button>
</body>
</html>