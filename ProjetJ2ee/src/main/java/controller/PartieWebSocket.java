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

    /**
     * Liste globale des parties en cours (ou en attente).
     */
    private static final List<Partie> parties = new ArrayList<>();

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
        // Ex: queryString = "user=admin"
        String query = session.getQueryString();
        String pseudo = extraireParametre(query, "user");

        // S'il est vide ou absent, fallback "Joueur_sessionId"
        if (pseudo == null || pseudo.trim().isEmpty()) {
            pseudo = "Joueur_" + session.getId();
        }

        System.out.println("[WebSocket] Session ouverte : " + session.getId() + " | pseudo=" + pseudo);

        // On stocke la session dans 'clients' avec "" comme gameId initial
        clients.put(session, "");
        // On stocke aussi le pseudo dans 'sessionPseudoMap'
        sessionPseudoMap.put(session, pseudo);

        // Envoyer la liste des parties (pour que le nouveau voie ce qui existe déjà)
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
     * Fait rejoindre la partie indiquée par le paramètre gameId.
     * @param gameId identifiant de la partie
     * @param session session du joueur qui veut rejoindre
     */
    private void rejoindrePartie(String gameId, Session session) {
        Partie partie = trouverPartie(gameId);
        if (partie == null) {
            envoyerMessageErreur(session, "Partie introuvable.");
            return;
        }

        // On récupère le pseudo
        String pseudo = sessionPseudoMap.get(session);
        if (pseudo == null) pseudo = "Joueur_" + session.getId();

        System.out.println("Tentative rejoindrePartie: " + gameId + " par " + pseudo);

        Joueur joueur = new Joueur(pseudo);
        boolean ok = partie.ajouterJoueur(joueur);
        if (!ok) {
            envoyerMessageErreur(session, "La partie est pleine.");
            return;
        }

        // On stocke dans 'clients' que ce session est dans ce gameId
        clients.put(session, gameId);

        System.out.println("Rejoint OK : " + joueur.getLogin() + " -> " + partie.getGameId());
        // On broadcast la nouvelle liste
        envoyerListeParties();

        // On envoie un message de redirection à ce joueur pour aller dans salleAttente.jsp
        try {
            String contextPath = "/ProjetJ2ee"; 
            // on ajoute &user=...
            String redirectMessage = "{\"redirect\":\""
                + contextPath
                + "/vue/salleAttente.jsp?gameId=" + gameId
                + "&user=" + pseudo
                + "\"}";

            session.getBasicRemote().sendText(redirectMessage);
        } catch (Exception e) {
            System.err.println("Erreur envoi redirect : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Lance la partie indiquée par gameId (réservée au créateur).
     * @param gameId identifiant de la partie
     * @param session session du joueur appelant la commande
     */
    private void lancerPartie(String gameId, Session session) {
        Partie partie = trouverPartie(gameId);
        if (partie == null) {
            envoyerMessageErreur(session, "Partie introuvable.");
            return;
        }

        // Vérifie si c'est bien le créateur
        String loginCreateur = partie.getCreateur();
        String monPseudo = sessionPseudoMap.get(session);
        if (monPseudo == null) monPseudo = "Joueur_" + session.getId();

        if (!monPseudo.equals(loginCreateur)) {
            envoyerMessageErreur(session, "Vous n'êtes pas le créateur de cette partie.");
            return;
        }

        // OK, on lance la partie
        partie.setEnCours(true);
        System.out.println("Partie " + partie.getGameId() + " lancée par " + monPseudo);

        // On peut initialiser la carte ici (exemple)
        partie.setCarte(new Carte(15, 15));
        partie.getCarte().initialiserCarte();

        // On informe tout le monde que la partie est lancée
        envoyerListeParties();
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
     *   {"gameId":"GAME-1234","nom":"Test","maxJoueurs":3,"createur":"admin","enCours":false,"joueurs":["admin"]},
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
                sb.append("\"").append(listeJoueurs.get(j).getLogin()).append("\"");
                if (j < listeJoueurs.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");
            if (i < parties.size() - 1) {
                sb.append(",");
            }
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
}
