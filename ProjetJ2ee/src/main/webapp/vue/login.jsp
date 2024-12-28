<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Connexion au Jeu 4X</title>
    <link rel="stylesheet" type="text/css" href="../css/style.css">
</head>
<body>
    <h1>Bienvenue dans le jeu 4X</h1>
    <form action="<%= request.getContextPath() %>/controller"e method="post">
    <input type="hidden" name="action" value="login">
    <label for="username">Nom d'utilisateur :</label>
    <input type="text" id="username" name="username" required>
    <br>
    <label for="password">Mot de passe :</label>
    <input type="password" id="password" name="password" required>
    <br>
    <button type="submit">Se connecter</button>
</form>
<% String contextPath = request.getContextPath(); %>
<p>Pas encore de compte ? <a href="<%= contextPath %>/vue/register.jsp">Inscrivez-vous ici</a></p>

</body>
</html>