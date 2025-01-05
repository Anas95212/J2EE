<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpServlet" %>
<%@ page import="jakarta.servlet.http.HttpServletRequest" %>
<%@ page import="jakarta.servlet.http.HttpServletResponse" %>
<!DOCTYPE html>
<html>
<head>
    <title>Scores</title>
</head>
<body>
    <h1>Scores</h1>
    <p>Votre score est : ${playerScore}</p>
    <a href="game.jsp">Retour au jeu</a>
</body>
</html>
