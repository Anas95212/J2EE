<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Victoire</title>
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
            color: #ffd700;
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
    <h1>Victoire !</h1>
    <p>Félicitations, vous êtes le dernier joueur en vie et vous avez remporté la partie !</p>
    
    <h2>Scores finaux :</h2>
    <table>
        <thead>
            <tr>
                <th>Pseudo</th>
                <th>Score</th>
            </tr>
        </thead>
        <tbody id="scores-table">
            <!-- Les scores seront ajoutés ici via WebSocket -->
        </tbody>
    </table>
    <a href="<%= request.getContextPath() %>/vue/lobby.jsp">Retour au Lobby</a>
</body>
</html>
