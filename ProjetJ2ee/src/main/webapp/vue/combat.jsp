<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Partie, model.Combat, model.Soldat, model.Joueur, controller.PartieWebSocket" %>
<%
    String pseudo = (String) session.getAttribute("loggedUser");
    if (pseudo == null) {
        out.println("Vous n'êtes pas connecté !");
        return;
    }

    String gameId = request.getParameter("gameId");
    String combatId = request.getParameter("combatId");

    // On recherche la partie
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

    Combat combat = partie.getCombatEnCours();
    if (combat == null) {
        out.println("Aucun combat en cours !");
        return;
    }
    // Vérifier ID, si on veut
    if (combatId != null && !combat.getCombatId().equals(combatId)) {
        out.println("Combat ID non valide !");
        return;
    }

    Soldat s1 = combat.getSoldat1();
    Soldat s2 = combat.getSoldat2();
    boolean jeSuisSoldat1 = s1.getOwner().getLogin().equals(pseudo);
    boolean jeSuisSoldat2 = s2.getOwner().getLogin().equals(pseudo);
    boolean jeSuisDansLeCombat = (jeSuisSoldat1 || jeSuisSoldat2);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Combat en cours</title>
    <style>
        body {
            background-color: #333; color: #fff;
            text-align: center; font-family: Arial, sans-serif;
        }
        .container {
            margin-top: 50px;
        }
        button {
            background-color: #4CAF50; color: #fff;
            border: none; padding: 10px 20px; cursor: pointer;
            font-size: 16px; border-radius: 5px; margin: 10px;
        }
        button:disabled {
            background-color: #999;
            cursor: not-allowed;
        }
    </style>
    <script>
        let gameId = "<%= gameId %>";
        let combatId = "<%= combat.getCombatId() %>";
        let pseudo = "<%= pseudo %>";
        let jeSuisDansLeCombat = <%= jeSuisDansLeCombat %>;

        let intervalId = null;
     // Connexion au WebSocket
function connectWebSocket() {
    const wsUrl = "ws://" + location.host + "<%= request.getContextPath() %>/ws/parties?user=" + encodeURIComponent("<%= pseudo %>");
    const socket = new WebSocket(wsUrl);

    socket.onmessage = function (event) {
        try {
            const data = JSON.parse(event.data);

            if (data.type === "combatStart") {
                console.log("Début du combat détecté. Redirection...");
                window.location.href = data.redirect; // Redirection vers combat.jsp
            } else {
                console.log("Message WebSocket reçu :", data);
            }
        } catch (error) {
            console.error("Erreur lors de la réception du message WebSocket :", error);
        }
    };

    socket.onopen = () => console.log("WebSocket connecté.");
    socket.onclose = () => console.log("WebSocket déconnecté.");
    socket.onerror = (event) => console.error("WebSocket erreur :", event);
}

// Connecter le WebSocket dès que la page est chargée
connectWebSocket();

        function fetchCombatState() {
            fetch("<%= request.getContextPath() %>/CombatController?action=getState&gameId=" + gameId + "&combatId=" + combatId)
                .then(resp => resp.json())
                .then(data => {
                    if (data.error) {
                        console.error("Erreur combat:", data.error);
                        return;
                    }

                    // Mettre à jour l'affichage
                    document.getElementById("pvSoldat1").textContent = data.pvSoldat1;
                    document.getElementById("pvSoldat2").textContent = data.pvSoldat2;
                    document.getElementById("tour").textContent = data.tourSoldat1 ? "Soldat1" : "Soldat2";

                    let monTour = false;
                    if (data.tourSoldat1 && data.soldat1Owner === pseudo) {
                        monTour = true;
                    }
                    if (!data.tourSoldat1 && data.soldat2Owner === pseudo) {
                        monTour = true;
                    }

                    // Bouton lancé de dé activé que si c'est mon tour
                    let btn = document.getElementById("btnLancerDe");
                    if (btn) btn.disabled = (!monTour || !jeSuisDansLeCombat);

                    // Si enCours=false => plus de polling, mais le WebSocket 
                    // redirigera tout le monde vers game.jsp
                    if (!data.enCours && intervalId) {
                        clearInterval(intervalId);
                        intervalId = null;
                    }
                })
                .catch(err => console.error("Erreur fetch:", err));
        }

        function lancerDe() {
            fetch("<%= request.getContextPath() %>/CombatController?action=rollDice&gameId=" + gameId + "&combatId=" + combatId)
                .then(resp => resp.json())
                .then(data => {
                    if (data.error) {
                        console.error("Erreur lancerDe:", data.error);
                    }
                    // Sinon, on attend le prochain fetchCombatState pour voir le résultat
                })
                .catch(err => console.error("Erreur lancerDe:", err));
        }

        window.onload = function() {
            intervalId = setInterval(fetchCombatState, 1000);
        };
    </script>
</head>
<body>
<div class="container">
    <h1>Combat en cours</h1>
    <p>Soldat1 (propriétaire: <%= s1.getOwner().getLogin() %>) — PV: <span id="pvSoldat1"><%= combat.getPvSoldat1() %></span></p>
    <p>Soldat2 (propriétaire: <%= s2.getOwner().getLogin() %>) — PV: <span id="pvSoldat2"><%= combat.getPvSoldat2() %></span></p>
    <h3>Tour actuel: <span id="tour"><%= combat.isTourSoldat1() ? "Soldat1" : "Soldat2" %></span></h3>

    <button id="btnLancerDe" onclick="lancerDe()" <%= jeSuisDansLeCombat ? "" : "disabled" %> >
        Lancer le dé
    </button>
</div>
</body>
</html>
