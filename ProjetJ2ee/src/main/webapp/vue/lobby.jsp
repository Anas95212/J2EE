<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Lobby des Parties</title>
    <style>
        /* STYLE GLOBAL */
        body {
            font-family: 'Arial', sans-serif;
            background-color: #2d2d2d; /* Fond sombre */
            color: #fff; /* Texte blanc */
            margin: 0;
            padding: 0;
        }
 
        /* CONTAINER PRINCIPAL */
        .container {
            width: 80%;
            max-width: 800px;
            margin: 20px auto; /* Centre le contenu */
            text-align: center;
        }
 
        /* TITRE PRINCIPAL */
        h1 {
            font-size: 28px;
            color: #ffd700; /* Couleur dorée */
            text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
            margin-bottom: 20px;
        }
 
        /* SOUS-TITRES */
        h2 {
            font-size: 20px;
            color: #fff;
            margin-top: 30px;
            margin-bottom: 15px;
            text-align: center;
        }
 
        /* TABLE DES PARTIES */
        #liste-parties {
            margin-top: 20px;
            background-color: #3b3b3b; /* Fond sombre */
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre */
        }
 
        #liste-parties div {
            padding: 10px;
            margin: 10px 0;
            background-color: #444; /* Fond légèrement plus clair pour chaque partie */
            border-radius: 5px;
            text-align: left; /* Alignement à gauche */
            color: #fff;
        }
 
        /* FORMULAIRE DE CRÉATION DE PARTIE */
        form {
            margin-top: 20px;
            background-color: #3b3b3b; /* Fond du formulaire */
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre */
            text-align: left; /* Alignement à gauche */
        }
 
        form label {
            display: block; /* Chaque label sur une ligne */
            margin-bottom: 5px;
            font-size: 14px;
            color: #bbb; /* Texte gris clair */
        }
 
        form input[type="text"],
        form input[type="number"] {
            width: calc(100% - 20px);
            padding: 10px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background-color: #fff; /* Fond blanc */
            color: #333; /* Texte noir */
            margin-bottom: 15px;
        }
 
        form input[type="text"]:focus,
        form input[type="number"]:focus {
            outline: none;
            border: 1px solid #ffd700; /* Bordure dorée au focus */
        }
 
        form button {
            width: 100%;
            padding: 10px;
            background-color: #4CAF50; /* Vert agréable */
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
        }
 
        form button:hover {
            background-color: #45a049; /* Vert légèrement plus foncé au survol */
            transform: translateY(-3px);
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3); /* Ombre au survol */
        }
 
        /* BOUTONS POUR REJOINDRE UNE PARTIE */
        .button {
            padding: 10px 20px;
            background-color: #4CAF50; /* Vert agréable */
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
        }
 
        .button:hover {
            background-color: #45a049;
            transform: translateY(-3px);
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3); /* Ombre au survol */
        }
 
        .disabled {
            background-color: #cccccc; /* Gris pour les boutons désactivés */
            cursor: not-allowed;
        }
