<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Connexion au Jeu 4X</title>
    <style>
        /* STYLE GLOBAL */
        body {
            font-family: 'Arial', sans-serif; /* Police moderne */
            background-color: #2d2d2d; /* Fond sombre */
            color: #fff; /* Texte en blanc pour contraste */
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh; /* Prend toute la hauteur de la fenêtre */
        }

        /* TITRE PRINCIPAL */
        h1 {
            text-align: center;
            margin-bottom: 20px;
            font-size: 28px;
            color: #ffd700; /* Doré pour le titre */
            text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
        }

        /* FORMULAIRE DE CONNEXION */
        form {
            background-color: #3b3b3b; /* Fond du formulaire */
            padding: 30px 20px;
            border-radius: 10px; /* Coins arrondis */
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre */
            width: 300px;
            display: flex;
            flex-direction: column; /* Organise les éléments verticalement */
            gap: 15px; /* Espace entre les champs */
        }

        /* CHAMPS DU FORMULAIRE */
        form label {
            font-size: 14px;
            color: #bbb; /* Couleur gris clair */
        }

        form input[type="text"],
        form input[type="password"] {
            width: 100%;
            padding: 10px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-sizing: border-box;
            background-color: #fff; /* Fond blanc */
            color: #333; /* Texte noir */
            margin-bottom: 15px; /* Ajout d'une marge entre les champs et le bouton */
        }

        form input[type="text"]:focus,
        form input[type="password"]:focus {
            outline: none;
            border: 1px solid #ffd700; /* Met en avant le champ sélectionné */
        }

        /* BOUTON DE CONNEXION */
        form button {
            background-color: #4CAF50; /* Vert agréable */
            color: white;
            border: none;
            padding: 10px;
            font-size: 16px;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        form button:hover {
            background-color: #45a049; /* Vert légèrement plus foncé au survol */
            transform: translateY(-2px); /* Légère levée pour effet interactif */
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3); /* Ombre au survol */
        }

        /* LIEN POUR L'INSCRIPTION */
        p {
            text-align: center;
            margin-top: 10px;
            font-size: 14px;
        }

        p a {
            color: #ffd700; /* Doré pour le lien */
            text-decoration: none;
            transition: color 0.3s ease;
        }

        p a:hover {
            color: #fff; /* Passe au blanc au survol */
        }
    </style>
</head>
<body>
	<div>
	    <h1>Bienvenue dans le jeu 4X</h1>
	    
	    <!-- Affichage des messages d'erreur ou de validation -->
	    <%
	        String errorMessage = (String) request.getAttribute("errorMessage");
	        String successMessage = (String) request.getAttribute("successMessage");
	        if (errorMessage != null) {
	    %>
	        <div class="error-message" style="color: red;"><%= errorMessage %></div></br>
	    <%
	        }
	        if (successMessage != null) {
	    %>
	        <div class="success-message" style="color: green;"><%= successMessage %></div></br>
	    <%
	        }
	    %>
	    
	    <form action="<%= request.getContextPath() %>/controller" method="post">
	        <input type="hidden" name="action" value="login">
	        <label for="username">Nom d'utilisateur :</label>
	        <input type="text" id="username" name="username" required>
	        <label for="password">Mot de passe :</label>
	        <input type="password" id="password" name="password" required>
	        <button type="submit">Se connecter</button>
	    </form>
	    <% String contextPath = request.getContextPath(); %>
	    <p>Pas encore de compte ? <a href="<%= contextPath %>/vue/register.jsp">Inscrivez-vous ici</a></p>
    </div>
</body>
</html>