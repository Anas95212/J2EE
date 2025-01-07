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
            background: linear-gradient(to bottom, #2e3440, #3b4252);
            color: #eceff4;
            font-family: 'Arial', sans-serif;
            text-align: center;
            margin: 0;
            padding: 0;
        }
 
        .combat-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            max-width: 1000px;
            margin: 50px auto;
            padding: 20px;
            border-radius: 15px;
            background-color: rgba(59, 66, 82, 0.9);
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
        }
 
        .soldier {
            text-align: center;
            flex: 1;
        }
 
        .soldier img {
            width: 150px;
            height: 150px;
            object-fit: contain;
            margin-bottom: 10px;
            border-radius: 50%;
            border: 3px solid #88c0d0;
        }
 
        .soldier1 img {
            border-color: #bf616a;
        }
 
        .soldier2 img {
            border-color: #a3be8c;
        }
.health-bar-container {
    width: 100%; /* Utiliser toute la largeur disponible */
    margin: 0 auto 10px;
    height: 15px;
    background-color: #4c566a;
    border-radius: 10px;
    overflow: hidden;
    position: relative;
}
 
.health-bar {
    height: 100%;
    background: linear-gradient(to right, #a3be8c, #ebcb8b, #bf616a);
    transition: width 0.3s ease;
    width: 93%; /* Par défaut 100% pour éviter les erreurs visuelles */
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
            margin-top: 10px;
            box-shadow: 0 3px 5px rgba(0, 0, 0, 0.3);
            font-weight: bold;
        }
 
        .action-button:disabled {
            background: #4c566a;
            cursor: not-allowed;
        }
 
        .turn-indicator {
            font-size: 20px;
            font-weight: bold;
            color: #eceff4;
            margin-top: 20px;
        }
 
        .turn-indicator span {
            color: #bf616a;
        }
 
        .turn-indicator .soldat2 {
            color: #a3be8c;
        }
 
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
            0% {
                transform: rotate(0deg) translateX(0px);
            }
            20% {
                transform: rotate(360deg) translateX(50px);
            }
            40% {
                transform: rotate(720deg) translateX(100px);
            }
            60% {
                transform: rotate(1080deg) translateX(50px);
            }
            80% {
                transform: rotate(1440deg) translateX(0px);
            }
            100% {
                transform: rotate(1800deg) translateX(0px);
            }
        }
    </style>
    <script>
        let gameId = "<%= gameId %>";
        let combatId = "<%= combat.getCombatId() %>";
        let pseudo = "<%= pseudo %>";
        let jeSuisDansLeCombat = <%= jeSuisDansLeCombat %>;
 
        let intervalId = null;
 
        function connectWebSocket() {
            const wsUrl = "ws://" + location.host + "<%= request.getContextPath() %>/ws/parties?user=" + encodeURIComponent("<%= pseudo %>");
            const socket = new WebSocket(wsUrl);
 
            socket.onmessage = function (event) {
                try {
                    const data = JSON.parse(event.data);
 
                    if (data.type === "combatStart") {
                        console.log("Début du combat détecté. Redirection...");
                        window.location.href = data.redirect;
                    } else if (data.type === "combatEnd") {
                        console.log("Fin du combat détectée. Retour à la carte du jeu.");
                        window.location.href = data.redirect;
                    }else if (data.type === "defeat" && data.pseudo === "<%= pseudo %>") {
                        alert("Vous avez perdu !");
                        window.location.href = "<%= request.getContextPath() %>/vue/defaite.jsp";
                    } else if (data.type === "victory" && data.pseudo === "<%= pseudo %>") {
                        alert("Félicitations, vous avez gagné !");
                        window.location.href = "<%= request.getContextPath() %>/vue/victoire.jsp";
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
 
        connectWebSocket();
 
        function updateHealthBars(soldier1PV, soldier2PV) {
            const maxPV = 20; // Maximum de points de vie (modifiable si nécessaire)
 
            // Calculer la largeur en pourcentage
            const widthSoldier1 = (soldier1PV / maxPV) * 100;
            const widthSoldier2 = (soldier2PV / maxPV) * 100;
 
            // Mettre à jour les barres de santé
            document.getElementById("healthBar1").style.width = widthSoldier1 + "%";
            document.getElementById("healthBar2").style.width = widthSoldier2 + "%";
 
            // Mettre à jour les informations de PV
            document.getElementById("healthInfo1").textContent = "PV : " + soldier1PV;
            document.getElementById("healthInfo2").textContent = "PV : " + soldier2PV;
        }
 
        function fetchCombatState() {
            fetch("<%= request.getContextPath() %>/CombatController?action=getState&gameId=" + gameId + "&combatId=" + combatId)
                .then(response => response.json())
                .then(data => {
                    if (data.error) {
                        console.error("Erreur combat:", data.error);
                        return;
                    }
 
                    updateHealthBars(data.pvSoldat1, data.pvSoldat2);
 
                    const turnIndicator = document.querySelector(".turn-indicator span");
                    turnIndicator.textContent = data.tourSoldat1 ? "Soldat1" : "Soldat2";
                    turnIndicator.className = data.tourSoldat1 ? "" : "soldat2";
 
                    const attackButton = document.querySelector(".action-button");
                    const monTour =
                        (data.tourSoldat1 && data.soldat1Owner === pseudo) ||
                        (!data.tourSoldat1 && data.soldat2Owner === pseudo);
 
                    attackButton.disabled = !monTour;
 
                    if (!data.enCours && intervalId) {
                        clearInterval(intervalId);
                        intervalId = null;
                        window.location.href = "<%= request.getContextPath() %>/game.jsp?gameId=" + gameId;
                    }
                })
                .catch(error => console.error("Erreur fetch:", error));
        }
 
        function lancerDe() {
            const deElement = document.getElementById("de");
 
            // Initialisation pour l'animation
            deElement.textContent = "?"; // Affiche un "?" initial
            deElement.classList.add("lancer");
 
            // Envoyer une requête au serveur pour lancer le dé
            fetch("<%= request.getContextPath() %>/CombatController?action=rollDice&gameId=" + gameId + "&combatId=" + combatId)
                .then(resp => resp.json())
                .then(data => {
                    if (data.error) {
                        console.error("Erreur lancerDe:", data.error);
                        alert("Erreur lors de l'attaque : " + data.error);
                    } else {
                        // Après 1 seconde, afficher la valeur réelle du dé
                        setTimeout(() => {
                            deElement.textContent = data.valeurDe; // Affiche la valeur du dé
                            deElement.classList.remove("lancer");
 
                            // Laisser la valeur affichée pendant 5 secondes avant de continuer
                            setTimeout(() => {
                                // Mettre à jour les PV des soldats
                                updateHealthBars(data.pvSoldat1, data.pvSoldat2);
 
                                // Mettre à jour le tour actuel
                                const turnIndicator = document.querySelector(".turn-indicator span");
                                turnIndicator.textContent = data.tourSoldat1 ? "Soldat1" : "Soldat2";
                                turnIndicator.className = data.tourSoldat1 ? "" : "soldat2";
 
                                // Activer ou désactiver le bouton selon le tour
                                const attackButton = document.querySelector(".action-button");
                                const monTour =
                                    (data.tourSoldat1 && pseudo === data.soldat1Owner) ||
                                    (!data.tourSoldat1 && pseudo === data.soldat2Owner);
                                attackButton.disabled = !monTour;
 
                                // Vérifier si le combat est terminé
                                if (!data.enCours) {
                                    alert("Le combat est terminé !");
                                    window.location.href = "<%= request.getContextPath() %>/game.jsp?gameId=" + gameId;
                                }
                            }, 5000); // Laisser le dé affiché pendant 5 secondes avant de continuer
                        }, 1000); // Attendre 1 seconde pour l'animation avant d'afficher la valeur du dé
                    }
                })
                .catch(err => console.error("Erreur lancerDe:", err));
        }
 
        window.onload = () => {
            intervalId = setInterval(fetchCombatState, 1000);
        };
 
 
 
    </script>
</head>
<body>
    <h1 style="color: #88c0d0; margin-top: 20px;">Combat en cours</h1>
    <div class="combat-container">
<div class="soldier soldier1">
    <h2 style="color: #bf616a;">Soldat 1 (propriétaire : <%= s1.getOwner().getLogin() %>)</h2>
    <img src="<%= request.getContextPath() %>/vue/images/knight.png" alt="Soldat1">
    <div class="health-bar-container">
        <div class="health-bar" id="healthBar1" style="width: <%= (combat.getPvSoldat1() * 100 / 20) %>%"></div>
    </div>
    <div class="health-info" id="healthInfo1">PV : <%= combat.getPvSoldat1() %></div>
</div>
<div class="soldier soldier2">
    <h2 style="color: #a3be8c;">Soldat 2 (propriétaire : <%= s2.getOwner().getLogin() %>)</h2>
    <img src="<%= request.getContextPath() %>/vue/images/knight.png" alt="Soldat2">
    <div class="health-bar-container">
        <div class="health-bar" id="healthBar2" style="width: <%= (combat.getPvSoldat2() * 100 / 20) %>%"></div>
    </div>
    <div class="health-info" id="healthInfo2">PV : <%= combat.getPvSoldat2() %></div>
</div>
 
    </div>
    <div class="turn-indicator">
        Tour actuel : <span class="<%= combat.isTourSoldat1() ? "" : "soldat2" %>">
            <%= combat.isTourSoldat1() ? "Soldat1" : "Soldat2" %>
        </span>
    </div>
    <div>
        <div class="de" id="de">?</div>
    </div>
    <button class="action-button" onclick="lancerDe()" <%= jeSuisDansLeCombat ? "" : "disabled" %>>
        Attaquer
    </button>
</body>
</html>