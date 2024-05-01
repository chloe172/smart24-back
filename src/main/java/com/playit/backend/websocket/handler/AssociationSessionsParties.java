package com.playit.backend.websocket.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.web.socket.WebSocketSession;

import com.playit.backend.model.Partie;

public class AssociationSessionsParties {

	private static HashMap<Long, List<WebSocketSession>> equipesParPartie = new HashMap<Long, List<WebSocketSession>>();

    private static HashMap<Long, WebSocketSession> maitreDuJeuParPartie = new HashMap<Long, WebSocketSession>();

    public static void ajouterSessionEquipeAPartie(WebSocketSession equipe, Partie partie) {
        Long idPartie = partie.getId();
        List<WebSocketSession> equipes = equipesParPartie.get(idPartie);
        if(equipes == null) {
            equipes = new ArrayList<>();
            equipesParPartie.put(idPartie, equipes);
        }
        equipes.add(equipe);
    }

    public static void associerSessionMaitreDuJeuAPartie(WebSocketSession maitreDuJeu, Partie partie) {
        Long idPartie = partie.getId();
        maitreDuJeuParPartie.put(idPartie, maitreDuJeu);
    }

    public static List<WebSocketSession> getEquipesParPartie(Partie partie) {
        return equipesParPartie.get(partie.getId());
    }

    public static WebSocketSession getMaitreDuJeuPartie(Partie partie) {
        return maitreDuJeuParPartie.get(partie.getId());
    }

    public static void retirerSession(WebSocketSession session) {
        for(Long idPartie : equipesParPartie.keySet()) {
            List<WebSocketSession> equipes = equipesParPartie.get(idPartie);
            if(equipes.contains(session)) {
                equipes.remove(session);
            }
        }
        maitreDuJeuParPartie.values().remove(session);
    }
    
}