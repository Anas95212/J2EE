package controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import model.Partie;
import model.Carte;
import model.Joueur;
import model.Soldat;
import model.Tuile;

/**
 * Gère la communication WebSocket sur l'URL "/ws/parties".
 * Reçoit des actions JSON (ex: {"action":"creerPartie","nom":"Test","maxJoueurs":4}).
 */
@ServerEndpoint("/ws/parties")
public class PartieWebSocket {

    /**
     * Associe chaque Session WebSocket à l'ID de partie qu'il a rejointe.
     * - La clé est la session WebSocket
     * - La valeur est le 'gameId' (ou "" si pas encore dans une partie).
     */
    private static final Map<Session, String> clients = new ConcurrentHashMap<>();

    /**
     * Associe chaque Session WebSocket à un pseudo (login).
     * - La clé est la session WebSocket
     * - La valeur est le pseudo (ex: "admin").
     */
    private static final Map<Session, String> sessionPseudoMap = new ConcurrentHashMap<>();
    
 // Associe chaque pseudo (login) à un gameId
    private static final Map<String, String> pseudoToGameId = new ConcurrentHashMap<>();


    /**
     * Liste globale des parties en cours (ou en attente).
     */
    private static final List<Partie> parties = new ArrayList<>();

    public static List<Partie> getParties() {
        return parties;
    }

    /**
     * Méthode appelée lorsqu'un client ouvre la connexion WebSocket.
     * On y lit le pseudo via ?user=Pseudo dans la query string.
     */

    @OnOpen
    public void onOpen(Session session) {
        if (session == null) {
            System.err.println("Erreur : session WebSocket null");
            return;
        }

        String query = session.getQueryString();
        String pseudo = extraireParametre(query, "user");

        if (pseudo == null || pseudo.trim().isEmpty()) {
            pseudo = "Joueur_" + session.getId();
        }

        System.out.println("[WebSocket] Session ouverte : " + session.getId() + " | pseudo=" + pseudo);

        // Ajoute ou met à jour la session
        sessionPseudoMap.put(session, pseudo);
        clients.put(session, ""); // Pas encore de partie associée

        // Si le pseudo est déjà associé à un gameId, le récupérer
        if (pseudoToGameId.containsKey(pseudo)) {
            String gameId = pseudoToGameId.get(pseudo);
            clients.put(session, gameId);
            System.out.println("[WebSocket] Reconnexion du pseudo " + pseudo + " à la partie " + gameId);
        }

        // Envoyer la liste des parties
        envoyerListeParties();
    }



    /**
     * Méthode appelée lorsqu'un message est reçu (en texte) depuis un client.
     * @param message le contenu texte du message
     * @param session la session qui a envoyé le message
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("[WebSocket] Message reçu de " + session.getId() + ": " + message);

        try {
            // On parse le message JSON en un Map simple
            Map<String, String> data = parseMessage(message);
            String action = data.get("action");
            System.out.println("Action demandée : " + action);

            if (action == null) {
                System.err.println("Action nulle ou non reconnue.");
                return;
            }

            switch (action) {
                case "creerPartie":
                    // ex: {"action":"creerPartie","nom":"Test","maxJoueurs":"4"}
                    creerPartie(
                        data.get("nom"),
                        Integer.parseInt(data.get("maxJoueurs")),
                        session
                    );
                    break;

                case "rejoindrePartie":
                    // ex: {"action":"rejoindrePartie","gameId":"GAME-168493..."}
                    rejoindrePartie(data.get("gameId"), session);
                    break;

                case "lancerPartie":
                    // ex: {"action":"lancerPartie","gameId":"GAME-168493..."}
                    lancerPartie(data.get("gameId"), session);
                    break;

                case "choisirCouleur":
                    choisirCouleur(
                        data.get("gameId"),
                        data.get("couleur"),
                        session
                    );
                    break;

                default:
                    System.out.println("Action inconnue: " + action);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur onMessage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode appelée lorsque la connexion WebSocket se ferme (côté client ou serveur).
     * @param session la session fermée
     */
    @OnClose
    public void onClose(Session session) {
        System.out.println("[WebSocket] Session fermée : " + session.getId());

        // Retire la pseudoMap
        String pseudo = sessionPseudoMap.remove(session);
        if (pseudo == null) pseudo = "Inconnu_" + session.getId();

        // Retire le gameId dans la map
        String gameId = clients.remove(session);
        if (gameId != null && !gameId.isEmpty()) {
            // Retirer le joueur de la partie correspondante
            Partie partie = trouverPartie(gameId);
            if (partie != null) {
                // On retire le joueur (celui qui a ce pseudo)
                partie.retirerJoueur(new Joueur(pseudo));

                // Si la partie est désormais vide, on l'enlève complètement
                if (partie.getJoueurs().isEmpty()) {
                    parties.remove(partie);
                }
                envoyerListeParties();
            }
        }
    }

