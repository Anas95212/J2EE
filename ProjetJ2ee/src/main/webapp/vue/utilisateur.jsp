<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="java.sql.*" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="controller.DatabaseConnection" %>
 
<!DOCTYPE html>
<html>
<head>
    <title>Profil de l'utilisateur</title>
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
            justify-content: center;
            height: 100vh;
        }
        h1 {
            color: #ffd700; /* Couleur dorée */
            margin-bottom: 20px;
        }
        .profile-container {
            background-color: #3b3b3b;
            padding: 30px 20px;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
            text-align: center;
        }
        .profile-data {
            margin: 10px 0;
        }
        .btn-lobby {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 12px 20px;
            font-size: 16px;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-top: 20px;
            text-decoration: none; /* on va faire un lien */
        }
        .btn-lobby:hover {
            background-color: #45a049;
            transform: translateY(-3px);
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3);
        }
    </style>
 
<%
    // -------------------------------------------
    // 1) Vérifier que l'utilisateur est logué
    // -------------------------------------------
    HttpSession mysession = request.getSession(false);
    if (session == null || session.getAttribute("loggedUser") == null) {
        // S'il n'est pas logué, on redirige vers login
        response.sendRedirect(request.getContextPath() + "/vue/login.jsp");
        return;
    }
 
    String pseudo = (String) session.getAttribute("loggedUser");
 
    // -------------------------------------------
    // 2) Calculer le score total dans la table parties
    // -------------------------------------------
    int scoreTotal = 0;
    // Ex: votre classe DatabaseConnection
    //     ou adapter la connexion si besoin
    try (Connection conn = DatabaseConnection.getConnection()) {
        // On somme tous les 'score_joueur' pour ce pseudo
        String sql = "SELECT SUM(score_joueur) AS totalScore "
                   + "FROM parties "
                   + "WHERE pseudo_joueur = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pseudo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    scoreTotal = rs.getInt("totalScore");
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
</head>
<body>
    <div class="profile-container">
        <h1>Mon Profil</h1>
 
        <div class="profile-data">
            <strong>Nom d'utilisateur :</strong>
            <%= pseudo %>
        </div>
 
        <div class="profile-data">
            <strong>Score total :</strong>
            <%= scoreTotal %>
        </div></br>
        </br>
 
        <!-- BOUTON RETOUR LOBBY -->
        <a class="btn-lobby" href="<%= request.getContextPath() + "/lobby" %>">
            Revenir au Lobby
        </a>
    </div>
 
</body>
</html>