</style>
    
    <%
    // Récupère le pseudo depuis la session HTTP (stocké lors du login)
    String pseudo = (String) session.getAttribute("loggedUser");
    if (pseudo == null) {
        // S'il n'est pas logué, on le renvoie à la page de login
        response.sendRedirect(request.getContextPath() + "/vue/login.jsp");
        return;
    }
	%>
    
    <script>
        let socket;
        let loggedUser = "<%= pseudo %>";
        /**
         * Se connecte au WebSocket "ws://localhost:8080/ProjetJ2ee/ws/parties",
         * puis gère les différents messages reçus (liste des parties, redirect, error).
         */
         function connectWebSocket() {
             // On construit l'URL en ajoutant ?user=...
             const wsUrl = "ws://localhost:8080/ProjetJ2ee/ws/parties?user=" 
                           + encodeURIComponent(loggedUser);
             console.log("Connexion WebSocket URL =", wsUrl);

             socket = new WebSocket(wsUrl);

             socket.onopen = function () {
                 console.log("WebSocket connecté pour pseudo =", loggedUser);
             };

             socket.onclose = function () {
                 console.log("WebSocket déconnecté.");
             };

             socket.onerror = function(event) {
                 console.error("WebSocket Error: ", event);
             };

             /**
              * Handler principal des messages reçus du serveur.
              */
             socket.onmessage = function(event) {
                 const rawData = event.data;
                 console.log("Message brut reçu du serveur :", rawData);

                 let data;
                 try {
                     data = JSON.parse(rawData);
                 } catch (parseError) {
                     console.error("Erreur de parsing JSON :", parseError);
                     return;
                 }

                 // 1) Si data est un tableau => c'est la liste des parties
                 if (Array.isArray(data)) {
                     majListeParties(data);
                     return;
                 }

                 // 2) Si on a data.redirect => on redirige vers salleAttente.jsp
                 if (data.redirect) {
                     console.log("Redirection demandée vers :", data.redirect);
                     window.location.href = data.redirect;
                     return;
                 }

                 // 3) Si on a data.error => on affiche une alerte
                 if (data.error) {
                     console.error("Erreur reçue :", data.error);
                     alert("Erreur : " + data.error);
                     return;
                 }
				 
                 // Sinon, c'est un format de message inconnu
                 console.warn("Message non géré :", data);
             };
         }

        /**
         * Met à jour l'affichage des parties dans la <div id="liste-parties">,
         * en créant un <div> par partie et en désactivant le bouton Rejoindre
         * si la partie est déjà pleine.
         * @param {Array} parties - Liste d'objets {gameId, nom, maxJoueurs, joueurs[], ...}
         */
        function majListeParties(parties) {
            const listeParties = document.getElementById("liste-parties");
            // On vide le contenu actuel
            listeParties.innerHTML = "";

            parties.forEach(partie => {
                const item = document.createElement("div");

                // Si la partie est pleine => bouton disabled
                const disabledAttr = (partie.joueurs.length >= partie.maxJoueurs) ? "disabled" : "";

                // Construction du HTML
                const content =
                    "<b>ID: " + partie.gameId + "</b>" +
                    " - <b>" + partie.nom + "</b>" +
                    " - " + partie.joueurs.length + "/" + partie.maxJoueurs + " joueurs " +
                    "<button class='button' onclick='rejoindrePartie(\"" + partie.gameId + "\")' " +
                          disabledAttr + ">Rejoindre</button>";

                item.innerHTML = content;
                listeParties.appendChild(item);
            });
        }

        /**
         * Envoie une action "creerPartie" au serveur pour créer une nouvelle partie.
         */
        function creerPartie() {
            const nom = document.getElementById("nom-partie").value;
            const maxJoueurs = document.getElementById("max-joueurs").value;
            if (nom && maxJoueurs) {
                socket.send(JSON.stringify({ action: "creerPartie", nom, maxJoueurs }));
            }
        }

        /**
         * Envoie une action "rejoindrePartie" au serveur pour rejoindre la partie indiquée.
         * Si le serveur accepte, il renverra { "redirect":"salleAttente.jsp?gameId=..." }
         * (ce qui déclenchera la redirection dans onmessage).
         */
        function rejoindrePartie(gameId) {
            console.log("Tentative de rejoindre la partie avec gameId =", gameId);
            socket.send(JSON.stringify({ action: "rejoindrePartie", gameId }));
        }

        // Se connecte au WebSocket dès que la page est chargée
        window.onload = connectWebSocket;
    </script>
</head>
<body>
    <div class="container">
        <h1>Lobby des Parties</h1>
        
        <!-- BOUTON VERS LA PAGE utilisateur.jsp -->
<div class="container">
    <form action="<%= request.getContextPath() %>/vue/utilisateur.jsp" method="get">
        <button type="submit" class="button">
            Mon Profil
        </button>
    </form>
</div>

        <h2>Parties disponibles :</h2>
        <div id="liste-parties">
            <p>Chargement des parties...</p>
        </div>

        <h2>Créer une nouvelle partie :</h2>
        <form onsubmit="event.preventDefault(); creerPartie();">
            <label for="nom-partie">Nom de la Partie :</label>
            <input type="text" id="nom-partie" required>
            <br>
            <label for="max-joueurs">Nombre de joueurs maximum (2-6) :</label>
            <input type="number" id="max-joueurs" min="2" max="6" required>
            <br><br>
            <button type="submit" class="button">Créer la Partie</button>
        </form>
    </div>
</body>
</html>