    /**
     * Méthode appelée en cas d'erreur sur la WebSocket.
     * @param session la session concernée (si disponible)
     * @param throwable l'erreur rencontrée
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[WebSocket] Erreur : " + throwable.getMessage());
        throwable.printStackTrace();
    }

    // ----------------------------------------------------------------------
    // Méthodes d'actions
    // ----------------------------------------------------------------------

    /**
     * Ajoute une couleur au joueur dans la partie, sans redirection
     * (restent dans la salle d'attente).
     */
    private void choisirCouleur(String gameId, String couleur, Session session) {
        Partie partie = trouverPartie(gameId);
        if (partie == null) {
            envoyerMessageErreur(session, "Partie introuvable pour choisirCouleur.");
            return;
        }
        String pseudo = sessionPseudoMap.get(session);
        if (pseudo == null) pseudo = "Joueur_" + session.getId();

        // Vérif si déjà prise
        for (Joueur j : partie.getJoueurs()) {
            if (couleur.equalsIgnoreCase(j.getCouleur())) {
                envoyerMessageErreur(session, "Couleur déjà prise.");
                return;
            }
        }
        // Assigner
        for (Joueur j : partie.getJoueurs()) {
            if (j.getLogin().equals(pseudo)) {
                j.setCouleur(couleur);
                break;
            }
        }
        // Pas de redirect => On met juste à jour la liste
        envoyerListeParties();
    }

    /**
     * Crée une nouvelle partie et l'ajoute à la liste.
     * @param nom nom de la partie
     * @param maxJoueurs nombre maximal de joueurs
     * @param session session WebSocket du créateur (pour l'identifier)
     */
    private void creerPartie(String nom, int maxJoueurs, Session session) {
        // On récupère le pseudo depuis sessionPseudoMap
        String createur = sessionPseudoMap.get(session);
        if (createur == null) {
            // fallback
            createur = "Joueur_" + session.getId();
        }

        System.out.println("Création d'une nouvelle partie : nom=" + nom + " | créateur=" + createur);

        Partie partie = new Partie(nom, maxJoueurs, createur);
        parties.add(partie);

        System.out.println("Partie créée : " + partie.getGameId());
        // On informe tout le monde qu'une nouvelle partie est dispo
        envoyerListeParties();
    }

