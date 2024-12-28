package controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import model.Partie;
import model.Joueur;

@ServerEndpoint("/ws/parties")
public class PartieWebSocket {
	private static final Map<Session, String> clients = new ConcurrentHashMap<>();

	static {
	    if (clients == null) {
	        System.err.println("Erreur critique : La carte 'clients' n'a pas été initialisée.");
	    } else {
	        System.out.println("Initialisation réussie de la carte 'clients'.");
	    }
	}


    private static final List<Partie> parties = new ArrayList<>(); // Liste des parties

    @OnOpen
    public void onOpen(Session session) {
        try {
            if (session == null) {
                System.err.println("Erreur : La session est null.");
                return;
            }

            // Debug session properties
            System.out.println("Session ouverte avec ID : " + session.getId());
            System.out.println("URI de la session : " + session.getRequestURI());

            // Vérifiez si clients est bien initialisé
            if (clients == null) {
                System.err.println("Erreur : La carte 'clients' est null.");
                return;
            }

            // Ajoutez la session au ConcurrentHashMap
            clients.put(session, "");
            System.out.println("Session ajoutée avec succès.");
            envoyerListeParties();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de la session : " + e.getMessage());
            e.printStackTrace();
        }
    }




    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message reçu de " + session.getId() + ": " + message);
        try {
            Map<String, String> data = parseMessage(message);
            String action = data.get("action");
            System.out.println("Action demandée : " + action);


            switch (action) {
                case "creerPartie":
                    System.out.println("Action : Créer une partie.");
                    creerPartie(data.get("nom"), Integer.parseInt(data.get("maxJoueurs")));
                    break;

                case "rejoindrePartie":
                    System.out.println("Action : Rejoindre une partie.");
                    rejoindrePartie(data.get("gameId"), session);
                    break;

                default:
                    System.out.println("Action inconnue : " + action);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (session == null) {
            System.err.println("Session fermée (null).");
            return;
        }

        System.out.println("Connexion WebSocket fermée : " + session.getId());
        String gameId = clients.remove(session);
        if (gameId != null) {
            Partie partie = trouverPartie(gameId);
            if (partie != null) {
                partie.retirerJoueur(new Joueur("Joueur_" + session.getId()));
                if (partie.getJoueurs().isEmpty()) {
                    parties.remove(partie); // Supprimer la partie si elle est vide
                }
                envoyerListeParties();
            }
        }
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        if (session != null) {
            System.err.println("Erreur WebSocket détectée pour la session " + session.getId() + ": " + throwable.getMessage());
        } else {
            System.err.println("Erreur WebSocket détectée (session null) : " + throwable.getMessage());
        }
        throwable.printStackTrace();
    }


    private void creerPartie(String nom, int maxJoueurs) {
        System.out.println("Création d'une nouvelle partie : Nom=" + nom + ", MaxJoueurs=" + maxJoueurs);
        Partie partie = new Partie(nom, maxJoueurs);
        System.out.println("ID de la nouvelle partie : " + partie.getGameId()); // Log pour vérifier l'ID
        parties.add(partie);
        envoyerListeParties();
    }

    private void rejoindrePartie(String gameId, Session session) {
        System.out.println("Tentative de rejoindre la partie : " + gameId); // Log du gameId
        Partie partie = trouverPartie(gameId);
        if (partie != null) {
            System.out.println("Partie trouvée : " + partie.getNomPartie());
            boolean joueurAjouté = partie.ajouterJoueur(new Joueur("Joueur_" + session.getId()));
            if (joueurAjouté) {
                System.out.println("Joueur ajouté à la partie : " + gameId);
                clients.put(session, gameId);
                envoyerListeParties();
            } else {
                System.out.println("Échec : La partie est pleine.");
                envoyerMessageErreur(session, "La partie est pleine.");
            }
        } else {
            System.out.println("Échec : Partie introuvable.");
            envoyerMessageErreur(session, "Partie introuvable.");
        }
    }



    private Partie trouverPartie(String gameId) {
        System.out.println("Recherche de la partie avec ID : " + gameId);
        return parties.stream()
                .filter(p -> p.getGameId().equals(gameId))
                .findFirst()
                .orElse(null);
    }

    private void envoyerListeParties() {
        System.out.println("Envoi de la liste des parties aux clients.");
        String json = convertirPartiesEnJSON();
        clients.keySet().forEach(session -> {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi des parties au client " + session.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void envoyerMessageErreur(Session session, String message) {
        try {
            String json = String.format("{\"error\":\"%s\"}", message);
            System.out.println("Envoi d'un message d'erreur au client " + session.getId() + ": " + message);
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message d'erreur au client " + session.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String convertirPartiesEnJSON() {
        System.out.println("Conversion des parties en JSON.");
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < parties.size(); i++) {
            Partie partie = parties.get(i);
            System.out.println("Partie incluse dans JSON : " + partie.getGameId() + " - " + partie.getNomPartie()); // Log
            jsonBuilder.append("{")
                    .append("\"gameId\":\"").append(partie.getGameId()).append("\",")
                    .append("\"nom\":\"").append(partie.getNomPartie()).append("\",")
                    .append("\"maxJoueurs\":").append(partie.getMaxJoueurs()).append(",")
                    .append("\"joueurs\":[");

            List<Joueur> joueurs = partie.getJoueurs();
            for (int j = 0; j < joueurs.size(); j++) {
                Joueur joueur = joueurs.get(j);
                jsonBuilder.append("\"").append(joueur.getLogin()).append("\"");
                if (j < joueurs.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]}");
            if (i < parties.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        System.out.println("JSON généré : " + jsonBuilder.toString()); // Log du JSON complet
        return jsonBuilder.toString();
    }


    private Map<String, String> parseMessage(String message) {
        System.out.println("Parsing du message : " + message);
        Map<String, String> data = new HashMap<>();
        String[] pairs = message.replace("{", "").replace("}", "").split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            data.put(keyValue[0].trim().replace("\"", ""), keyValue[1].trim().replace("\"", ""));
        }
        return data;
    }
}
