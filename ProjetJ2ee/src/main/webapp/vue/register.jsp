<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Inscription</title>
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

        /* FORMULAIRE */
        form {
            background-color: #3b3b3b; /* Fond du formulaire */
            padding: 30px 20px;
            border-radius: 10px; /* Coins arrondis */
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Ombre légère */
            width: 300px;
            display: flex;
            flex-direction: column; /* Organisation verticale */
            gap: 15px; /* Espacement entre les champs */
        }

        form label {
            font-size: 14px;
            color: #bbb; /* Texte gris clair */
        }

        form input[type="text"],
        form input[type="password"] {
            width: 93%;
            padding: 10px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background-color: #fff; /* Fond blanc */
            color: #333; /* Texte noir */
            margin-bottom: 15px; /* Ajout d'une marge entre les champs et le bouton */
        }

        form input[type="text"]:focus,
        form input[type="password"]:focus {
            outline: none;
            border: 1px solid #ffd700; /* Met en avant le champ sélectionné */
        }

        /* BOUTON */
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
            transform: translateY(-2px); /* Légère levée au survol */
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.3); /* Ombre au survol */
        }

        /* LIEN POUR LA CONNEXION */
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
