<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Carte" %>
<!DOCTYPE html>
<html>
<head>
    <title>Jeu 4X - Carte du Jeu</title>
    <style>
        /* CONTAINER PRINCIPAL POUR LA GRILLE ET LES BOUTONS */
        .container {
            display: flex; /* Organisation en ligne */
            justify-content: center; /* Centre horizontalement la grille et les boutons */
            align-items: flex-start; /* Aligne les éléments au début verticalement */
            gap: 20px; /* Espace entre la grille et les boutons */
            margin: 20px; /* Espacement global autour */
        }

        /* CONTENEUR DES BOUTONS */
        .actions {
            display: flex;
            flex-direction: column; /* Empile les boutons verticalement */
            gap: 10px; /* Espacement entre chaque bouton */
            align-items: flex-start; /* Alignement des boutons à gauche dans leur section */
        }

        /* GRILLE */
        .game-grid {
            border-collapse: collapse; /* Fusionne les bordures des cellules */
            margin: 20px auto; /* Centre le tableau */
            background-color: #f0f0f0; /* Couleur de fond pour un contraste agréable */
            box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.25); /* Ombre pour donner un effet 3D */
        }

        .game-grid td {
            width: 50px; /* Taille uniforme des cellules */
            height: 50px; /* Idem */
            border: 1px solid #ccc; /* Bordure légère pour délimiter les cases */
            text-align: center; /* Centre le contenu dans la cellule */
            background-color: #e9e9e9; /* Couleur par défaut des cellules vides */
            position: relative; /* Nécessaire pour positionner des éléments internes */
        }

        /* STYLISATION DES TYPES DE TUILES */
        .game-grid .vide {
            background-color: #f4f4f4; /* Gris clair pour les tuiles vides */
        }

        .game-grid .ville {
            background-color: #ffd700; /* Doré pour les villes */
        }

        .game-grid .foret {
            background-color: #228b22; /* Vert foncé pour les forêts */
        }

        .game-grid .montagne {
            background-color: #8b4513; /* Marron pour les montagnes */
        }

        /* IMAGES DANS LES CELLULES */
        .game-grid td img {
            width: 100%; /* Ajuste l'image pour remplir la cellule */
            height: 100%; /* Ajuste l'image à la hauteur de la cellule */
            object-fit: cover; /* Garde les proportions de l'image */
            border-radius: 5px; /* Coins arrondis pour un effet esthétique */
            box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3); /* Ombre légère pour un effet de profondeur */
        }

        /* TITRE ET DIV PRINCIPAL */
        body {
            font-family: 'Arial', sans-serif; /* Police moderne et lisible */
            background-color: #2d2d2d; /* Fond sombre pour mettre en valeur le jeu */
            color: #fff; /* Texte blanc pour un contraste agréable */
            margin: 0;
            padding: 0;
        }

        h1 {
            text-align: center;
            margin-top: 20px;
            font-size: 28px;
            color: #ffd700; /* Couleur dorée pour le titre */
            text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7); /* Ombre pour l'effet stylisé */
        }

        /* BOUTONS */
        form button {
            background-color: #4CAF50; /* Vert agréable */
            color: white;
            border: none;
            padding: 10px 20px;
            font-size: 16px;
            margin: 5px 0; /* Espace vertical entre les boutons */
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        form button:hover {
            background-color: #45a049; /* Vert légèrement plus foncé au survol */
            transform: translateY(-3px); /* Légère levée au survol */
            box-shadow: 0px 5px 10px rgba(0, 0, 0, 0.3); /* Ombre au survol */
        }
    </style>
</head>
<body>
    <h1>Jeu 4X - Carte du Jeu</h1>

    <%
        // session est implicitement disponible dans une JSP
        // donc PAS de "HttpSession session = request.getSession();" ici

        Carte carte = (Carte) session.getAttribute("carte");

        if (carte == null) {
            // Si aucune carte n'existe, on la crée et on la stocke en session
            carte = new Carte(12, 12);
            carte.initialiserCarte();
            session.setAttribute("carte", carte);
        }

        // Générer le HTML de la carte
        String carteHTML = carte.toHTML();
    %>

    <div class="container">
        <!-- Conteneur de la grille -->
        <div class="game-grid-container">
            <h3>Carte :</h3>
            <div><%= carteHTML %></div>
        </div>

        <!-- Conteneur des actions (boutons) -->
        <div class="actions">
            <h3>Actions :</h3>
            <form action="game?action=move" method="post">
                <button type="submit" name="direction" value="north">Move North</button>
                <button type="submit" name="direction" value="south">Move South</button>
                <button type="submit" name="direction" value="east">Move East</button>
                <button type="submit" name="direction" value="west">Move West</button>
            </form>
        </div>
    </div>
</body>
</html>