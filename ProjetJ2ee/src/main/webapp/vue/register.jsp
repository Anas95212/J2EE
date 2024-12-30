<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Inscription</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        form {
            margin-bottom: 20px;
        }
        label {
            display: inline-block;
            width: 150px;
            margin-bottom: 10px;
        }
        input {
            padding: 8px;
            margin-bottom: 10px;
            width: 200px;
        }
        button {
            padding: 8px 16px;
            background-color: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background-color: #45a049;
        }
        .message {
            margin-top: 20px;
            padding: 10px;
            border: 1px solid;
            width: 50%;
        }
        .error {
            color: red;
            border-color: red;
            background-color: #fdd;
        }
        .success {
            color: green;
            border-color: green;
            background-color: #dfd;
        }
    </style>
</head>
<body>
    <h1>Créer un compte</h1>
    <form action="<%= request.getContextPath() %>/Registercontroller" method="post">
        <input type="hidden" name="action" value="register">
        <label for="username">Nom d'utilisateur :</label>
        <input type="text" id="username" name="username" required>
        <br>
        <label for="password">Mot de passe :</label>
        <input type="password" id="password" name="password" required>
        <br>
        <button type="submit">S'inscrire</button>
    </form>

    <!-- Afficher les messages d'erreur ou de succès -->
    <% 
        String errorMessage = (String) request.getAttribute("errorMessage");
        String successMessage = (String) request.getAttribute("msg");
        if (errorMessage != null) { 
    %>
        <div class="message error"><%= errorMessage %></div>
    <% 
        } else if (successMessage != null) { 
    %>
        <div class="message success"><%= successMessage %></div>
    <% 
        } 
    %>
</body>
</html>
