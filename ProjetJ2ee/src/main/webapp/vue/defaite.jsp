<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Défaite</title>
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
            color: #ff4c4c;
            font-size: 48px;
            margin-bottom: 20px;
        }
        p {
            font-size: 20px;
            margin-bottom: 40px;
        }
        a {
            text-decoration: none;
            color: #4CAF50;
            font-size: 18px;
            padding: 10px 20px;
            border: 1px solid #4CAF50;
            border-radius: 5px;
        }
        a:hover {
            background-color: #4CAF50;
            color: white;
        }
    </style>
</head>
<body>
    <h1>Défaite...</h1>
    <p>Vous avez été éliminé. Mieux vaut essayer une nouvelle partie !</p>
    
    <p class="score">Votre score final : <span id="user-score"></span></p>
    
    <script>
        // Récupérer le score depuis la session
        const userScore = sessionStorage.getItem("userScore");
        document.getElementById("user-score").innerText = userScore || "Non disponible";
    </script>
    
    <a href="<%= request.getContextPath() %>/vue/lobby.jsp">Retour au Lobby</a>
    
    
</body>
</html>
