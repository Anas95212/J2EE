<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Partie, model.CombatVille, model.Soldat, model.Ville, controller.PartieWebSocket" %>

<%
    // 1) Vérification session
    String pseudo = (String) session.getAttribute("loggedUser");
    if (pseudo == null) {
        out.println("Vous n'êtes pas connecté !");
        return;
    }

    // 2) Récupérer gameId et combatId
    String gameId = request.getParameter("gameId");
    String combatId = request.getParameter("combatId");

    // 3) Trouver la partie
    Partie partie = null;
    for (Partie p : PartieWebSocket.getParties()) {
        if (p.getGameId().equals(gameId)) {
            partie = p;
            break;
        }
    }
    if (partie == null) {
        out.println("Partie introuvable !");
        return;
    }

    // 4) Récupérer le CombatVille
    CombatVille combatVille = partie.getCombatVilleEnCours();
    if (combatVille == null) {
        out.println("Aucun combat Ville en cours !");
        return;
    }

    // 5) Vérifier l'ID
    if (!combatVille.getCombatId().equals(combatId)) {
        out.println("Combat ID non valide !");
        return;
    }

    // 6) Récupérer soldat et ville
    Soldat soldat = combatVille.getSoldatAttaquant();
    Ville ville   = combatVille.getVilleCible();
    boolean isAttacker = pseudo.equals(soldat.getOwner().getLogin());

    // 7) Récupérer les couleurs
    String couleurSoldat = (soldat.getOwner().getCouleur() != null)
        ? soldat.getOwner().getCouleur()
        : "#88c0d0";

    String couleurVille = "#88c0d0";
    if (ville.getProprietaire() != null && ville.getProprietaire().getCouleur() != null) {
        couleurVille = ville.getProprietaire().getCouleur();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Combat Soldat vs Ville</title>
    <style>
        body {
            background: linear-gradient(to bottom, #2e3440, #3b4252);
            color: #eceff4;
            font-family: 'Arial', sans-serif;
            text-align: center;
            margin: 0;
            padding: 0;
        }
        .combat-container {
            display: flex;
            justify-content: space-around;
            align-items: center;
            max-width: 900px;
            margin: 50px auto;
            padding: 20px;
            border-radius: 15px;
            background-color: rgba(59, 66, 82, 0.9);
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
        }
        .entity {
            width: 200px;
        }
        .entity img {
            width: 150px;
            height: 150px;
            object-fit: contain;
            margin-bottom: 10px;
            border-radius: 50%;
            border: 3px solid #88c0d0; /* Par défaut */
        }
        /* soldat => bordure avec la couleur du joueur */
        .soldat img {
            border-color: <%= couleurSoldat %>;
        }
        /* ville => bordure avec la couleur de la ville (si conquise) */
        .ville img {
            border-color: <%= couleurVille %>;
        }

        .health-bar-container {
            width: 100%;
            margin: 0 auto 10px;
            height: 15px;
            background-color: #4c566a;
            border-radius: 10px;
            overflow: hidden;
        }
        .health-bar {
            height: 100%;
            background: linear-gradient(to right, #a3be8c, #ebcb8b, #bf616a);
            transition: width 0.3s ease;
        }
        .health-info {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .action-button {
            background: #5e81ac;
            color: #eceff4;
            font-size: 16px;
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            margin-top: 20px;
            box-shadow: 0 3px 5px rgba(0, 0, 0, 0.3);
            font-weight: bold;
        }
        .action-button:disabled {
            background: #4c566a;
            cursor: not-allowed;
        }
        h1 {
            color: #88c0d0;
            margin-top: 20px;
        }
        /* Style du dé */
        .de {
            font-size: 36px;
            font-weight: bold;
            margin-top: 20px;
            border-radius: 10px;
            width: 50px;
            height: 50px;
            display: inline-block;
            text-align: center;
            line-height: 50px;
            background-color: #ccc;
            transition: transform 1s;
        }
        .lancer {
            animation: lancer 1s;
        }
        @keyframes lancer {
            0%   { transform: rotate(0deg)   translateX(0px);   }
            20%  { transform: rotate(360deg) translateX(50px);  }
            40%  { transform: rotate(720deg) translateX(100px); }
            60%  { transform: rotate(1080deg)translateX(50px);  }
            80%  { transform: rotate(1440deg)translateX(0px);   }
            100% { transform: rotate(1800deg)translateX(0px);   }
        }
    </style>

    <script>
    // Récupération des variables côté serveur
    const pseudo   = "<%= pseudo %>";
    const gameId   = "<%= gameId %>";
    const combatId = "<%= combatId %>";

    // Variables pour l'état du combat
    let pvSoldat   = 0;
    let defVille   = 0;
    let enCours    = true;
    let tourSoldat = true;

    // Valeurs "max" pour l’affichage des barres de vie
    const maxSoldat = 20;
    const maxVille  = 100;

    let socket = null; // WebSocket

    // === 1) Connexion WebSocket pour écouter "combatVilleEnd", "defeat", "victory", etc.
    function connectWebSocket() {
        const wsUrl = "ws://" + location.host 
                    + "<%= request.getContextPath() %>/ws/parties?user=" 
                    + encodeURIComponent(pseudo);

        socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            console.log("cityCombat.jsp: WebSocket connecté au serveur.");
        };

        socket.onclose = () => {
            console.log("cityCombat.jsp: WebSocket déconnecté.");
        };

        socket.onerror = (err) => {
            console.error("cityCombat.jsp: Erreur WebSocket:", err);
        };

        socket.onmessage = (event) => {
            let data = null;
            try {
                data = JSON.parse(event.data);
            } catch (e) {
                console.error("cityCombat.jsp: Erreur de parsing JSON:", e);
                return;
            }

            // Traitement des différents types de messages
            if (data.type === "combatVilleEnd") {
                // => On redirige tout le monde vers game.jsp (fin du combat)
                console.log("Réception de combatVilleEnd, redirection...");
                window.location.href = data.redirect;

            }
            else if (data.type === "combatVilleUpdate") {
                // Mettre à jour localement la barre de vie, etc.
                pvSoldat   = data.pvSoldat;
                defVille   = data.defVille;
                tourSoldat = data.tourSoldat;
                // Optionnel: data.valeurDe si tu veux afficher le dé

                updateBars();

                // Actualiser le bouton Attaquer selon tourSoldat
                document.getElementById("btnAttaque").disabled = !tourSoldat;
            }else if (data.type === "defeat" && data.pseudo === pseudo) {
                // => Joueur éliminé
                alert("Vous avez été éliminé !");
                window.location.href = "<%= request.getContextPath() %>/vue/defaite.jsp"
                    + "?pseudo=" + encodeURIComponent(pseudo)
                    + "&score=" + encodeURIComponent(data.score)
                    + "&gameId=" + encodeURIComponent(gameId);

            } else if (data.type === "victory" && data.pseudo === pseudo) {
                // => Joueur vainqueur
                alert("Félicitations, vous avez gagné !");
                window.location.href = "<%= request.getContextPath() %>/vue/victoire.jsp"
                    + "?pseudo=" + encodeURIComponent(pseudo)
                    + "&score=" + encodeURIComponent(data.score)
                    + "&gameId=" + encodeURIComponent(gameId);

            } else if (data.type === "gameEnd") {
                // => Fin de partie générale
                alert("La partie est terminée !");
                window.location.href = "<%= request.getContextPath() %>/vue/lobby.jsp";

            } else {
                // Pour debug, ou d’autres types de messages
                console.log("Message WebSocket reçu:", data);
            }
        };
    }

    // === 2) Récupère l'état du combat via CityCombatController (action=getState)
    function fetchState() {
        fetch("<%= request.getContextPath() %>/CityCombatController?action=getState"
              + "&gameId=" + gameId
              + "&combatId=" + combatId)
        .then(resp => resp.json())
        .then(data => {
            if (data.error) {
                console.error("Erreur fetchState:", data.error);
                return;
            }
            // Mettre à jour les variables
            pvSoldat   = data.pvSoldat;
            defVille   = data.defVille;
            enCours    = data.enCours;
            tourSoldat = data.tourSoldat;

            updateBars();

            const btnAttaque = document.getElementById("btnAttaque");
            if (!enCours) {
                // Combat fini => pour l'attaquant local
                btnAttaque.disabled = true;
                alert("Combat terminé !");
                // On peut attendre un peu avant d'éventuellement
                // laisser le WebSocket rediriger tout le monde.
                // setTimeout(() => { ... }, 2000);

            } else {
                // Détermine si c'est le tour du soldat (client) ou pas
                btnAttaque.disabled = !tourSoldat;
            }
        })
        .catch(err => console.error("Erreur fetchState:", err));
    }

    // === 3) Met à jour les barres de vie Soldat & Ville
    function updateBars() {
        let pctSoldat = (pvSoldat / maxSoldat) * 100;
        let pctVille  = (defVille  / maxVille) * 100;

        document.getElementById("soldat-bar").style.width = pctSoldat + "%";
        document.getElementById("ville-bar").style.width  = pctVille  + "%";

        document.getElementById("soldat-info").textContent 
            = "Soldat : " + pvSoldat + " / " + maxSoldat;

        document.getElementById("ville-info").textContent  
            = "Ville : " + defVille + " / " + maxVille;
    }

    // === 4) Le soldat attaque la ville (rollDice)
    function attaquerVille() {
        const deElement = document.getElementById("de");
        deElement.textContent = "?";
        deElement.classList.add("lancer");

        fetch("<%= request.getContextPath() %>/CityCombatController?action=rollDice"
              + "&gameId=" + gameId
              + "&combatId=" + combatId)
        .then(resp => resp.json())
        .then(data => {
            setTimeout(() => {
                // Afficher la valeur du dé après 1s
                deElement.textContent = data.valeurDe;
                deElement.classList.remove("lancer");
            }, 1000);

            pvSoldat   = data.pvSoldat;
            defVille   = data.defVille;
            enCours    = data.enCours;
            tourSoldat = data.tourSoldat;

            updateBars();

            if (!enCours) {
                alert("Combat terminé !");
                // Ici, on peut laisser le WebSocket faire la redirection globale
            } else {
                if (!tourSoldat) {
                    setTimeout(riposteVille, 2000);
                }
            }
        })
        .catch(err => console.error("Erreur attaquerVille:", err));
    }

    // === 5) La ville riposte
    function riposteVille() {
        const deElement = document.getElementById("de");
        deElement.textContent = "?";
        deElement.classList.add("lancer");

        fetch("<%= request.getContextPath() %>/CityCombatController?action=riposte"
              + "&gameId=" + gameId
              + "&combatId=" + combatId)
        .then(resp => resp.json())
        .then(data => {
            setTimeout(() => {
                deElement.textContent = data.valeurDe;
                deElement.classList.remove("lancer");
            }, 1000);

            pvSoldat   = data.pvSoldat;
            defVille   = data.defVille;
            enCours    = data.enCours;
            tourSoldat = data.tourSoldat;

            updateBars();

            if (!enCours) {
                alert("Combat terminé !");
                // Le WebSocket va gérer la redirection commune
            } else {
                // On redonne la main au soldat s’il doit rejouer
                document.getElementById("btnAttaque").disabled = !tourSoldat;
            }
        })
        .catch(err => console.error("Erreur riposteVille:", err));
    }

    // === 6) Au chargement de la page
    window.onload = () => {
        connectWebSocket(); // connexion pour écouter "combatVilleEnd" & co.
        fetchState();       // chargement initial de l'état
    };
</script>

</head>

<body>
    <h1>Combat : Soldat vs Ville</h1>

    <!-- Conteneur global pour le soldat et la ville -->
    <div class="combat-container">

        <!-- Le soldat attaquant -->
        <div class="entity soldat">
            <img src="<%= request.getContextPath() %>/vue/images/knight.png" alt="Soldat">
            <div class="health-bar-container">
                <div class="health-bar" id="soldat-bar"></div>
            </div>
            <div class="health-info" id="soldat-info">Soldat : ? / ?</div>
        </div>

        <!-- La ville ciblée -->
        <div class="entity ville">
            <img src="<%= request.getContextPath() %>/vue/images/castle.png" alt="Ville">
            <div class="health-bar-container">
                <div class="health-bar" id="ville-bar"></div>
            </div>
            <div class="health-info" id="ville-info">Ville : ? / ?</div>
        </div>

    </div>

    <!-- Le dé, pour l'animation -->
    <div style="margin-top: 20px;">
        <div class="de" id="de">?</div>
    </div>

    <!-- Bouton d'attaque -->
    <button class="action-button id="btnAttaque"
        onclick="attaquerVille()"
        <%= isAttacker ? "" : "disabled" %>>
    Attaquer la ville
</button>
</body>
</html>
