<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Combat en Cours</title>
    <link rel="stylesheet" type="text/css" href="../css/style.css">
</head>
<body>
    <h1>Combat en cours</h1>
    <p>Attaquant : <%= request.getAttribute("attaquant") %></p>
    <p>Défenseur : <%= request.getAttribute("defenseur") %></p>
    <p>Résultat : <%= request.getAttribute("resultatCombat") %></p>
</body>
</html>