    /**
     * Fait simplement rejoindre la partie indiquée par le paramètre gameId,
     * sans rediriger vers /game. Les joueurs restent en salle d'attente.
     * @param gameId identifiant de la partie
     * @param session session du joueur qui veut rejoindre
     */
    private void rejoindrePartie(String gameId, Session session) {
        // 1) Retrouver la partie
        Partie partie = trouverPartie(gameId);
        if (partie == null) {
            envoyerMessageErreur(session, "Partie introuvable.");
            return;
        }

        // 2) Récupérer le pseudo
        String pseudo = sessionPseudoMap.get(session);
        if (pseudo == null || pseudo.trim().isEmpty()) {
            pseudo = "Joueur_" + session.getId();
        }

        // 3) Créer un nouveau Joueur et tenter de l’ajouter
        Joueur joueur = new Joueur(pseudo);
        boolean ok = partie.ajouterJoueur(joueur);
        if (!ok) {
            // La partie est peut-être pleine
            envoyerMessageErreur(session, "Impossible de rejoindre : la partie est pleine ou indisponible.");
            return;
        }

        // 4) Associe le pseudo au gameId
        pseudoToGameId.put(pseudo, gameId);

        // On associe également ce WebSocket à ce gameId (dans clients)
        clients.put(session, gameId);

        // 5) Diffuser la liste des parties à tous
        envoyerListeParties();

        // 6) Rediriger ce joueur vers salleAttente.jsp
        try {
            // Adapte ton contexte si besoin
            String contextPath = "/ProjetJ2ee";
            // On veut pointer vers /vue/salleAttente.jsp?gameId=...&user=...
            String redirectUrl = contextPath 
                + "/vue/salleAttente.jsp?gameId=" + gameId 
                + "&user=" + pseudo;

            // On fabrique un JSON { "redirect" : "..." }
            String redirectJson = "{\"redirect\":\"" + redirectUrl + "\"}";

            // On envoie ce message au SEUL joueur en question
            session.getBasicRemote().sendText(redirectJson);
            System.out.println("[WebSocket] Redirection envoyée pour " + pseudo + " vers " + redirectUrl);

        } catch (Exception e) {
            System.err.println("Erreur lors de la redirection pour rejoindrePartie : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Lance la partie indiquée par gameId (réservée au créateur).
     * => Redirige tous les joueurs vers /game?gameId=...
     * @param gameId identifiant de la partie
     * @param session session du joueur appelant la commande
     */
    private void lancerPartie(String gameId, Session session) {
        // 1) Retrouver la partie
        Partie partie = trouverPartie(gameId);
        if (partie == null) {
            envoyerMessageErreur(session, "Partie introuvable.");
            return;
        }

        // 2) Vérifier si c’est le créateur (optionnel)
        String pseudoCreateur = partie.getCreateur();
        String monPseudo = sessionPseudoMap.get(session); 
        if (monPseudo == null) monPseudo = "Joueur_" + session.getId();

        if (!monPseudo.equals(pseudoCreateur)) {
            envoyerMessageErreur(session, "Vous n'êtes pas le créateur de cette partie.");
            return;
        }

        // 3) Marquer la partie comme "enCours"
        partie.setEnCours(true);

        // 4) Assigner des couleurs aléatoires aux joueurs qui n’en ont pas
        // Ex : Palette de 6 couleurs
        List<String> palette = new ArrayList<>(List.of(
            "#0000FF", "#FF0000", "#00FF00", "#FFA500", "#FFFF00", "#800080"
        ));
        // Retirer celles déjà prises
        for (Joueur j : partie.getJoueurs()) {
            if (j.getCouleur() != null && !j.getCouleur().isEmpty()) {
                palette.remove(j.getCouleur());
            }
        }

        Random rand = new Random();
        for (Joueur j : partie.getJoueurs()) {
            if (j.getCouleur() == null || j.getCouleur().isEmpty()) {
                if (!palette.isEmpty()) {
                    // On assigne une couleur aléatoire
                    int idx = rand.nextInt(palette.size());
                    j.setCouleur(palette.remove(idx));
                } else {
                    // Au cas où on n’a plus de couleur dispo
                    j.setCouleur("#AAAAAA");
                }
            }
        }

        // 5) Placer 1 soldat par joueur
        Carte c = partie.getCarte();
        for (Joueur j : partie.getJoueurs()) {
            // Créer un soldat (en supposant Soldat(...) a un constructeur
            // qui prend le Joueur en param, ou on le setOwner(j).
            // Ici on utilise un constructeur custom:
            Soldat s = new Soldat(0, 0, 10, 20, j);
            // Chercher une tuile libre pour le soldat
            int x, y;
            do {
                x = rand.nextInt(c.getLignes());
                y = rand.nextInt(c.getColonnes());
            } while (!tuileValidePourSoldat(c.getTuile(x, y)));

            // Positionner le soldat
            s.setPositionX(x);
            s.setPositionY(y);
            // L'ajouter au joueur
            j.ajouterUnite(s);

            // Poser le soldat sur la tuile
            Tuile t = c.getTuile(x, y);
            t.setSoldatPresent(s);
        }

        // 6) Informer tout le monde que la partie est lancée (mise à jour de la liste)
        envoyerListeParties();

        // 7) Envoyer un message de redirection à *tous les joueurs* de ce gameId
        // => ainsi, chacun quitte salleAttente et arrive sur /game?gameId=...
        try {
            String contextPath = "/ProjetJ2ee"; // ou ton contexte
            String redirectUrl = contextPath + "/game?gameId=" + gameId;
            String redirectMessage = "{\"redirect\":\"" + redirectUrl + "\"}";

            // On envoie ce redirectMessage à TOUTES les sessions de *cette* partie
            for (Session s : clients.keySet()) {
                if (gameId.equals(clients.get(s))) {
                    s.getBasicRemote().sendText(redirectMessage);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message de redirection : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour vérifier qu'une tuile est valide 
     * pour placer un soldat initial (pas montagne, pas ville, pas déjà un soldat).
     */
    private boolean tuileValidePourSoldat(Tuile t) {
        if (t == null) return false;
        if (t.getSoldatPresent() != null) return false;
        if (t.getBaseType() == Tuile.TypeTuile.MONTAGNE) return false;
        if (t.getBaseType() == Tuile.TypeTuile.VILLE) return false;
        // On autorise FORET ou VIDE
        return true;
    }



    // ----------------------------------------------------------------------
    // Méthodes utilitaires
    // ----------------------------------------------------------------------

    /**
     * Recherche une partie dans la liste par son identifiant.
     * @param gameId identifiant de la partie
     * @return la partie trouvée ou null si introuvable
     */
    private Partie trouverPartie(String gameId) {
        for (Partie p : parties) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Construit un JSON (tableau) représentant la liste des parties,
     * puis l'envoie à tous les clients connectés.
     */
    private void envoyerListeParties() {
        String json = convertirPartiesEnJSON();
        for (Session s : clients.keySet()) {
            try {
                s.getBasicRemote().sendText(json);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi à la session " + s.getId() + " : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Envoie un message d'erreur (format JSON) au client donné.
     * @param session session WebSocket destinataire
     * @param message contenu de l'erreur
     */
    private void envoyerMessageErreur(Session session, String message) {
        try {
            String json = "{\"error\":\"" + message + "\"}";
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'erreur à " + session.getId() + " : " + e.getMessage());
        }
    }

    /**
     * Génère une chaîne JSON décrivant toutes les parties,
     * ex: 
     * [
     *   {"gameId":"GAME-1234","nom":"Test","maxJoueurs":3,"createur":"admin","enCours":false,"joueurs":[{"login":"admin","couleur":"#FF0000"}]},
     *   ...
     * ]
     * @return une chaîne JSON
     */
    private String convertirPartiesEnJSON() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parties.size(); i++) {
            Partie p = parties.get(i);
            sb.append("{")
              .append("\"gameId\":\"").append(p.getGameId()).append("\",")
              .append("\"nom\":\"").append(p.getNomPartie()).append("\",")
              .append("\"maxJoueurs\":").append(p.getMaxJoueurs()).append(",")
              .append("\"createur\":\"").append(p.getCreateur()).append("\",")
              .append("\"enCours\":").append(p.isEnCours()).append(",")
              .append("\"joueurs\":[");
            List<Joueur> listeJoueurs = p.getJoueurs();
            for (int j = 0; j < listeJoueurs.size(); j++) {
                Joueur jj = listeJoueurs.get(j);
                sb.append("{");
                sb.append("\"login\":\"").append(jj.getLogin()).append("\",");
                sb.append("\"couleur\":\"").append(jj.getCouleur() == null ? "" : jj.getCouleur()).append("\"");
                sb.append("}");
                if (j < listeJoueurs.size() - 1) sb.append(",");
            }
            sb.append("]}");
            if (i < parties.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parse grossièrement un message JSON de type {"action":"...","gameId":"..."}
     * et renvoie une Map clé -> valeur.
     * @param message le message JSON sous forme de chaîne
     * @return un map contenant les clés/valeurs extraites
     */
    private Map<String, String> parseMessage(String message) {
        Map<String, String> data = new HashMap<>();
        String[] pairs = message.replace("{", "").replace("}", "").split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim().replace("\"", "");
                data.put(k, v);
            }
        }
        return data;
    }

    /**
     * Extrait la valeur d'un paramètre de query string (ex: "user=admin&foo=bar").
     * @param query ex: "user=admin&foo=bar"
     * @param paramName ex: "user"
     * @return la valeur du paramètre décodée, ou null si non trouvée
     */
    private String extraireParametre(String query, String paramName) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                String key = kv[0];
                String val = kv[1];
                if (key.equals(paramName)) {
                    return URLDecoder.decode(val, StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
    
    public static void broadcastGameUpdate(String gameId) {
        Partie partie = getPartieById(gameId);
        if (partie == null) {
            System.err.println("Erreur : Partie introuvable pour gameId=" + gameId);
            return;
        }
 
        String jsonMessage = convertirCarteAvecJoueurEnJSON(partie);
        System.out.println("[WebSocket] Diffusion de l'état de la partie : " + jsonMessage);
 
        for (Session session : clients.keySet()) {
            String clientGameId = clients.get(session);
            if (gameId.equals(clientGameId)) {
                try {
                    // Diffuse à tous les joueurs connectés à la partie
                    session.getBasicRemote().sendText(jsonMessage);
                    System.out.println("[WebSocket] Message envoyé à la session : " + session.getId());
                } catch (Exception e) {
                    System.err.println("[WebSocket] Erreur lors de l'envoi au client : " + session.getId());
                    e.printStackTrace();
                }
            }
        }
    }
   
    
    private static String convertirCarteAvecJoueurEnJSON(Partie partie) {
        Carte carte = partie.getCarte();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"type\":\"update\",");
        json.append("\"gameId\":\"").append(partie.getGameId()).append("\",");
        json.append("\"htmlCarte\":\"").append(carte.toHTML(partie.getGameId()).replace("\"", "\\\"")).append("\",");
        json.append("\"currentPlayer\":\"").append(partie.getJoueurs().get(partie.getIndexJoueurActuel()).getLogin()).append("\"");
        json.append("}");
        return json.toString();
    }

    private static Partie getPartieById(String gameId) {
        for (Partie p : parties) {
            if (p.getGameId().equals(gameId)) {
                return p;
            }
        }
        return null;
    }
    public static void broadcastCombatStart(String gameId, String combatId) {
        // Construire l'URL de redirection
        String contextPath = "/ProjetJ2ee"; // Adaptez selon votre contexte
        String redirectUrl = contextPath + "/vue/combat.jsp?gameId=" + gameId + "&combatId=" + combatId;

        // Construire le message JSON pour redirection
        String msg = "{\"type\":\"combatStart\",\"redirect\":\"" + redirectUrl + "\"}";
        System.out.println("[WebSocket] Envoi du message combatStart : " + msg);

        // Envoyer le message à tous les clients connectés à ce gameId
        for (Session s : clients.keySet()) {
            String clientGameId = clients.get(s); // Obtenir le gameId associé à cette session
            System.out.println("Session: " + s);
            if (gameId.equals(clientGameId)) { // Filtrer par gameId
                try {
                    s.getBasicRemote().sendText(msg); // Envoyer le message
                    System.out.println("[WebSocket] Message combatStart envoyé à la session : " + s.getId());
                } catch (Exception e) {
                    System.err.println("[WebSocket] Erreur lors de l'envoi du message à la session : " + s.getId() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }



    public static void broadcastCombatEnd(String gameId) {
        String contextPath = "/ProjetJ2ee"; // Adaptez selon votre configuration
        String redirectUrl = contextPath + "/game?gameId=" + gameId;
        String msg = "{\"type\":\"combatEnd\",\"redirect\":\"" + redirectUrl + "\"}";

        for (Session s : clients.keySet()) {
            if (gameId.equals(clients.get(s))) {
                try {
                    s.getBasicRemote().sendText(msg);
                } catch (Exception e) {
                    System.err.println("[WebSocket] Erreur lors de l'envoi du message de fin de combat à la session : " + s.getId());
                    e.printStackTrace();
                }
            }
        }
    }

    private static String convertirCarteEnJSON(Partie partie) {
        Carte carte = partie.getCarte();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"type\":\"update\",");
        json.append("\"gameId\":\"").append(partie.getGameId()).append("\",");
        json.append("\"htmlCarte\":\"").append(carte.toHTML(partie.getGameId()).replace("\"", "\\\"")).append("\"");
        json.append("}");
        return json.toString();
    }
    
    public static void notifyGameStateUpdate(String gameId) {
        Partie partie = null;
        for (Partie p : parties) {
            if (p.getGameId().equals(gameId)) {
                partie = p;
                break;
            }
        }

        if (partie == null) {
            System.err.println("Partie introuvable pour gameId=" + gameId);
            return;
        }

        // Construire l'objet JSON contenant l'état de la partie
        String json;
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"type\":\"gameUpdate\",");
            sb.append("\"gameId\":\"").append(gameId).append("\",");
            sb.append("\"currentPlayer\":\"").append(partie.getJoueurs().get(partie.getIndexJoueurActuel()).getLogin()).append("\",");
            sb.append("\"carte\":").append(partie.getCarte().toJSON()); // Ajoute une méthode toJSON dans Carte
            sb.append("}");
            json = sb.toString();
        } catch (Exception e) {
            System.err.println("Erreur lors de la construction du JSON de gameUpdate : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Envoyer l'état de la partie à tous les joueurs de la partie
        for (Session s : clients.keySet()) {
            if (gameId.equals(clients.get(s))) {
                try {
                    s.getBasicRemote().sendText(json);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi de l'update à la session " + s.getId() + " : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    
    public static void broadcastRefresh(String gameId) {
        String msg = "{\"type\":\"refresh\",\"gameId\":\"" + gameId + "\"}";
        System.out.println("[WebSocket] Envoi du message refresh : " + msg);

        for (Session s : clients.keySet()) {
            String clientGameId = clients.get(s);
            if (gameId.equals(clientGameId)) {
                try {
                    // Envoi uniquement le message sans affecter l'état de la partie
                    s.getBasicRemote().sendText(msg);
                    System.out.println("[WebSocket] Refresh envoyé au client : " + s.getId());
                } catch (Exception e) {
                    System.err.println("[WebSocket] Erreur lors de l'envoi du refresh au client " + s.getId() + " : " + e.getMessage());
                }
            }
        }
    }
    
    
    public static void broadcastDefeat(String gameId, String pseudo) {
    	Partie partie = getPartieById(gameId);
        if (partie == null) {
            System.err.println("Erreur : Partie introuvable pour gameId=" + gameId);
            return;
        }

        Joueur joueurElimine = partie.getJoueurs().stream()
                                     .filter(j -> j.getLogin().equals(pseudo))
                                     .findFirst()
                                     .orElse(null);

        if (joueurElimine == null) {
            System.err.println("Erreur : Joueur introuvable pour pseudo=" + pseudo);
            return;
        }
        String msg = "{\"type\":\"defeat\",\"pseudo\":\"" + pseudo + "\",\"score\":" + joueurElimine.getScore() + "}";
        for (Session s : clients.keySet()) {
            if (gameId.equals(clients.get(s))) {
                try {
                    s.getBasicRemote().sendText(msg);
                    System.out.println("[WebSocket] Envoi du message des score defaite : " + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        broadcastScores(partie.getGameId(),partie.getJoueurs());
    }

    public static void broadcastGameEnd(String gameId) {
        String msg = "{\"type\":\"gameEnd\"}";
        for (Session s : clients.keySet()) {
            if (gameId.equals(clients.get(s))) {
                try {
                    s.getBasicRemote().sendText(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void broadcastVictory(String gameId, String pseudo) {
        Partie partie = getPartieById(gameId);
        if (partie == null) {
            System.err.println("Erreur : Partie introuvable pour gameId=" + gameId);
            return;
        }
        Joueur vainqueur = partie.getJoueurs().stream()
                .filter(j -> j.getLogin().equals(pseudo))
                .findFirst()
                .orElse(null);
        
        // Construire le message JSON complet
        String msg = "{\"type\":\"victory\",\"pseudo\":\"" + pseudo + "\",\"score\":" + vainqueur.getScore() + "}";

        // Envoyer le message à tous les joueurs de la partie
        for (Session s : clients.keySet()) {
            if (gameId.equals(clients.get(s))) {
                try {
                    s.getBasicRemote().sendText(msg);
                    System.out.println("[WebSocket] Envoi du message des score pour le vainqueur : " + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        broadcastScores(partie.getGameId(),partie.getJoueurs());
    }


    public static void fetchScores(String gameId, Session session) {
        Partie partie = getPartieById(gameId);
        if (partie == null) {
            System.err.println("Erreur : Partie introuvable pour gameId=" + gameId);
            return;
        }

        // Construire un tableau JSON contenant les scores des joueurs
        StringBuilder scoresJson = new StringBuilder("[");
        List<Joueur> joueurs = partie.getJoueurs();
        for (int i = 0; i < joueurs.size(); i++) {
            Joueur joueur = joueurs.get(i);
            scoresJson.append("{\"pseudo\":\"").append(joueur.getLogin())
                      .append("\",\"score\":").append(joueur.getScore()).append("}");
            if (i < joueurs.size() - 1) {
                scoresJson.append(",");
            }
        }
        scoresJson.append("]");

        // Construire le message JSON complet
        String msg = "{\"type\":\"scores\",\"scores\":" + scoresJson + "}";

        // Envoyer le message uniquement à la session demandante
        try {
            session.getBasicRemote().sendText(msg);
            System.out.println("[WebSocket] Scores envoyés au joueur : " + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    public static void broadcastScores(String gameId, List<Joueur> joueurs) {
        StringBuilder scoresJson = new StringBuilder("{\"type\":\"scores\",\"scores\":[");
        for (int i = 0; i < joueurs.size(); i++) {
            Joueur joueur = joueurs.get(i);
            scoresJson.append("{\"pseudo\":\"").append(joueur.getLogin()).append("\",")
                      .append("\"score\":").append(joueur.getScore()).append("}");
            if (i < joueurs.size() - 1) scoresJson.append(",");
        }
        scoresJson.append("]}");

        for (Session session : clients.keySet()) {
            if (gameId.equals(clients.get(session))) {
                try {
                    session.getBasicRemote().sendText(scoresJson.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    
    
}